package com.example.androidproject1.cart

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject1.R
import com.example.androidproject1.adapter.CartAdapter
import com.example.androidproject1.model.CartItem
import com.example.androidproject1.payment.CheckoutActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartActivity : AppCompatActivity() {

    private lateinit var rvCart: RecyclerView
    private lateinit var tvEmptyCart: TextView
    private lateinit var btnStartShopping: Button
    private lateinit var tvSubTotal: TextView
    private lateinit var tvDeliveryFee: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnContinue: Button
    private lateinit var backArrow: ImageView
    private lateinit var llEmptyCart: LinearLayout

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var cartAdapter: CartAdapter
    private var cartItems = mutableListOf<CartItem>()
    var subTotal = 0.0
    private var deliveryFee=0.0
    private var taxAmount=0.0
    private var totalAmount=0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        initViews()
        setupRecyclerView()
        setupClickListeners()
        loadCartItems()
        clearFields()
    }

    private fun initViews() {
        rvCart = findViewById(R.id.rv_cart)
        tvEmptyCart = findViewById(R.id.tv_empty_cart)
        btnStartShopping = findViewById(R.id.bt_start_shopping)
        tvSubTotal = findViewById(R.id.tv_sub_total)
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee)
        tvTax = findViewById(R.id.tv_tax)
        tvTotal = findViewById(R.id.tv_total)
        btnContinue = findViewById(R.id.bt_cart_continue)
        backArrow = findViewById(R.id.imageview_back_arrow)
        llEmptyCart = findViewById(R.id.ll_empty_cart)
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(cartItems, this::onItemRemoved, this::onQuantityChanged)
        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = cartAdapter
    }

    private fun setupClickListeners() {
        backArrow.setOnClickListener {
            Toast.makeText(this, "Back clicked", Toast.LENGTH_SHORT).show()
            onBackPressedDispatcher.onBackPressed()
        }


        btnStartShopping.setOnClickListener {
            finish()
        }

        btnContinue.setOnClickListener {

            CartDataHolder.subTotal = subTotal
            CartDataHolder.taxAmount = taxAmount
            CartDataHolder.deliveryFee = deliveryFee
            CartDataHolder.totalAmount = totalAmount
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putExtra("TOTAL_AMOUNT", calculateTotalAmount())
            startActivity(intent)
            Toast.makeText(this, "Proceeding to checkout", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateTotalAmount(): Double {
        // Calculate the total amount from cart items
        var total = 0.0
        for (item in cartItems) {
            total += item.totalPrice
        }
        // Add delivery fee and tax if needed
        return total
    }

    private fun loadCartItems() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val cartRef = database.getReference("cart").child(user.uid)

            cartRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    cartItems.clear()



                    for (itemSnapshot in snapshot.children) {
                        val cartItem = itemSnapshot.getValue(CartItem::class.java)
                        cartItem?.let {
                            it.key = itemSnapshot.key
                            cartItems.add(it)
                            subTotal += it.totalPrice
                        }
                    }

                    updateCartUI(subTotal)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CartActivity, "Error loading cart: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } ?: run {
            showEmptyCart()
        }
    }

    private fun updateCartUI(subTotal: Double) {
        if (cartItems.isEmpty()) {
            showEmptyCart()
        } else {
            showCartWithItems()

            // Calculate totals
            deliveryFee = if (subTotal > 50) 0.0 else 5.0
           taxAmount = subTotal * 0.1 // 10% tax
            totalAmount=subTotal + deliveryFee + taxAmount

            tvSubTotal.text = "$${"%.2f".format(subTotal)}"
            tvDeliveryFee.text = "$${"%.2f".format(deliveryFee)}"
            tvTax.text = "$${"%.2f".format(taxAmount)}"
            tvTotal.text = "$${"%.2f".format(subTotal + deliveryFee + taxAmount)}"

            cartAdapter.notifyDataSetChanged()
        }
    }
    private fun clearFields(){
        tvSubTotal.text = "\$ 0.0"
        tvDeliveryFee.text = "\$ 0.0"
        tvTax.text = "\$ 0.0"
        tvTotal.text = "\$ 0.0"
    }

    private fun showEmptyCart() {
        rvCart.visibility = View.GONE
        llEmptyCart.visibility = View.VISIBLE
    }

    private fun showCartWithItems() {
        rvCart.visibility = View.VISIBLE
        llEmptyCart.visibility = View.GONE
    }

    private fun onItemRemoved(cartItem: CartItem) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            cartItem.key?.let { itemKey ->
                database.getReference("cart").child(user.uid).child(itemKey).removeValue()
                    .addOnSuccessListener {
                        clearFields()
                        Toast.makeText(this, "Item removed from cart", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to remove item: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun onQuantityChanged(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            onItemRemoved(cartItem)
            return
        }

        val currentUser = auth.currentUser
        currentUser?.let { user ->
            cartItem.key?.let { itemKey ->
                val updates = hashMapOf<String, Any>(
                    "quantity" to newQuantity,
                    "totalPrice" to (cartItem.price * newQuantity),
                    "timestamp" to System.currentTimeMillis()
                )

                database.getReference("cart").child(user.uid).child(itemKey).updateChildren(updates)
                    .addOnSuccessListener {
                        // Quantity updated successfully
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update quantity: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
object CartDataHolder {
    var subTotal: Double = 0.0
    var taxAmount: Double = 0.0
    var deliveryFee: Double = 0.0
    var totalAmount: Double = 0.0

    fun clear() {
        subTotal = 0.0
        taxAmount = 0.0
        deliveryFee = 0.0
        totalAmount = 0.0
    }
}
package com.example.androidproject1.product

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidproject1.R
import com.example.androidproject1.adapter.SizeAdapter
import com.example.androidproject1.cart.CartActivity
import com.example.androidproject1.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var btnBack: ImageView
    private lateinit var tvLabel: TextView
    private lateinit var imgProduct: ImageView
    private lateinit var tvProductTitle: TextView
    private lateinit var tvProductPrice: TextView
    private lateinit var tvProductDescription: TextView
    private lateinit var tvColorLabel: TextView
    private lateinit var colorOption1: ImageView
    private lateinit var tvSizeLabel: TextView
    private lateinit var rvSizes: RecyclerView
    private lateinit var tvQuantityLabel: TextView
    private lateinit var btnQuantityMinus: ImageView
    private lateinit var tvQuantity: TextView
    private lateinit var btnQuantityPlus: ImageView
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnAddToCart: Button

    private lateinit var sizeAdapter: SizeAdapter
    private var categoryName: String = ""
    private var productId: Int =0
    private var productName=""
    private var currentProduct: Product? = null
    private var selectedSize: String = ""
    private var quantity: Int = 1
    private var basePrice: Int = 0
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()

        initView()
        getIntentData()
        setupClickListeners()
        getProductData()
    }

    private fun getIntentData() {
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""
        productName=intent.getStringExtra("PRODUCT_NAME")?:""
        productId = intent.getIntExtra("PRODUCT_ID",-1) ?: 0
        Toast.makeText(this, "Product id is $productId",Toast.LENGTH_SHORT).show()
        tvLabel.text = productName
    }

    private fun initView() {
        btnBack = findViewById(R.id.btn_back)
        tvLabel = findViewById(R.id.textview_label)
        imgProduct = findViewById(R.id.img_product)
        tvProductTitle = findViewById(R.id.tv_product_title)
        tvProductPrice = findViewById(R.id.tv_product_price)
        tvProductDescription = findViewById(R.id.tv_product_description)
        tvColorLabel = findViewById(R.id.tv_color_label)
        colorOption1 = findViewById(R.id.color_option_1)
        tvSizeLabel = findViewById(R.id.tv_size_label)
        rvSizes = findViewById(R.id.rv_sizes)
        tvQuantityLabel = findViewById(R.id.tv_quantity_label)
        btnQuantityMinus = findViewById(R.id.btn_quantity_minus)
        tvQuantity = findViewById(R.id.tv_quantity)
        btnQuantityPlus = findViewById(R.id.btn_quantity_plus)
        tvTotalPrice = findViewById(R.id.tv_total_price)
        btnAddToCart = findViewById(R.id.btn_add_to_cart)

        setupSizeRecyclerView()
    }

    private fun setupSizeRecyclerView() {
        rvSizes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnQuantityMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateQuantityDisplay()
            }
        }

        btnQuantityPlus.setOnClickListener {
            quantity++
            updateQuantityDisplay()
        }



        btnAddToCart.setOnClickListener {
            addToCart()
        }
    }

    private fun updateQuantityDisplay() {
        tvQuantity.text = quantity.toString()
        val totalPrice = basePrice * quantity
        tvTotalPrice.text = "Total: $${String.format("%.2f", totalPrice.toDouble())}"
    }

    private fun addToCart(){
        if (selectedSize.isEmpty()) {
            Toast.makeText(this, "Please select a size before adding to cart", Toast.LENGTH_SHORT).show()

        }else{
            val currentUser = auth.currentUser
            currentProduct?.let { product ->
                val userId = currentUser?.uid?:""



                checkExistingCartItem(userId, product)
            }
        }

    }

    private fun checkExistingCartItem(userId: String, product: Product) {
        val database = FirebaseDatabase.getInstance()
        val cartRef = database.getReference("cart").child(userId)

        cartRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var itemExists = false
                var existingItemKey: String? = null
                var existingQuantity = 0

                for (itemSnapshot in snapshot.children) {
                    val existingProductId = itemSnapshot.child("productId").getValue(Int::class.java)
                    val existingSize = itemSnapshot.child("size").getValue(String::class.java)

                    if (existingProductId == productId && existingSize == selectedSize) {
                        itemExists = true
                        existingItemKey = itemSnapshot.key
                        existingQuantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                        break
                    }
                }

                if (itemExists && existingItemKey != null) {
                    // Update existing item quantity
                    updateExistingCartItem(userId, existingItemKey, existingQuantity + quantity, product)
                } else {
                    // Add new item to cart
                    addNewCartItem(userId, product)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProductDetailActivity, "Error checking cart: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateExistingCartItem(userId: String, itemKey: String, newQuantity: Int, product: Product) {
        val database = FirebaseDatabase.getInstance()
        val itemRef = database.getReference("cart").child(userId).child(itemKey)

        val updates = hashMapOf<String, Any>(
            "quantity" to newQuantity,
            "totalPrice" to (product.price * newQuantity),
            "timestamp" to System.currentTimeMillis()
        )

        itemRef.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Cart updated successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@ProductDetailActivity, CartActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Failed to update cart: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addNewCartItem(userId: String, product: Product) {
        val database = FirebaseDatabase.getInstance()
        val cartRef = database.getReference("cart").child(userId).push()

        val cartItem = hashMapOf(
            "productId" to productId,
            "productName" to product.name,
            "categoryName" to categoryName,
            "size" to selectedSize,
            "quantity" to quantity,
            "price" to product.price,
            "totalPrice" to (product.price * quantity),
            "imageUrl" to product.image,
            "color" to product.color,
            "timestamp" to System.currentTimeMillis()
        )

        cartRef.setValue(cartItem)
            .addOnSuccessListener {
                Toast.makeText(this, "Added to cart successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@ProductDetailActivity, CartActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Failed to add product to cart: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getProductData() {
        val database = FirebaseDatabase.getInstance()
        val productRef = database.getReference("categories")
            .child(categoryName)
            .child("products")
            .child(productId.toString())

        productRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val product = snapshot.getValue(Product::class.java)
                    if (product != null) {
                        currentProduct = product
                        displayProductData(product)
                    } else {
                        Toast.makeText(this@ProductDetailActivity, "Product not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ProductDetailActivity, "Product not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProductDetailActivity, "Error loading product: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayProductData(product: Product) {
        tvProductTitle.text = product.name
        tvProductPrice.text =  "$${String.format("%.2f", product.price.toDouble())}"
        tvProductDescription.text = product.description

        basePrice = product.price
        updateQuantityDisplay()

        loadProductImage(product.image)
        setColorIndicator(product.color)
        setupSizeOptions(product.sizes)
    }

    private fun setupSizeOptions(sizes: List<String>) {
        if (sizes.isNotEmpty()) {
            sizeAdapter = SizeAdapter(sizes){ size ->
                selectedSize = size
                Toast.makeText(this, "Selected size: $size", Toast.LENGTH_SHORT).show()
            }
            rvSizes.adapter = sizeAdapter
        }
    }

    private fun setColorIndicator(colorName: String) {
        val colorInt = getColorFromName(colorName)
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(colorInt)
        drawable.setStroke(4, Color.parseColor("#E0E0E0"))

        colorOption1.background = drawable
    }

    private fun getColorFromName(colorName: String): Int {
        return when (colorName.lowercase()) {
            "black" -> Color.BLACK
            "white" -> Color.WHITE
            "red" -> Color.RED
            "blue" -> Color.BLUE
            "green" -> Color.GREEN
            "yellow" -> Color.YELLOW
            "purple" -> Color.parseColor("#800080")
            "pink" -> Color.parseColor("#FFC0CB")
            "orange" -> Color.parseColor("#FFA500")
            "brown" -> Color.parseColor("#A52A2A")
            "gray", "grey" -> Color.GRAY
            "navy" -> Color.parseColor("#000080")
            "maroon" -> Color.parseColor("#800000")
            "olive" -> Color.parseColor("#808000")
            "lime" -> Color.parseColor("#00FF00")
            "aqua", "cyan" -> Color.CYAN
            "teal" -> Color.parseColor("#008080")
            "silver" -> Color.parseColor("#C0C0C0")
            "gold" -> Color.parseColor("#FFD700")
            "beige" -> Color.parseColor("#F5F5DC")
            "tan" -> Color.parseColor("#D2B48C")
            "khaki" -> Color.parseColor("#F0E68C")
            "ivory" -> Color.parseColor("#FFFFF0")
            "espresso" -> Color.parseColor("#3C2415")
            else -> Color.LTGRAY
        }
    }

    private fun loadProductImage(imageUrl: String) {
        if (imageUrl.isNotEmpty()) {
            try {
                val storageRef: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                Glide.with(this)
                    .load(storageRef)
                    .placeholder(R.drawable.dewss)
                    .error(R.drawable.dewss)
                    .into(imgProduct)
            } catch (e: Exception) {

                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.dewss)
                    .error(R.drawable.dewss)
                    .into(imgProduct)
            }
        }
    }


}
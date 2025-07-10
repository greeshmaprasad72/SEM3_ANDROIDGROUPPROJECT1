package com.example.androidproject1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject1.adapter.CategoryAdapter
import com.example.androidproject1.cart.CartActivity
import com.example.androidproject1.model.Category
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class HomeActivity : AppCompatActivity() {
    private var adapter:CategoryAdapter?=null
    private lateinit var recyclerView:RecyclerView
    private lateinit var database:FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private var cartListener: ValueEventListener? = null
    private lateinit var flCartContainer: FrameLayout
    private lateinit var tvCartCount: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        database=Firebase.database
        flCartContainer=findViewById(R.id.fl_cart_container)
        tvCartCount=findViewById(R.id.tv_cart_badge)
        flCartContainer.setOnClickListener {
            val intent=Intent(this,CartActivity::class.java)
            startActivity(intent)
        }
        setupCartCount()

    }

    private fun initializeRecyclerView(){
        try{
            recyclerView=findViewById(R.id.categories_recycler_view)
            val query = database.reference.child("categories")

            val options = FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(query, Category::class.java)
                .build()

            adapter=CategoryAdapter(options)

            recyclerView.layoutManager=LinearLayoutManager(this)
            recyclerView.adapter=adapter
        }catch (e:Exception){
            Log.i("TAG", "initializeRecyclerView: $e")
        }

    }

    private fun setupCartCount() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val cartRef = FirebaseDatabase.getInstance()
                .getReference("cart")
                .child(userId)

            cartListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Count number of unique products in cart
                    val productCount = snapshot.childrenCount.toInt()

                    // Update cart count display
                    updateCartCountDisplay(productCount)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TAG", "Failed to read cart data: ${error.message}")
                }
            }

            cartRef.addValueEventListener(cartListener!!)
        }
    }

    private fun updateCartCountDisplay(count: Int) {
        if (count > 0) {
            tvCartCount.text = count.toString()
            tvCartCount.visibility = View.VISIBLE
        } else {
            tvCartCount.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        initializeRecyclerView()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
}
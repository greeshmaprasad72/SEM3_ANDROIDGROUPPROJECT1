package com.example.androidproject1.product

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject1.R
import com.example.androidproject1.adapter.ProductAdapter
import com.example.androidproject1.adapter.CategoryAdapter
import com.example.androidproject1.adapter.CategoryDrawerAdapter
import com.example.androidproject1.cart.CartActivity
import com.example.androidproject1.model.Product
import com.example.androidproject1.model.Category
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryRecyclerView: RecyclerView
    private var productAdapter: ProductAdapter? = null
    private var categoryAdapter: CategoryDrawerAdapter? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var categoryText: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var hamburgerIcon: ImageView
    private lateinit var toolbarTitle: TextView
    private lateinit var tvCartCount:TextView
    private lateinit var flCartContainer: FrameLayout

    private var currentCategoryName: String = ""
    private lateinit var auth: FirebaseAuth
    private var cartListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        initializeViews()
        setupDrawer()
        setupCategoryRecyclerView()
        setupCartCount()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.rv_product)
        categoryRecyclerView = findViewById(R.id.recyclerview_category)
        categoryText = findViewById(R.id.tv_category)
        progressBar = findViewById(R.id.progress_bar)
        drawerLayout = findViewById(R.id.drawer_layout)
        hamburgerIcon = findViewById(R.id.im_arrow_back)
        toolbarTitle = findViewById(R.id.textview_label)
        flCartContainer=findViewById(R.id.fl_cart_container)
        tvCartCount=findViewById(R.id.tv_cart_badge)

        flCartContainer.setOnClickListener{
            val intent=Intent(this,CartActivity::class.java)
            startActivity(intent)
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

    private fun setupDrawer() {
        hamburgerIcon.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun setupCategoryRecyclerView() {
        try {
            val query = FirebaseDatabase.getInstance()
                .getReference("categories")

            val options = FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(query, Category::class.java)
                .build()

            categoryAdapter = CategoryDrawerAdapter(options) { selectedCategory ->
                // Handle category selection
                currentCategoryName = selectedCategory.name
                toolbarTitle.text = selectedCategory.name
                categoryText.text = selectedCategory.name

                // Update product list based on selected category
                initializeProductRecyclerView(selectedCategory.name)

                // Close drawer after selection
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            categoryRecyclerView.layoutManager = LinearLayoutManager(this)
            categoryRecyclerView.adapter = categoryAdapter

        } catch (e: Exception) {
            Log.d("TAG", "setupCategoryRecyclerView: $e")
        }
    }

    private fun initializeProductRecyclerView(categoryName: String) {
        try {
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            // Stop previous adapter if exists
            productAdapter?.stopListening()

            val query = FirebaseDatabase.getInstance()
                .getReference("categories")
                .child(categoryName)
                .child("products")

            val options = FirebaseRecyclerOptions.Builder<Product>()
                .setQuery(query, Product::class.java)
                .build()

            productAdapter = ProductAdapter(options, categoryName)
            recyclerView.layoutManager = GridLayoutManager(this, 2)
            recyclerView.adapter = productAdapter

            // Start listening for new adapter
            productAdapter?.startListening()

            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

        } catch (e: Exception) {
            Log.d("TAG", "initializeProductRecyclerView: $e")
        }
    }

    override fun onStart() {
        super.onStart()
        currentCategoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Activewear"

        if (currentCategoryName.isNotEmpty()) {
            toolbarTitle.text = currentCategoryName
            categoryText.text = currentCategoryName
            initializeProductRecyclerView(currentCategoryName)
        }

        categoryAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
//        productAdapter?.stopListening()
//        categoryAdapter?.stopListening()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
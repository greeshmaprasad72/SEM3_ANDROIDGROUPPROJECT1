package com.example.androidproject1.product

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject1.R
import com.example.androidproject1.adapter.ProductAdapter
import com.example.androidproject1.model.Product
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase

class ProductActivity : AppCompatActivity() {
    private lateinit var recyclerView:RecyclerView
    private var adapter:ProductAdapter?=null
    private lateinit var  progressBar:ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val categoryName=intent.getStringExtra("CATEGORY_NAME") ?:""
        recyclerView=findViewById(R.id.rv_product)
        val categoryText:TextView=findViewById(R.id.tv_category)
         progressBar = findViewById(R.id.progress_bar)
        categoryText.text=categoryName
        initializeRecyclerView(categoryName)


    }

    private fun initializeRecyclerView(categoryName:String) {
        try{
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            val query = FirebaseDatabase.getInstance()
                .getReference("categories")
                .child(categoryName)
                .child("products")

            val options = FirebaseRecyclerOptions.Builder<Product>()
                .setQuery(query, Product::class.java)
                .build()
            adapter = ProductAdapter(options,categoryName)

            recyclerView.layoutManager = GridLayoutManager(this,2)
            recyclerView.adapter = adapter

            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }catch (e:Exception){
            Log.d("TAG", "initializeRecyclerView: $e")
        }

    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
}
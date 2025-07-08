package com.example.androidproject1

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidproject1.adapter.CategoryAdapter
import com.example.androidproject1.model.Category
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class HomeActivity : AppCompatActivity() {
    private var adapter:CategoryAdapter?=null
    private lateinit var recyclerView:RecyclerView
    private lateinit var database:FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        database=Firebase.database

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
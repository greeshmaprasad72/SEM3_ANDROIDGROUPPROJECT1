package com.example.androidproject1.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidproject1.R
import com.example.androidproject1.model.Category
import com.example.androidproject1.product.ProductActivity
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CategoryAdapter(options: FirebaseRecyclerOptions<Category>) :FirebaseRecyclerAdapter<Category,CategoryAdapter.CategoryViewHolder>(options) {
    class CategoryViewHolder(inflator:LayoutInflater,parent:ViewGroup) :
        RecyclerView.ViewHolder(inflator.inflate(R.layout.category_item,parent,false)) {
        val categoryName: TextView= itemView.findViewById(R.id.category_title)
        val categoryImage: ImageView = itemView.findViewById(R.id.category_image)
        val clCategory:ConstraintLayout=itemView.findViewById(R.id.cl_category_card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflator=LayoutInflater.from(parent.context)
        return CategoryViewHolder(inflator,parent)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int, model: Category) {
        holder.categoryName.text=model.name
        val storRef: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(model.image)


        Glide.with(holder.categoryImage.context).load(storRef).into(holder.categoryImage)
        holder.clCategory.setOnClickListener {
            val intent =Intent(holder.itemView.context,ProductActivity::class.java)
            intent.putExtra("CATEGORY_NAME",model.name)
            holder.itemView.context.startActivity(intent)
        }




    }
}
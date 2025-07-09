package com.example.androidproject1.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.androidproject1.R
import com.example.androidproject1.model.Category
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CategoryDrawerAdapter(
    options: FirebaseRecyclerOptions<Category>,
    private val onCategoryClick: (Category) -> Unit
) : FirebaseRecyclerAdapter<Category, CategoryDrawerAdapter.CategoryViewHolder>(options) {

    class CategoryViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.drawer_category_item, parent, false)) {

        val categoryName: TextView = itemView.findViewById(R.id.tv_category_name)
        val imCatImage:ImageView= itemView.findViewById(R.id.im_category_iamge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CategoryViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int, model: Category) {
        holder.categoryName.text = model.name
        val storageRef: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(model.image)
        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                Log.d("TAG", "Got download URL: $uri")

                Glide.with(holder.imCatImage.context)
                    .load(uri.toString())
                    .placeholder(R.drawable.dewss)
                    .error(R.drawable.dewss)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(holder.imCatImage)
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Failed to get download URL: ${exception.message}")

                // Method 2: Try loading directly with StorageReference
//                loadWithStorageReference(imageUrl, imageView)
            }
        holder.itemView.setOnClickListener {
            onCategoryClick(model)
        }
    }
}
package com.example.androidproject1.adapter

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.androidproject1.R
import com.example.androidproject1.model.Product
import com.example.androidproject1.product.ProductDetailActivity
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProductAdapter(options: FirebaseRecyclerOptions<Product>,private val categoryName:String) :
    FirebaseRecyclerAdapter<Product, ProductAdapter.ProductViewHolder>(options) {


    class ProductViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.row_prodduct_item, parent, false)) {

        val productName: TextView = itemView.findViewById(R.id.tv_product_1_name)
        val productPrice: TextView = itemView.findViewById(R.id.tv_product_1_price)
        val productImage: ImageView = itemView.findViewById(R.id.iv_product_1)
        val colorIndicator: ImageView = itemView.findViewById(R.id.iv_color_indicator)
        val cartButton: ImageView = itemView.findViewById(R.id.imCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ProductViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int, model: Product) {
        Log.d("TAG", "onBindViewHolder: "+model.name)
        holder.productName.text = model.name
        holder.productPrice.text = "$${model.price}"


        setColorIndicator(holder.colorIndicator, model.color)



            val storageRef: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(model.image)
        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                Log.d("TAG", "Got download URL: $uri")

                Glide.with(holder.productImage.context)
                    .load(uri.toString())
                    .placeholder(R.drawable.dewss)
                    .error(R.drawable.dewss)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(holder.productImage)
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Failed to get download URL: ${exception.message}")

                // Method 2: Try loading directly with StorageReference
//                loadWithStorageReference(imageUrl, imageView)
            }






        holder.itemView.setOnClickListener {
            val intent=Intent(holder.itemView.context,ProductDetailActivity::class.java)
           intent. putExtra("CATEGORY_NAME", categoryName )
            intent.putExtra("PRODUCT_NAME",model.name)
           intent. putExtra("PRODUCT_ID", model.id)
            holder.itemView.context.startActivity(intent)

        }
    }

    private fun setColorIndicator(imageView: ImageView, colorName: String) {
        val colorInt = getColorFromName(colorName)
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(colorInt)

        imageView.background = drawable
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
}
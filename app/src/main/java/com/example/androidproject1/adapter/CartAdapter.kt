package com.example.androidproject1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidproject1.R
import com.example.androidproject1.model.CartItem
import com.google.firebase.storage.FirebaseStorage

class CartAdapter(
    private val cartItems: List<CartItem>,
    private val onItemRemoved: (CartItem) -> Unit,
    private val onQuantityChanged: (CartItem, Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.iv_product_image)
        val productName: TextView = itemView.findViewById(R.id.tv_product_name)
        val productSize: TextView = itemView.findViewById(R.id.tv_product_size)
        val productPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        val productQuantity: TextView = itemView.findViewById(R.id.tv_product_quantity)
        val btnRemove: ImageView = itemView.findViewById(R.id.iv_remove_item)
        val btnDecrease: ImageView = itemView.findViewById(R.id.iv_decrease_quantity)
        val btnIncrease: ImageView = itemView.findViewById(R.id.iv_increase_quantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]

        holder.productName.text = cartItem.productName
        holder.productSize.text = "Size: ${cartItem.size}"
        holder.productPrice.text = "$${"%.2f".format(cartItem.totalPrice.toDouble())}"
        holder.productQuantity.text = cartItem.quantity.toString()

        // Load product image
        try {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(cartItem.imageUrl)
            Glide.with(holder.itemView.context)
                .load(storageRef)
                .placeholder(R.drawable.dewss)
                .error(R.drawable.dewss)
                .into(holder.productImage)
        } catch (e: Exception) {
            Glide.with(holder.itemView.context)
                .load(cartItem.imageUrl)
                .placeholder(R.drawable.dewss)
                .error(R.drawable.dewss)
                .into(holder.productImage)
        }

        holder.btnRemove.setOnClickListener {
            onItemRemoved(cartItem)
        }

        holder.btnDecrease.setOnClickListener {
            val newQuantity = cartItem.quantity - 1
            onQuantityChanged(cartItem, newQuantity)
        }

        holder.btnIncrease.setOnClickListener {
            val newQuantity = cartItem.quantity + 1
            onQuantityChanged(cartItem, newQuantity)
        }
    }

    override fun getItemCount(): Int = cartItems.size
}
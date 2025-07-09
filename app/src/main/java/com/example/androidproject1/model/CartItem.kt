package com.example.androidproject1.model

data class CartItem(
    val productId: Int = 0,
    val productName: String = "",
    val categoryName: String = "",
    val size: String = "",
    var quantity: Int = 1,
    val price: Int = 0,
    var totalPrice: Int = 0,
    val imageUrl: String = "",
    val color: String = "",
    val timestamp: Long = 0L,
    var key: String? = null
)
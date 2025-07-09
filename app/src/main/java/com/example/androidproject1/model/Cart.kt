package com.example.androidproject1.model



data class Cart(
    var productId: String? = null,
    var productName: String? = null,
    var categoryName: String? = null,
    var price: Double = 0.0,
    var quantity: Int = 0,
    var totalPrice: Double = 0.0,
    var imageUrl: String? = null,
    var color: String? = null,
    var size: String? = null,
    var timestamp: Long = 0
) {

    constructor() : this(null, null, null, 0.0, 0, 0.0, null, null, null, 0)
}

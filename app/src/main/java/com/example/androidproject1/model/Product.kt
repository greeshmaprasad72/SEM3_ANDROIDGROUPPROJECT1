package com.example.androidproject1.model

data class Product(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val price: Int = 0,
    val color: String = "",
    val material: String = "",
    val image: String = "",
    val sizes: List<String> = emptyList()
)

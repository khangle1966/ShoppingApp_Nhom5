package com.nhom5.shoppingapp.model

data class Product(
    val name: String = "",
    val price: String = "",
    val imageUrl: String = "",
    val rating: Float = 0f,
    val actualPrice: String = "",
    val offerPercentage: String = "",
)

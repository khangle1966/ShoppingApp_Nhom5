package com.nhom5.shoppingapp.model

data class CartItem(
    val id: String = "",  // Thêm thuộc tính id
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)

package com.nhom5.shoppingapp.model

data class CartItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val imageUrl: String = "",
    val size: List<String> = emptyList(),  // Thêm thuộc tính size
    val color: List<String> = emptyList()  // Thêm thuộc tính color
)

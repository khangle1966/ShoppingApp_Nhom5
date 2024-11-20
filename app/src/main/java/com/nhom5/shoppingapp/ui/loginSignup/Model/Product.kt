package com.nhom5.shoppingapp.model

data class Product(
    val productId: String = "",     // Thêm thuộc tính productId
    val name: String = "",
    val price: Double = 0.0,
    val actualPrice: Double = 0.0,
    val imageUrl: List<String> = emptyList(),
    val rating: Float = 0f,
    val offerPercentage: String = "",
    val specifications: String = "", // Thông số sản phẩm
    // Thêm thuộc tính size và color
    val size: List<String> = emptyList(),   // Danh sách các size của sản phẩm
    val color: List<String> = emptyList(),   // Danh sách các màu của sản phẩm
    val status: String = "Available" // Trạng thái mặc định là "Available"

)

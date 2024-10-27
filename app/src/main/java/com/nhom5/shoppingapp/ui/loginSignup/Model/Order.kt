package com.nhom5.shoppingapp.model

data class Order(
    val orderId: String = "",
    val userId: String = "",
    var items: List<CartItem> = emptyList(),
    var totalPrice: Double = 0.0,
    var shippingAddress: String = "",
    val paymentMethod: String = "",
    var status: String = "Pending" ,
    val orderDate: String = "" // Có thể sử dụng SimpleDateFormat để định dạng ngày

)
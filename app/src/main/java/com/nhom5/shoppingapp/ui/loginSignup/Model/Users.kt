package com.nhom5.shoppingapp.model

data class User(
    var uid: String = "",
    var mobile: String = "",
    var address: String = "",
    var name: String = "",
    var email: String = "",
    var userType: String = "customer",
    var addresses: MutableList<Address> = mutableListOf()
)

package com.nhom5.shoppingapp.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.model.Product

class ProductRepository(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()

    // Lấy sản phẩm từ Firestore dựa trên productId
    fun getProductById(productId: String, callback: (Product?) -> Unit) {
        firestore.collection("products").document(productId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val product = document.toObject(Product::class.java)?.copy(productId = document.id)
                    callback(product)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                callback(null)
            }
    }
}

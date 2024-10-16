package com.nhom5.shoppingapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nhom5.shoppingapp.model.Product
import com.nhom5.shoppingapp.repository.ProductRepository

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProductRepository = ProductRepository(application)
    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> = _product

    fun getProductById(productId: String) {
        // Lấy dữ liệu sản phẩm từ repository
        repository.getProductById(productId) { product ->
            _product.value = product
        }
    }
}

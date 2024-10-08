package com.nhom5.shoppingapp.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.R
import com.nhom5.shoppingapp.databinding.FragmentHomeBinding
import com.nhom5.shoppingapp.model.Product
import com.nhom5.shoppingapp.ui.adapters.ProductAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import android.widget.FrameLayout // Import FrameLayout để truy cập loaderLayout

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var loaderLayout: FrameLayout // Thêm biến cho loaderLayout
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo binding
        binding = FragmentHomeBinding.bind(view)

        // Tìm kiếm loaderLayout từ view
        loaderLayout = view.findViewById(R.id.loader_layout) // Truy cập loader_layout trực tiếp

        // Thiết lập RecyclerView cho sản phẩm
        productAdapter = ProductAdapter(productList)
        binding.productsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }

        // Set up FloatingActionButton để thêm sản phẩm
        val fab: FloatingActionButton = binding.homeFabAddProduct
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_goto_addProduct)
        }

        // Hiển thị loader khi bắt đầu lấy dữ liệu
        showLoader()

        // Lấy dữ liệu từ Firestore
        loadProducts()
    }

    private fun loadProducts() {
        firestore.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                productList.clear()
                for (document in documents) {
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
                }
                productAdapter.notifyDataSetChanged()
                hideLoader() // Ẩn loader khi tải dữ liệu xong
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Lỗi khi tải sản phẩm: ${exception.message}", Toast.LENGTH_SHORT).show()
                hideLoader() // Ẩn loader nếu có lỗi
            }
    }

    private fun hideLoader() {
        // Sử dụng biến loaderLayout để thay đổi visibility
        loaderLayout.visibility = View.GONE
    }

    private fun showLoader() {
        loaderLayout.visibility = View.VISIBLE
    }
}

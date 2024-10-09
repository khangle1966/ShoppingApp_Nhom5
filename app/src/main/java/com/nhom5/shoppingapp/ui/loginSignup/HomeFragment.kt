package com.nhom5.shoppingapp.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.R
import com.nhom5.shoppingapp.databinding.FragmentHomeBinding
import com.nhom5.shoppingapp.model.Product
import com.nhom5.shoppingapp.ui.adapters.ProductAdapter
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var loaderLayout: FrameLayout // Loader layout
    private lateinit var productAdapter: ProductAdapter
    private val fullProductList = mutableListOf<Product>() // Danh sách sản phẩm đầy đủ
    private val filteredProductList = mutableListOf<Product>() // Danh sách sản phẩm đã được lọc
    private val firestore = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo binding
        binding = FragmentHomeBinding.bind(view)

        // Tìm kiếm loaderLayout từ view
        loaderLayout = view.findViewById(R.id.loader_layout) // Truy cập loader_layout trực tiếp

        // Thiết lập RecyclerView cho sản phẩm
        productAdapter = ProductAdapter(filteredProductList)
        binding.productsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }

        // Set up FloatingActionButton để thêm sản phẩm
        val fab: FloatingActionButton = binding.homeFabAddProduct
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addProductFragment)
        }

        // Thêm chức năng tìm kiếm
        val searchEditText: EditText = view.findViewById(R.id.home_search_edit_text)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Hiển thị loader khi bắt đầu lấy dữ liệu
        showLoader()

        // Lấy dữ liệu từ Firestore
        loadProducts()
    }

    private fun loadProducts() {
        firestore.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                fullProductList.clear()
                filteredProductList.clear() // Reset danh sách lọc

                for (document in documents) {
                    val product = document.toObject(Product::class.java)
                    fullProductList.add(product)
                    filteredProductList.add(product) // Ban đầu hiển thị tất cả sản phẩm
                }

                productAdapter.notifyDataSetChanged()
                hideLoader() // Ẩn loader khi tải dữ liệu xong
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Lỗi khi tải sản phẩm: ${exception.message}", Toast.LENGTH_SHORT).show()
                hideLoader() // Ẩn loader nếu có lỗi
            }
    }

    private fun filterProducts(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullProductList // Nếu không có từ khóa tìm kiếm, hiển thị toàn bộ sản phẩm
        } else {
            fullProductList.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        filteredProductList.clear()
        filteredProductList.addAll(filteredList)
        productAdapter.notifyDataSetChanged() // Cập nhật RecyclerView
    }

    private fun hideLoader() {
        loaderLayout.visibility = View.GONE
    }

    private fun showLoader() {
        loaderLayout.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()

        // Kiểm tra trạng thái đăng nhập
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // Điều hướng đến màn hình đăng nhập nếu chưa đăng nhập
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }
}

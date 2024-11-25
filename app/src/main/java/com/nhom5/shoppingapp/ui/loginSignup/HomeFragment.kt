package com.nhom5.shoppingapp.ui.home


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton

import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.R
import com.nhom5.shoppingapp.databinding.FragmentHomeBinding
import com.nhom5.shoppingapp.model.Product
import com.nhom5.shoppingapp.model.User
import com.nhom5.shoppingapp.ui.adapters.ProductAdapter

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var loaderLayout: FrameLayout
    private lateinit var productAdapter: ProductAdapter
    private val fullProductList = mutableListOf<Product>()
    private val filteredProductList = mutableListOf<Product>()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo binding
        binding = FragmentHomeBinding.bind(view)

        // Tìm loaderLayout từ view
        loaderLayout = view.findViewById(R.id.loader_layout)

        // Thiết lập RecyclerView
        productAdapter = ProductAdapter(filteredProductList) { product ->
            val action = HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(product.productId)
            findNavController().navigate(action)
        }

        binding.productsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }
        // Ẩn hoặc hiển thị nút thêm sản phẩm dựa vào userType
        checkUserType()
        // FloatingActionButton để thêm sản phẩm
        val fab: FloatingActionButton = binding.homeFabAddProduct
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addProductFragment)
        }

        // Xử lý tìm kiếm sản phẩm
        val searchEditText: EditText = view.findViewById(R.id.home_search_edit_text)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Xử lý sự kiện khi nhấn nút Sort
        val sortButton: ImageButton = view.findViewById(R.id.sortButton)
        sortButton.setOnClickListener { view ->
            showSortMenu(view)
        }


        // Hiển thị loader và tải dữ liệu
        showLoader()
        loadProducts()
    }

    // Kiểm tra userType và ẩn FAB nếu người dùng là "customer"
    private fun checkUserType() {
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    if (user?.userType == "admin") {
                        // Ẩn FloatingActionButton nếu userType là customer
                        binding.homeFabAddProduct.visibility = View.VISIBLE

                    } else {
                        // Hiển thị FloatingActionButton nếu userType không phải là customer
                        binding.homeFabAddProduct.visibility = View.GONE
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Lỗi khi tải thông tin người dùng: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun showSortMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.sort_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.sortByNameAscending -> sortByNameAscending()
                R.id.sortByNameDescending -> sortByNameDescending()
                R.id.sortByPriceAscending -> sortByPriceAscending()
                R.id.sortByPriceDescending -> sortByPriceDescending()
            }
            true
        }
        popupMenu.show()
    }

    private fun loadProducts() {
        firestore.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                fullProductList.clear() // Dùng cho tìm kiếm và lọc
                filteredProductList.clear() // Dùng cho hiển thị ban đầu

                for (document in documents) {
                    try {
                        // Lấy product và gán documentId vào productId
                        val product = document.toObject(Product::class.java).copy(productId = document.id)

                        // Thêm tất cả sản phẩm vào fullProductList và filteredProductList
                        fullProductList.add(product)
                        filteredProductList.add(product)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                productAdapter.notifyDataSetChanged()
                hideLoader()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Lỗi khi tải sản phẩm: ${exception.message}", Toast.LENGTH_SHORT).show()
                hideLoader()
            }
    }




    private fun filterProducts(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullProductList
        } else {
            fullProductList.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        filteredProductList.clear()
        filteredProductList.addAll(filteredList)
        productAdapter.notifyDataSetChanged()
    }


    private fun sortByNameAscending() {
        filteredProductList.sortWith(compareBy { it.name.trim().lowercase() })
        productAdapter.notifyDataSetChanged()
        binding.productsRecyclerView.scrollToPosition(0)
    }

    private fun sortByNameDescending() {
        filteredProductList.sortWith(compareByDescending { it.name.trim().lowercase() })
        productAdapter.notifyDataSetChanged()
        binding.productsRecyclerView.scrollToPosition(0)
    }



    private fun sortByPriceAscending() {
        filteredProductList.sortBy { it.price }
        productAdapter.notifyDataSetChanged()
        binding.productsRecyclerView.scrollToPosition(0)
    }

    private fun sortByPriceDescending() {
        filteredProductList.sortByDescending { it.price }
        productAdapter.notifyDataSetChanged()
        binding.productsRecyclerView.scrollToPosition(0)
    }

    private fun hideLoader() {
        loaderLayout.visibility = View.GONE
    }

    private fun showLoader() {
        loaderLayout.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }
}

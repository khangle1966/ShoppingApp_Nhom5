package com.nhom5.shoppingapp.ui.admin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore


import com.nhom5.shoppingapp.R
import com.nhom5.shoppingapp.databinding.FragmentAdminBinding
import com.nhom5.shoppingapp.model.Product
import com.nhom5.shoppingapp.ui.adapters.AdminProductAdapter
import androidx.navigation.fragment.findNavController
import androidx.appcompat.app.AlertDialog
import android.widget.Toast

class AdminFragment : Fragment(R.layout.fragment_admin) {

    private lateinit var binding: FragmentAdminBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val productList = mutableListOf<Product>()
    private lateinit var adapter: AdminProductAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdminBinding.bind(view)

        binding.adminTopAppBar.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        // Setup RecyclerView
        setupRecyclerView()

        // Load Products
        loadProducts()
        binding.revenueStatisticsCard.setOnClickListener {
            // Navigate to the RevenueStatsFragment
            val action = AdminFragmentDirections.actionAdminFragmentToRevenueStatsFragment()
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminProductAdapter(productList, ::onEditProduct, ::onDeleteProduct)
        binding.adminProductsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.adminProductsRecyclerView.adapter = adapter
    }

    private fun loadProducts() {
        firestore.collection("products")
            .get()
            .addOnSuccessListener { result ->
                productList.clear()
                for (document in result) {
                    val product = document.toObject(Product::class.java).copy(productId = document.id)
                    productList.add(product)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->

            }
    }

    private fun onEditProduct(product: Product) {
        val action = AdminFragmentDirections.actionAdminFragmentToEditProductFragment(product.productId)
        findNavController().navigate(action)
    }

    private fun onDeleteProduct(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Change Product Status")
            .setMessage("Choose an action to change the product status:")
            .setPositiveButton("DISABLE") { dialog, _ ->
                // Cập nhật trạng thái của sản phẩm thành "Disabled"
                updateProductStatus(product, "Disabled")
                dialog.dismiss()
            }
            .setNeutralButton("AVAILABLE") { dialog, _ ->
                // Cập nhật trạng thái của sản phẩm thành "Available"
                updateProductStatus(product, "Available")
                dialog.dismiss()
            }
            .setNegativeButton("CANCEL") { dialog, _ ->
                // Đóng dialog mà không thực hiện thay đổi
                dialog.dismiss()
            }
            .show()
    }

    private fun updateProductStatus(product: Product, newStatus: String) {
        firestore.collection("products").document(product.productId)
            .update("status", newStatus)
            .addOnSuccessListener {
                // Cập nhật sản phẩm trong danh sách hiển thị
                val index = productList.indexOfFirst { it.productId == product.productId }
                if (index != -1) {
                    productList[index] = product.copy(status = newStatus)
                    adapter.notifyItemChanged(index)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update product status.", Toast.LENGTH_SHORT).show()
            }
    }


}

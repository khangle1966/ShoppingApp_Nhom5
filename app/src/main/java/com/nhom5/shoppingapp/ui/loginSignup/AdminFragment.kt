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
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product?")
            .setPositiveButton("DELETE") { dialog, _ ->
                firestore.collection("products").document(product.productId)
                    .delete()
                    .addOnSuccessListener {
                        // Update the UI after deletion
                        productList.remove(product)
                        adapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        // Handle failure
                    }
                dialog.dismiss()
            }
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}

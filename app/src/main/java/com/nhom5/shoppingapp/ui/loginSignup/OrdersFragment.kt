package com.nhom5.shoppingapp.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.databinding.FragmentOrdersBinding
import com.nhom5.shoppingapp.model.Order
import androidx.navigation.fragment.findNavController
import com.nhom5.shoppingapp.R
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ImageButton
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog

class OrdersFragment : Fragment() {

    private lateinit var binding: FragmentOrdersBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var ordersAdapter: OrdersAdapter
    private var ordersList = mutableListOf<Order>()
    private var filteredOrdersList = mutableListOf<Order>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.orderAllOrdersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        ordersAdapter = OrdersAdapter(filteredOrdersList) { orderId ->
            val action = OrdersFragmentDirections.actionOrdersFragmentToOrderDetailsFragment(orderId)
            findNavController().navigate(action)
        }
        binding.orderAllOrdersRecyclerView.adapter = ordersAdapter

        if (currentUser != null) {
            loadOrders(currentUser.uid)
        } else {
            showError("Người dùng không hợp lệ.")
        }

        setupSearchEditText()
        setupSortButton()
    }

    private fun loadOrders(userId: String) {
        showLoader()

        val ordersRef = firestore.collection("orders").whereEqualTo("userId", userId)

        ordersRef.get()
            .addOnSuccessListener { querySnapshot ->
                ordersList = querySnapshot.documents.map { document ->
                    document.toObject(Order::class.java)!!
                }.toMutableList()
                filteredOrdersList = ordersList.toMutableList()
                updateOrdersVisibility()
                hideLoader()
            }
            .addOnFailureListener { e ->
                hideLoader()
                showError("Có lỗi xảy ra khi tải đơn hàng: ${e.message}")
            }
    }

    private fun updateOrdersVisibility() {
        if (filteredOrdersList.isNotEmpty()) {
            binding.ordersEmptyTextView.visibility = View.GONE
            ordersAdapter.updateOrders(filteredOrdersList)
        } else {
            binding.ordersEmptyTextView.visibility = View.VISIBLE
        }
    }

    private fun setupSearchEditText() {
        val searchEditText = binding.root.findViewById<TextInputEditText>(R.id.home_search_edit_text)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterOrders(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterOrders(query: String?) {
        filteredOrdersList = if (query.isNullOrEmpty()) {
            ordersList
        } else {
            ordersList.filter { it.orderId.contains(query, ignoreCase = true) }.toMutableList()
        }
        updateOrdersVisibility()
    }

    private fun setupSortButton() {
        val sortButton = binding.root.findViewById<ImageButton>(R.id.sortButton)
        sortButton.setOnClickListener {
            showSortDialog()
        }
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf("Ngày gần nhất đến xa nhất", "Ngày xa nhất đến gần nhất")

        AlertDialog.Builder(requireContext())
            .setTitle("Sắp xếp đơn hàng")
            .setItems(sortOptions) { _, which ->
                when (which) {
                    0 -> sortOrdersByDate(ascending = false) // Gần nhất đến xa nhất
                    1 -> sortOrdersByDate(ascending = true)  // Xa nhất đến gần nhất
                }
            }
            .show()
    }

    private fun sortOrdersByDate(ascending: Boolean) {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        if (ascending) {
            filteredOrdersList.sortBy { dateFormat.parse(it.orderDate) }
        } else {
            filteredOrdersList.sortByDescending { dateFormat.parse(it.orderDate) }
        }
        ordersAdapter.updateOrders(filteredOrdersList)
    }

    private fun showLoader() {
        binding.loaderLayout.loaderLayout.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        binding.loaderLayout.loaderLayout.visibility = View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}

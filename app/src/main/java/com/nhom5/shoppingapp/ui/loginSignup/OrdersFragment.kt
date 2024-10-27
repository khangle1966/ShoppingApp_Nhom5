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

class OrdersFragment : Fragment() {

    private lateinit var binding: FragmentOrdersBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var ordersAdapter: OrdersAdapter
    private var ordersList = mutableListOf<Order>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thiết lập RecyclerView
        binding.orderAllOrdersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        ordersAdapter = OrdersAdapter(ordersList) { orderId ->
            // Khi người dùng nhấn vào một đơn hàng, điều hướng đến OrderDetailsFragment với orderId
            val action = OrdersFragmentDirections.actionOrdersFragmentToOrderDetailsFragment(orderId)
            findNavController().navigate(action)
        }
        binding.orderAllOrdersRecyclerView.adapter = ordersAdapter
        binding.ordersAppBar.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        if (currentUser != null) {
            loadOrders(currentUser.uid)
        } else {
            showError("Người dùng không hợp lệ.")
        }
    }

    private fun loadOrders(userId: String) {
        showLoader()

        val ordersRef = firestore.collection("orders").whereEqualTo("userId", userId)

        ordersRef.get()
            .addOnSuccessListener { querySnapshot ->
                ordersList = querySnapshot.documents.map { document ->
                    document.toObject(Order::class.java)!!
                }.toMutableList()

                if (ordersList.isNotEmpty()) {
                    binding.ordersEmptyTextView.visibility = View.GONE
                    ordersAdapter.updateOrders(ordersList)
                } else {
                    binding.ordersEmptyTextView.visibility = View.VISIBLE
                }

                hideLoader()
            }
            .addOnFailureListener { e ->
                hideLoader()
                showError("Có lỗi xảy ra khi tải đơn hàng: ${e.message}")
            }
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

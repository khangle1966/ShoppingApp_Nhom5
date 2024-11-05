package com.nhom5.shoppingapp.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.databinding.FragmentOrderDetailsBinding
import com.nhom5.shoppingapp.model.Order
import com.nhom5.shoppingapp.model.CartItem

class OrderDetailsFragment : Fragment() {

    private lateinit var binding: FragmentOrderDetailsBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var orderId: String
    private lateinit var orderDetailsAdapter: OrderDetailsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nhận orderId từ arguments
        orderId = arguments?.getString("orderId") ?: ""
        binding.orderDetailAppBar.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        setupRecyclerView()
        loadOrderDetails(orderId)
    }

    private fun setupRecyclerView() {
        binding.orderDetailsProRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        orderDetailsAdapter = OrderDetailsAdapter(mutableListOf())
        binding.orderDetailsProRecyclerView.adapter = orderDetailsAdapter
    }

    private fun loadOrderDetails(orderId: String) {
        showLoader()

        val orderRef = firestore.collection("orders").document(orderId)

        orderRef.get()
            .addOnSuccessListener { document ->
                val order = document.toObject(Order::class.java)

                order?.let {

                    // Hiển thị các chi tiết của đơn hàng
                    binding.orderDetailsPaymentLayout.priceItemsLabelTv.text = "Items (${it.items.size})"
                    binding.orderDetailsPaymentLayout.priceTotalAmountTv.text = "$${String.format("%.2f", it.totalPrice)}"
                    binding.orderDetailsShippingAddLayout.purchaseDateValue.text = "${it.orderDate}"
                    binding.orderDetailsShippingAddLayout.shipAddValueTv.text = it.shippingAddress
                    binding.orderDetailsShippingAddLayout.shipCurrStatusValueTv.text = it.status
                    binding.orderDetailsShippingAddLayout.customerNameValueTv.text = it.customName ?: "N/A"
                    binding.orderDetailsShippingAddLayout.customerPhoneValueTv.text = it.customPhone ?: "N/A"
                    orderDetailsAdapter.updateItems(it.items)

                    hideLoader()
                }
            }
            .addOnFailureListener { e ->
                hideLoader()
                showError("Không thể tải chi tiết đơn hàng: ${e.message}")
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

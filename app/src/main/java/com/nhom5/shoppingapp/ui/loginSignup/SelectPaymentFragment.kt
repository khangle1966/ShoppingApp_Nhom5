package com.nhom5.shoppingapp.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.R
import com.nhom5.shoppingapp.databinding.FragmentSelectPaymentBinding
import com.nhom5.shoppingapp.model.CartItem
import com.nhom5.shoppingapp.model.Order
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.*
class SelectPaymentFragment : Fragment() {
    private var customName: String? = null
    private var customPhone: String? = null
    private lateinit var binding: FragmentSelectPaymentBinding
    private lateinit var paymentAdapter: PaymentAdapter
    private val paymentMethods = listOf("Credit Card", "PayPal", "Cash on Delivery")
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private var totalPrice: Float = 0.0f
    private var selectedAddress: String? = null
    private var shippingAddress: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nhận selectedAddress và totalPrice từ AddressSelectionFragment
        shippingAddress = arguments?.getString("shippingAddress")
        totalPrice = arguments?.getFloat("totalPrice") ?: 0.0f
        customName = arguments?.getString("customName")
        customPhone = arguments?.getString("customPhone")
        setupRecyclerView()

        binding.payByNextBtn.text = "Thanh toán $totalPrice" // Hiển thị tổng tiền trên nút

        binding.payByNextBtn.setOnClickListener {
            val selectedPayment = paymentAdapter.getSelectedPayment()
            if (selectedPayment != null) {
                createOrder(selectedPayment)
            } else {
                Toast.makeText(requireContext(), "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show()
            }
        }
        binding.payByAppBar.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.payByPaymentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        paymentAdapter = PaymentAdapter(paymentMethods)
        binding.payByPaymentsRecyclerView.adapter = paymentAdapter
    }

    private fun createOrder(selectedPayment: String) {
        val user = currentUser
        if (user != null) {
            val userId = user.uid
            val orderId = firestore.collection("orders").document().id

            firestore.collection("carts").document(userId).collection("cartItems").get()
                .addOnSuccessListener { querySnapshot ->
                    val cartItems = querySnapshot.documents.map { document ->
                        CartItem(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            price = document.getDouble("price") ?: 0.0,
                            quantity = document.getLong("quantity")?.toInt() ?: 1,
                            imageUrl = document.getString("imageUrl") ?: "",
                            selectedColor = document.getString("selectedColor") ?: "",
                            selectedSize = document.getString("selectedSize") ?: ""
                        )
                    }

                    val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())

                    // Create the order with custom name and phone if available
                    val order = Order(
                        orderId = orderId,
                        userId = userId,
                        items = cartItems,
                        totalPrice = totalPrice.toDouble(),
                        shippingAddress = shippingAddress ?: "No Address",
                        paymentMethod = selectedPayment,
                        orderDate = currentDate,
                        status = "Processing",
                        customName = customName,
                        customPhone = customPhone
                    )

                    firestore.collection("orders").document(orderId).set(order)
                        .addOnSuccessListener {
                            clearPurchasedCartItems(userId, cartItems)
                            findNavController().navigate(R.id.action_selectPaymentFragment_to_orderSuccessFragment)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Không thể tạo đơn hàng: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Không thể lấy giỏ hàng: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }



    private fun clearPurchasedCartItems(userId: String, purchasedItems: List<CartItem>) {
        val cartItemsRef = firestore.collection("carts").document(userId).collection("cartItems")

        for (item in purchasedItems) {
            // Xóa từng sản phẩm đã mua từ giỏ hàng
            cartItemsRef.document(item.id).delete()
                .addOnSuccessListener {
                    // Bạn có thể thêm log hoặc thông báo ở đây nếu cần thiết
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Không thể xóa sản phẩm: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

}

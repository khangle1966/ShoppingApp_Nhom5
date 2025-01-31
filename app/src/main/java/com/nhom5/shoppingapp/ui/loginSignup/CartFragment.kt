package com.nhom5.shoppingapp.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.databinding.FragmentCartBinding
import com.nhom5.shoppingapp.model.CartItem
import androidx.navigation.fragment.findNavController
import com.nhom5.shoppingapp.R
import java.text.NumberFormat
import java.util.Locale
class CartFragment : Fragment(), CartItemActionListener {

    private lateinit var binding: FragmentCartBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var cartAdapter: CartAdapter
    private var cartItems = mutableListOf<CartItem>()
    private var totalPrice: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thiết lập RecyclerView
        binding.cartProductsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cartAdapter = CartAdapter(cartItems, this)
        binding.cartProductsRecyclerView.adapter = cartAdapter

        if (currentUser != null) {
            loadCartItems(currentUser.uid)
        } else {
            showError("Người dùng không hợp lệ.")
        }

        // Vô hiệu hóa nút Checkout ban đầu
        binding.cartCheckOutBtn.isEnabled = false

        binding.cartCheckOutBtn.setOnClickListener {
            onCheckOutButtonClick()
        }
    }

    private fun loadCartItems(userId: String) {
        showLoader()

        val cartRef = firestore.collection("carts").document(userId).collection("cartItems")

        cartRef.get()
            .addOnSuccessListener { querySnapshot ->
                cartItems = querySnapshot.documents.map { document ->
                    CartItem(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        price = document.getDouble("price") ?: 0.0,
                        quantity = document.getLong("quantity")?.toInt() ?: 1,
                        imageUrl = document.getString("imageUrl") ?: "",
                        selectedColor = document.getString("selectedColor") ?: "",
                        selectedSize = document.getString("selectedSize") ?: ""
                    )
                }.toMutableList()

                if (cartItems.isNotEmpty()) {
                    binding.cartEmptyTextView.visibility = View.GONE
                    binding.cartProductsRecyclerView.visibility = View.VISIBLE
                    binding.cartCheckOutBtn.isEnabled = true
                    cartAdapter.updateCartItems(cartItems)
                    updateSummaryInfo()
                } else {
                    binding.cartEmptyTextView.visibility = View.VISIBLE
                    binding.cartProductsRecyclerView.visibility = View.GONE
                    binding.cartCheckOutBtn.isEnabled = false
                }

                hideLoader()
            }
            .addOnFailureListener { e ->
                hideLoader()
                showError("Có lỗi xảy ra khi tải giỏ hàng: ${e.message}")
            }
    }


    private fun updateSummaryInfo() {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        val totalItemsPrice = cartItems.sumOf { it.price * it.quantity }
        val importCharges = totalItemsPrice * 0.1
        val shipping = if (cartItems.isNotEmpty()) 30.0 else 0.0
        totalPrice = (totalItemsPrice + shipping + importCharges).toFloat()

        binding.itemsPrice.text = "${numberFormat.format(totalItemsPrice)}$"
        binding.shippingPrice.text = "${numberFormat.format(shipping)}$"
        binding.importChargesPrice.text = "${numberFormat.format(importCharges)}$"
        binding.totalPrice.text = "${numberFormat.format(totalPrice)}$"
    }

    override fun onDelete(cartItem: CartItem) {
        val userId = currentUser?.uid ?: return
        val cartItemRef = firestore.collection("carts").document(userId).collection("cartItems").document(cartItem.id)

        cartItemRef.delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show()
                cartItems.remove(cartItem)
                cartAdapter.updateCartItems(cartItems)
                updateSummaryInfo()

                if (cartItems.isEmpty()) {
                    binding.cartEmptyTextView.visibility = View.VISIBLE
                    binding.cartProductsRecyclerView.visibility = View.GONE
                    binding.cartCheckOutBtn.isEnabled = false
                }
            }
            .addOnFailureListener { e ->
                showError("Không thể xóa sản phẩm: ${e.message}")
            }
    }

    private fun onCheckOutButtonClick() {
        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "Giỏ hàng trống. Không thể thanh toán.", Toast.LENGTH_SHORT).show()
            return
        }

        val action = CartFragmentDirections.actionCartFragmentToAddressSelectionFragment(totalPrice)
        findNavController().navigate(action)
    }

    override fun onIncrement(cartItem: CartItem) {
        val userId = currentUser?.uid ?: return
        val cartItemRef = firestore.collection("carts").document(userId).collection("cartItems").document(cartItem.id)

        val newQuantity = cartItem.quantity + 1

        cartItemRef.update("quantity", newQuantity)
            .addOnSuccessListener {
                val index = cartItems.indexOfFirst { it.id == cartItem.id }
                if (index != -1) {
                    cartItems[index] = cartItem.copy(quantity = newQuantity)
                    cartAdapter.updateCartItems(cartItems)
                    updateSummaryInfo()
                }
            }
            .addOnFailureListener { e ->
                showError("Không thể tăng số lượng: ${e.message}")
            }
    }

    override fun onDecrement(cartItem: CartItem) {
        if (cartItem.quantity > 1) {
            val userId = currentUser?.uid ?: return
            val cartItemRef = firestore.collection("carts").document(userId).collection("cartItems").document(cartItem.id)

            val newQuantity = cartItem.quantity - 1

            cartItemRef.update("quantity", newQuantity)
                .addOnSuccessListener {
                    val index = cartItems.indexOfFirst { it.id == cartItem.id }
                    if (index != -1) {
                        cartItems[index] = cartItem.copy(quantity = newQuantity)
                        cartAdapter.updateCartItems(cartItems)
                        updateSummaryInfo()
                    }
                }
                .addOnFailureListener { e ->
                    showError("Không thể giảm số lượng: ${e.message}")
                }
        } else {
            Toast.makeText(requireContext(), "Số lượng không thể nhỏ hơn 1", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showLoader() {
        binding.loaderLayout.loaderLayout.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        binding.loaderLayout.loaderLayout.visibility = View.GONE
    }
}

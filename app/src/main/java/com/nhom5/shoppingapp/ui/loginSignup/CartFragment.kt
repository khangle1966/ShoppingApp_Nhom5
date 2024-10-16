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

class CartFragment : Fragment(), CartItemActionListener {

    private lateinit var binding: FragmentCartBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var cartAdapter: CartAdapter
    private var cartItems = mutableListOf<CartItem>()

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
    }

    private fun loadCartItems(userId: String) {
        showLoader() // Hiển thị loader trước khi bắt đầu tải dữ liệu

        val cartRef = firestore.collection("carts").document(userId).collection("cartItems")

        cartRef.get()
            .addOnSuccessListener { querySnapshot ->
                cartItems = querySnapshot.documents.map { document ->
                    val id = document.id
                    // Lấy imageUrl dưới dạng List<String>
                    val imageUrls = document.get("imageUrl") as? List<String> ?: emptyList()
                    // Sử dụng hình ảnh đầu tiên hoặc một hình ảnh mặc định
                    val imageUrl = if (imageUrls.isNotEmpty()) imageUrls[0] else ""

                    CartItem(
                        id = id,
                        name = document.getString("name") ?: "",
                        price = document.getDouble("price") ?: 0.0,
                        quantity = document.getLong("quantity")?.toInt() ?: 1,
                        imageUrl = imageUrl
                    )
                }.toMutableList()

                if (cartItems.isNotEmpty()) {
                    binding.cartEmptyTextView.visibility = View.GONE
                    cartAdapter.updateCartItems(cartItems) // Cập nhật dữ liệu giỏ hàng trong adapter
                } else {
                    binding.cartEmptyTextView.visibility = View.VISIBLE
                }

                hideLoader() // Ẩn loader sau khi tải dữ liệu thành công
            }
            .addOnFailureListener { e ->
                hideLoader() // Ẩn loader khi có lỗi xảy ra
                showError("Có lỗi xảy ra khi lấy giỏ hàng: ${e.message}")
            }
    }

    override fun onDelete(cartItem: CartItem) {
        val userId = currentUser?.uid ?: return
        val cartItemRef = firestore.collection("carts").document(userId).collection("cartItems").document(cartItem.id)

        cartItemRef.delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show()
                // Xóa sản phẩm khỏi danh sách và cập nhật adapter
                cartItems.remove(cartItem)
                cartAdapter.updateCartItems(cartItems)
            }
            .addOnFailureListener { e ->
                showError("Không thể xóa sản phẩm: ${e.message}")
            }
    }

    override fun onIncrement(cartItem: CartItem) {
        val userId = currentUser?.uid ?: return
        val cartItemRef = firestore.collection("carts").document(userId).collection("cartItems").document(cartItem.id)

        val newQuantity = cartItem.quantity + 1

        cartItemRef.update("quantity", newQuantity)
            .addOnSuccessListener {
                // Cập nhật số lượng trong danh sách và adapter
                val index = cartItems.indexOfFirst { it.id == cartItem.id }
                if (index != -1) {
                    cartItems[index] = cartItem.copy(quantity = newQuantity)
                    cartAdapter.updateCartItems(cartItems)
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
                    // Cập nhật số lượng trong danh sách và adapter
                    val index = cartItems.indexOfFirst { it.id == cartItem.id }
                    if (index != -1) {
                        cartItems[index] = cartItem.copy(quantity = newQuantity)
                        cartAdapter.updateCartItems(cartItems)
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

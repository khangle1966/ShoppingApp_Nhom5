package com.nhom5.shoppingapp.ui.productdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nhom5.shoppingapp.databinding.FragmentProductDetailsBinding
import com.nhom5.shoppingapp.model.Product
import com.nhom5.shoppingapp.ui.adapters.ProductImageAdapter
import com.nhom5.shoppingapp.viewmodel.ProductViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.fragment.findNavController
import  com.nhom5.shoppingapp.R
class ProductDetailsFragment : Fragment() {
    private lateinit var binding: FragmentProductDetailsBinding
    private lateinit var productViewModel: ProductViewModel
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)

        val productId = arguments?.getString("productId")
        if (productId != null) {
            loadProductDetails(productId)
        } else {
            showError("Không tìm thấy sản phẩm.")
        }
        // Xử lý sự kiện khi nhấn "Add to Cart"
        binding.proDetailsAddCartBtn.setOnClickListener {
            val product = productViewModel.product.value
            if (product != null && currentUser != null) {
                addProductToCart(currentUser.uid, product)
            } else {
                showError("Sản phẩm hoặc người dùng không hợp lệ.")
            }
        }
    }

    private fun loadProductDetails(productId: String) {
        showLoading()

        productViewModel.getProductById(productId)
        productViewModel.product.observe(viewLifecycleOwner) { product ->
            if (product != null) {
                hideLoading()
                bindProductDetails(product)
            } else {
                showError("Không thể tải thông tin sản phẩm.")
            }
        }
    }

    private fun showLoading() {
        binding.loaderLayout.loaderLayout.visibility = View.VISIBLE
        binding.proDetailsScrollView.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.loaderLayout.loaderLayout.visibility = View.GONE
        binding.proDetailsScrollView.visibility = View.VISIBLE
    }


    private fun bindProductDetails(product: Product) {
        // Hiển thị tên sản phẩm
        binding.proDetailsTitleTv.text = product.name
        // Hiển thị giá sản phẩm
        binding.proDetailsPriceTv.text = "$${product.price}"
        // Hiển thị đánh giá sản phẩm
        binding.proDetailsRatingBar.rating = product.rating

        // Hiển thị hình ảnh sản phẩm
        val imageAdapter = ProductImageAdapter(product.imageUrl)
        binding.proDetailsImagesRecyclerview.adapter = imageAdapter

        // Hiển thị size sản phẩm
        binding.proDetailsSizesRadioGroup.removeAllViews() // Xóa các RadioButton cũ nếu có
        product.size?.forEach { size ->
            val radioButton = RadioButton(context)
            radioButton.text = size
            binding.proDetailsSizesRadioGroup.addView(radioButton)
        }

        // Hiển thị màu sản phẩm
        binding.proDetailsColorsRadioGroup.removeAllViews() // Xóa các RadioButton cũ nếu có
        product.color?.forEach { color ->
            val radioButton = RadioButton(context)
            radioButton.text = color
            binding.proDetailsColorsRadioGroup.addView(radioButton)
        }

        // Hiển thị thông số kỹ thuật của sản phẩm
        binding.proDetailsSpecificsText.text = product.specifications
    }

    // Hàm để thêm sản phẩm vào giỏ hàng trong Firestore
    private fun addProductToCart(userId: String, product: Product) {
        val cartRef = firestore.collection("carts").document(userId).collection("cartItems").document(product.productId)

        cartRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Nếu sản phẩm đã tồn tại trong giỏ hàng, tăng số lượng
                val currentQuantity = document.getLong("quantity") ?: 1
                cartRef.update("quantity", currentQuantity + 1)
            } else {
                // Thêm sản phẩm mới vào giỏ hàng
                val cartItem = hashMapOf(
                    "name" to product.name,
                    "price" to product.price,
                    "quantity" to 1,
                    "imageUrl" to product.imageUrl
                )
                cartRef.set(cartItem)
            }
            Toast.makeText(requireContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
            navigateToCart() // Điều hướng đến CartFragment sau khi thêm
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Có lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToCart() {
        findNavController().navigate(R.id.action_productDetailsFragment_to_cartFragment)
    }


}

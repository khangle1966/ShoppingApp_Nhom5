package com.nhom5.shoppingapp.ui.product

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.databinding.FragmentAddProductBinding
import com.nhom5.shoppingapp.model.Product
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 71
    private lateinit var imageAdapter: ImageAdapter
    private val selectedImages = mutableListOf<Uri>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)

        // Cấu hình RecyclerView
        imageAdapter = ImageAdapter(selectedImages) { uri -> removeImage(uri) }
        binding.addProImagesRv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.addProImagesRv.adapter = imageAdapter

        // Mở thư viện chọn hình ảnh khi nhấn nút thêm hình ảnh
        binding.addProImagesBtn.setOnClickListener {
            chooseImage()
        }

        binding.addProBtn.setOnClickListener {
            showLoader() // Hiển thị loader

            // Lấy thông tin từ các trường nhập liệu
            val productName = binding.proNameEditText.text.toString().trim()
            val productPrice = binding.proPriceEditText.text.toString().toDoubleOrNull() ?: 0.0
            val productMrp = binding.proMrpEditText.text.toString().toDoubleOrNull() ?: 0.0

            val productDesc = binding.proDescEditText.text.toString().trim()

            if (validateInput(productName, productPrice, productMrp, productDesc)) {
                if (selectedImages.isNotEmpty()) {
                    uploadImagesAndAddProduct(productName, productPrice, productMrp, productDesc)
                } else {
                    addProductToFirebase(
                        Product(
                            name = productName,
                            price = productPrice,
                            actualPrice = productMrp,
                            imageUrl = "",
                            rating = 0f,
                            offerPercentage = "0"
                        )
                    )
                }
            } else {
                hideLoader() // Ẩn loader nếu có lỗi
            }
        }

        return binding.root
    }

    private fun validateInput(
        name: String,
        price: Double,
        mrp: Double,
        desc: String
    ): Boolean {
        return when {
            name.isEmpty() -> {
                showError("Tên sản phẩm không được để trống")
                false
            }
            price <= 0.0 -> {
                showError("Giá bán phải lớn hơn 0")
                false
            }
            mrp <= 0.0 -> {
                showError("Giá niêm yết phải lớn hơn 0")
                false
            }
            desc.isEmpty() -> {
                showError("Mô tả không được để trống")
                false
            }
            else -> {
                binding.addProErrorTextView.visibility = View.GONE
                true
            }
        }
    }

    private fun showError(message: String) {
        binding.addProErrorTextView.text = message
        binding.addProErrorTextView.visibility = View.VISIBLE
    }

    private fun addProductToFirebase(product: Product) {
        firestore.collection("products")
            .add(product)
            .addOnSuccessListener {
                Toast.makeText(context, "Sản phẩm đã được thêm", Toast.LENGTH_SHORT).show()
                hideLoader()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                hideLoader()
            }
    }

    private fun uploadImagesAndAddProduct(
        name: String,
        price: Double,
        mrp: Double,
        desc: String
    ) {
        val imageUrls = mutableListOf<String>()
        selectedImages.forEach { uri ->
            val filePath = storage.child("images/${UUID.randomUUID()}")
            filePath.putFile(uri)
                .addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener { downloadUri ->
                        imageUrls.add(downloadUri.toString())
                        if (imageUrls.size == selectedImages.size) {
                            addProductToFirebase(
                                Product(
                                    name = name,
                                    price = price,
                                    actualPrice = mrp,
                                    imageUrl = imageUrls[0],
                                    rating = 0f,
                                    offerPercentage = "0"
                                )
                            )
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Lỗi upload hình ảnh: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    hideLoader()
                }
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Chọn hình ảnh"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let { imageAdapter.addImage(it) }
        }
    }

    private fun removeImage(uri: Uri) {
        imageAdapter.removeImage(uri)
    }

    private fun showLoader() {
        binding.loaderLayout.loaderLayout.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        binding.loaderLayout.loaderLayout.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

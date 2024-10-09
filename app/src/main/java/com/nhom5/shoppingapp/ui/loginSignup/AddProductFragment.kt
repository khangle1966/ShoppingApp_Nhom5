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
        binding.addProImagesRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.addProImagesRv.adapter = imageAdapter

        // Mở thư viện chọn hình ảnh khi nhấn nút thêm hình ảnh
        binding.addProImagesBtn.setOnClickListener {
            chooseImage()
        }

        binding.addProBtn.setOnClickListener {
            // Hiển thị loader khi người dùng nhấn nút Add Product
            showLoader()

            // Lấy thông tin sản phẩm từ các trường nhập liệu
            val productName = binding.proNameEditText.text.toString().trim()
            val productPrice = binding.proPriceEditText.text.toString().trim()
            val productMrp = binding.proMrpEditText.text.toString().trim()
            val productDesc = binding.proDescEditText.text.toString().trim()

            // Kiểm tra dữ liệu hợp lệ trước khi thêm sản phẩm
            if (validateInput(productName, productPrice, productMrp, productDesc)) {
                // Nếu có hình ảnh, upload trước rồi thêm sản phẩm
                if (selectedImages.isNotEmpty()) {
                    uploadImagesAndAddProduct(productName, productPrice, productMrp, productDesc)
                } else {
                    addProductToFirebase(Product(
                        name = productName,
                        price = productPrice,
                        actualPrice = productMrp,
                        imageUrl = "", // Không có hình ảnh
                        rating = 0f,
                        offerPercentage = "0"
                    ))
                }
            } else {
                hideLoader() // Ẩn loader nếu có lỗi
            }
        }

        return binding.root
    }

    private fun validateInput(name: String, price: String, mrp: String, desc: String): Boolean {
        return when {
            name.isEmpty() -> {
                binding.addProErrorTextView.text = "Tên sản phẩm không được để trống"
                binding.addProErrorTextView.visibility = View.VISIBLE
                false
            }
            price.isEmpty() -> {
                binding.addProErrorTextView.text = "Giá bán không được để trống"
                binding.addProErrorTextView.visibility = View.VISIBLE
                false
            }
            mrp.isEmpty() -> {
                binding.addProErrorTextView.text = "Giá niêm yết không được để trống"
                binding.addProErrorTextView.visibility = View.VISIBLE
                false
            }
            desc.isEmpty() -> {
                binding.addProErrorTextView.text = "Mô tả không được để trống"
                binding.addProErrorTextView.visibility = View.VISIBLE
                false
            }
            else -> {
                binding.addProErrorTextView.visibility = View.GONE
                true
            }
        }
    }

    private fun addProductToFirebase(product: Product) {
        firestore.collection("products")
            .add(product)
            .addOnSuccessListener {
                Toast.makeText(context, "Sản phẩm đã được thêm", Toast.LENGTH_SHORT).show()
                hideLoader() // Ẩn loader sau khi thêm sản phẩm thành công
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                hideLoader() // Ẩn loader nếu có lỗi xảy ra
            }
    }

    private fun uploadImagesAndAddProduct(name: String, price: String, mrp: String, desc: String) {
        val imageUrls = mutableListOf<String>()
        selectedImages.forEachIndexed { index, uri ->
            val filePath = storage.child("images/" + UUID.randomUUID().toString())
            filePath.putFile(uri)
                .addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener { downloadUri ->
                        imageUrls.add(downloadUri.toString())
                        if (imageUrls.size == selectedImages.size) {
                            // Sau khi tất cả ảnh đã upload, thêm sản phẩm
                            addProductToFirebase(Product(
                                name = name,
                                price = price,
                                actualPrice = mrp,
                                imageUrl = imageUrls[0], // Lấy ảnh đầu tiên làm ảnh chính
                                rating = 0f,
                                offerPercentage = "0"
                            ))
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Lỗi upload hình ảnh: ${it.message}", Toast.LENGTH_SHORT).show()
                    hideLoader() // Ẩn loader nếu upload hình ảnh thất bại
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
            selectedImageUri?.let {
                imageAdapter.addImage(it) // Thêm ảnh vào RecyclerView
            }
        }
    }

    private fun removeImage(uri: Uri) {
        imageAdapter.removeImage(uri) // Xóa ảnh khỏi RecyclerView
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

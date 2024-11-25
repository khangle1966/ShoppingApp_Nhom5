package com.nhom5.shoppingapp.ui.product

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.databinding.FragmentAddProductBinding
import com.nhom5.shoppingapp.model.Product
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import com.nhom5.shoppingapp.R
import com.google.android.material.chip.Chip
import androidx.core.content.ContextCompat
import android.content.res.ColorStateList
import androidx.navigation.fragment.findNavController

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 71
    private lateinit var imageAdapter: ImageAdapter
    private val selectedImages = mutableListOf<Uri>()

    // Biến để lưu lựa chọn nhiều size và color
    private val selectedSizes: MutableList<String> = mutableListOf()
    private val selectedColors: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        binding.addProAppBar.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        // Cấu hình RecyclerView cho hình ảnh
        imageAdapter = ImageAdapter(selectedImages) { uri -> removeImage(uri) }
        binding.addProImagesRv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.addProImagesRv.adapter = imageAdapter

        // Xử lý chọn nhiều size từ ChipGroup
        setupSizeSelection()

        // Xử lý chọn nhiều color từ ChipGroup
        setupColorSelection()

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
                            imageUrl = listOf(), // Không có hình ảnh ban đầu
                            rating = 0f,
                            offerPercentage = "0",
                            size = selectedSizes,  // Truyền danh sách các size đã chọn
                            color = selectedColors,  // Truyền danh sách các màu đã chọn
                            specifications = productDesc
                        )
                    )
                }
            } else {
                hideLoader() // Ẩn loader nếu có lỗi
            }
        }

        return binding.root
    }

    private fun setupSizeSelection() {
        binding.addProSizeChipGroup.children.forEach { view ->
            if (view is Chip) {
                view.setOnCheckedChangeListener { _, isChecked ->
                    val size = view.text.toString()
                    if (isChecked) {
                        if (!selectedSizes.contains(size)) {
                            selectedSizes.add(size)
                        }
                    } else {
                        selectedSizes.remove(size)
                    }
                }
            }
        }
    }

    private fun setupColorSelection() {
        val colorMap = mapOf(
            binding.colorBlack to "Đen",
            binding.colorWhite to "Trắng",
            binding.colorRed to "Đỏ",
            binding.colorGreen to "Xanh Lá",
            binding.colorBlue to "Xanh Dương",
            binding.colorYellow to "Vàng"
        )

        colorMap.forEach { (view, colorName) ->
            view.setOnClickListener {
                if (selectedColors.contains(colorName)) {
                    // Nếu đã chọn, bỏ chọn và xóa khỏi danh sách
                    selectedColors.remove(colorName)
                    view.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.transparent))
                } else {
                    // Nếu chưa chọn, thêm vào danh sách và làm nổi bật viền
                    selectedColors.add(colorName)
                    view.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue_accent_300))
                }
            }
        }
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
            selectedSizes.isEmpty() -> {
                showError("Hãy chọn ít nhất 1 size cho sản phẩm")
                false
            }
            selectedColors.isEmpty() -> {
                showError("Hãy chọn ít nhất 1 màu cho sản phẩm")
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
                                    imageUrl = imageUrls,
                                    rating = 0f,
                                    offerPercentage = "0",
                                    size = selectedSizes,
                                    color = selectedColors,
                                    specifications = desc
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

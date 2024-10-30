package com.nhom5.shoppingapp.ui.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nhom5.shoppingapp.R
import com.nhom5.shoppingapp.databinding.FragmentEditProductBinding
import com.nhom5.shoppingapp.model.Product

class EditProductFragment : Fragment(R.layout.fragment_edit_product) {

    private lateinit var binding: FragmentEditProductBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var productId: String? = null
    private var selectedImageUri: Uri? = null
    private val imageUrls = mutableListOf<String>()  // Lưu URL của các ảnh đã chọn

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditProductBinding.bind(view)

        productId = arguments?.getString("productId")

        binding.editProductAppBar.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        if (productId != null) {
            loadProductData(productId!!)
        }

        val imagePicker =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    selectedImageUri = result.data?.data
                    selectedImageUri?.let { uri ->
                        // Hiển thị hình ảnh đã chọn
                        addImageToContainer(uri.toString())
                    }
                }
            }

        binding.selectImageBtn.setOnClickListener {
            if (imageUrls.size < 3) {  // Giới hạn chỉ cho phép chọn tối đa 3 hình ảnh
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                imagePicker.launch(intent)
            } else {
                Toast.makeText(requireContext(), "Chỉ được thêm tối đa 3 ảnh", Toast.LENGTH_SHORT).show()
            }
        }

        binding.saveProductBtn.setOnClickListener {
            uploadImageToFirebaseStorage { updatedImageUrls ->
                saveProductChanges(updatedImageUrls)  // Truyền URL ảnh vào hàm lưu sản phẩm
            }
        }
    }

    private fun loadProductData(productId: String) {
        firestore.collection("products").document(productId)
            .get()
            .addOnSuccessListener { document ->
                val product = document.toObject(Product::class.java)
                product?.let {
                    binding.productNameEt.setText(it.name)
                    binding.productPriceEt.setText(it.price.toString())
                    binding.productActualPriceEt.setText(it.actualPrice.toString())
                    binding.productOfferPercentageEt.setText(it.offerPercentage.toString())
                    binding.productSpecificationsEt.setText(it.specifications.toString())

                    // Load hình ảnh
                    if (it.imageUrl.isNotEmpty()) {
                        imageUrls.addAll(it.imageUrl)  // Thêm các URL ảnh vào danh sách

                        // Xóa tất cả các ImageView cũ nếu có
                        binding.imageContainer.removeAllViews()

                        // Tạo và thêm ImageView động vào LinearLayout
                        imageUrls.forEach { url ->
                            addImageToContainer(url)
                        }
                    }

                    setColorSelections(it.color)
                    setSizeSelections(it.size)
                }
            }
    }

    private fun addImageToContainer(imageUrl: String) {
        // Tạo ImageView mới
        val imageView = ImageView(requireContext())
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 0, 16, 0)  // Tạo khoảng cách giữa các ảnh
        imageView.layoutParams = layoutParams
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.adjustViewBounds = true
        imageView.layoutParams.width = 200  // Đặt kích thước cho các hình ảnh
        imageView.layoutParams.height = 200

        // Sử dụng Glide để tải ảnh
        Glide.with(this)
            .load(imageUrl)
            .into(imageView)

        // Xử lý sự kiện nhấn giữ để xóa ảnh
        imageView.setOnLongClickListener {
            // Hiển thị một thông báo xác nhận xóa ảnh
            Toast.makeText(requireContext(), "Xóa ảnh này", Toast.LENGTH_SHORT).show()

            // Xóa ảnh khỏi danh sách và LinearLayout
            binding.imageContainer.removeView(imageView)  // Xóa ImageView khỏi LinearLayout
            imageUrls.remove(imageUrl)  // Xóa URL ảnh khỏi danh sách

            true  // Trả về true để đánh dấu sự kiện đã được xử lý
        }

        // Thêm ImageView vào LinearLayout
        binding.imageContainer.addView(imageView)
    }

    private fun setColorSelections(selectedColors: List<String>) {
        binding.colorBlack.isChecked = selectedColors.contains("Black")
        binding.colorWhite.isChecked = selectedColors.contains("White")
        binding.colorRed.isChecked = selectedColors.contains("Red")
        binding.colorChipGreen.isChecked = selectedColors.contains("Green")
        binding.colorBlue.isChecked = selectedColors.contains("Blue")
        binding.goldColor.isChecked = selectedColors.contains("Yellow")
    }

    private fun setSizeSelections(selectedSizes: List<String>) {
        binding.sizeS.isChecked = selectedSizes.contains("S")
        binding.sizeM.isChecked = selectedSizes.contains("M")
        binding.sizeL.isChecked = selectedSizes.contains("L")
        binding.sizeXl.isChecked = selectedSizes.contains("XL")
    }

    private fun saveProductChanges(updatedImageUrls: List<String>) {
        val name = binding.productNameEt.text.toString()
        val price = binding.productPriceEt.text.toString().toDouble()
        val actualPrice = binding.productActualPriceEt.text.toString().toDouble()
        val offerPercentage = binding.productOfferPercentageEt.text.toString()
        val specifications = binding.productSpecificationsEt.text.toString()

        val colors = mutableListOf<String>()
        if (binding.colorBlack.isChecked) colors.add("Black")
        if (binding.colorWhite.isChecked) colors.add("White")
        if (binding.colorRed.isChecked) colors.add("Red")
        if (binding.colorChipGreen.isChecked) colors.add("Green")
        if (binding.colorBlue.isChecked) colors.add("Blue")
        if (binding.goldColor.isChecked) colors.add("Yellow")

        val sizes = mutableListOf<String>()
        if (binding.sizeS.isChecked) sizes.add("S")
        if (binding.sizeM.isChecked) sizes.add("M")
        if (binding.sizeL.isChecked) sizes.add("L")
        if (binding.sizeXl.isChecked) sizes.add("XL")

        val productUpdates = mutableMapOf<String, Any>(
            "name" to name,
            "price" to price,
            "actualPrice" to actualPrice,
            "offerPercentage" to offerPercentage,
            "specifications" to specifications,
            "color" to colors,
            "size" to sizes
        )

        if (imageUrls.isNotEmpty()) {
            productUpdates["imageUrl"] = imageUrls  // Cập nhật danh sách URL ảnh
        }

        productId?.let {
            firestore.collection("products").document(it)
                .update(productUpdates)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Product updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update product", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadImageToFirebaseStorage(callback: (List<String>) -> Unit) {
        selectedImageUri?.let { uri ->
            val storageRef = storage.reference.child("product_images/${System.currentTimeMillis()}.jpg")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        imageUrls.add(uri.toString())  // Thêm URL ảnh vào danh sách
                        callback(imageUrls.take(3))  // Trả về tối đa 3 URL ảnh
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

//
//    private fun getSelectedColors(): List<String> {
//        val selectedColors = mutableListOf<String>()
//        if (binding.colorBlack.isChecked) selectedColors.add("Black")
//        if (binding.colorWhite.isChecked) selectedColors.add("White")
//        if (binding.colorRed.isChecked) selectedColors.add("Red")
//        if (binding.colorChipGreen.isChecked) selectedColors.add("Green")
//        if (binding.colorBlue.isChecked) selectedColors.add("Blue")
//        if (binding.goldColor.isChecked) selectedColors.add("Yellow")
//
//        return selectedColors
//    }
//
//    private fun getSelectedSizes(): List<String> {
//        val selectedSizes = mutableListOf<String>()
//        if (binding.sizeS.isChecked) selectedSizes.add("S")
//        if (binding.sizeM.isChecked) selectedSizes.add("M")
//        if (binding.sizeL.isChecked) selectedSizes.add("L")
//        if (binding.sizeXl.isChecked) selectedSizes.add("XL")
//        return selectedSizes
//    }
//
//    private fun updateProduct(
//        name: String,
//        price: Double,
//        actualPrice: Double,
//        selectedColors: List<String>,
//        selectedSizes: List<String>,
//        imageUrl: List<String>
//    ) {
//        val productUpdates = mapOf(
//            "name" to name,
//            "price" to price,
//            "actualPrice" to actualPrice,
//            "color" to selectedColors,
//            "size" to selectedSizes,
//            "imageUrl" to imageUrl
//        )
//
//        productId?.let {
//            firestore.collection("products").document(it)
//                .update(productUpdates)
//                .addOnSuccessListener {
//                    Toast.makeText(requireContext(), "Product updated successfully", Toast.LENGTH_SHORT).show()
//                    // Xử lý điều hướng sau khi lưu thành công
//                }
//                .addOnFailureListener {
//                    Toast.makeText(requireContext(), "Failed to update product", Toast.LENGTH_SHORT).show()
//                }
//        }
//    }


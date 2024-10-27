package com.nhom5.shoppingapp.ui.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
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

        // Xử lý chọn hình ảnh từ bộ nhớ
        val imagePicker =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    selectedImageUri = result.data?.data
                    Glide.with(this)
                        .load(selectedImageUri)
                        .into(binding.selectedImageView)
                    binding.selectedImageView.visibility = View.VISIBLE
                }
            }

        binding.selectImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePicker.launch(intent)
        }

        binding.saveProductBtn.setOnClickListener {
            saveProductChanges()
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
                        Glide.with(this)
                            .load(it.imageUrl[0])  // Lấy ảnh đầu tiên làm đại diện
                            .into(binding.selectedImageView)
                        binding.selectedImageView.visibility = View.VISIBLE
                    }

                    // Chọn màu từ Firebase và hiển thị màu đã chọn
                    setColorSelections(it.color)

                    // Chọn size đã lưu
                    setSizeSelections(it.size)
                }
            }
    }

    private fun setColorSelections(selectedColors: List<String>) {
        // Kiểm tra màu đã chọn và đánh dấu vào checkbox tương ứng
        binding.colorBlack.isChecked = selectedColors.contains("Black")
        binding.colorWhite.isChecked = selectedColors.contains("White")
        binding.colorRed.isChecked = selectedColors.contains("Red")
        binding.colorChipGreen.isChecked = selectedColors.contains("Green")
        binding.colorBlue.isChecked = selectedColors.contains("Blue")
        binding.goldColor.isChecked = selectedColors.contains("Yellow")


    }

    private fun setSizeSelections(selectedSizes: List<String>) {
        // Kiểm tra size đã chọn và đánh dấu vào ToggleButton tương ứng
        binding.sizeS.isChecked = selectedSizes.contains("S")
        binding.sizeM.isChecked = selectedSizes.contains("M")
        binding.sizeL.isChecked = selectedSizes.contains("L")
        binding.sizeXl.isChecked = selectedSizes.contains("XL")
    }

    private fun saveProductChanges() {
        val name = binding.productNameEt.text.toString()
        val price = binding.productPriceEt.text.toString().toDouble()
        val actualPrice = binding.productActualPriceEt.text.toString().toDouble()
        val offerPercentage = binding.productOfferPercentageEt.text.toString()
        val specifications = binding.productSpecificationsEt.text.toString()

        // Lấy danh sách màu được chọn
        val colors = mutableListOf<String>()
        if (binding.colorBlack.isChecked) colors.add("Black")
        if (binding.colorWhite.isChecked) colors.add("White")
        if (binding.colorRed.isChecked) colors.add("Red")
        if (binding.colorChipGreen.isChecked) colors.add("Green")
        if (binding.colorBlue.isChecked) colors.add("Blue")
        if (binding.goldColor.isChecked) colors.add("Yellow")


        // Lấy danh sách size được chọn
        val sizes = mutableListOf<String>()
        if (binding.sizeS.isChecked) sizes.add("S")
        if (binding.sizeM.isChecked) sizes.add("M")
        if (binding.sizeL.isChecked) sizes.add("L")
        if (binding.sizeXl.isChecked) sizes.add("XL")

        // Tạo bản cập nhật sản phẩm
        val productUpdates = mapOf(
            "name" to name,
            "price" to price,
            "actualPrice" to actualPrice,
            "offerPercentage" to offerPercentage,
            "specifications" to specifications,
            "color" to colors,
            "size" to sizes
        )

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
}

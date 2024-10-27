package com.nhom5.shoppingapp.ui.loginSignup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nhom5.shoppingapp.databinding.FragmentProfileBinding
import com.nhom5.shoppingapp.model.User

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var avatarUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Tải thông tin người dùng từ Firestore theo UserID
        loadUserProfile()

        // Nút Save
        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        // Nút chọn avatar
        binding.profileImage.setOnClickListener {
            selectAvatar()
        }

        return view
    }

    // Tải thông tin người dùng từ Firebase Firestore
    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            binding.profileEmail.setText(user.email)

            // Lấy tài liệu người dùng từ Firestore theo UID
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Ánh xạ tài liệu từ Firestore thành đối tượng User
                        val userData = document.toObject(User::class.java)
                        userData?.let {
                            binding.profileName.setText(it.name)
                            binding.profilePhone.setText(it.mobile)
                            binding.profileAddress.setText(it.address)
                        }
                    } else {
                        Log.d("ProfileFragment", "No such document")
                        Toast.makeText(requireContext(), "Không tìm thấy dữ liệu người dùng!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("ProfileFragment", "Error getting documents: ", exception)
                    Toast.makeText(requireContext(), "Lỗi khi tải dữ liệu: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Không có người dùng đăng nhập!", Toast.LENGTH_SHORT).show()
        }
    }

    // Phương thức chọn ảnh avatar từ thiết bị
    private fun selectAvatar() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    // Nhận kết quả khi người dùng chọn ảnh
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK && data != null) {
            avatarUri = data.data
            binding.profileImage.setImageURI(avatarUri)  // Hiển thị ảnh đã chọn
        }
    }

    // Phương thức lưu thông tin người dùng và avatar
    private fun saveProfile() {
        val user = auth.currentUser
        if (user != null) {
            // Lấy thông tin từ giao diện
            val name = binding.profileName.text.toString()
            val mobile = binding.profilePhone.text.toString()
            val address = binding.profileAddress.text.toString()
            val email = binding.profileEmail.text.toString()

            // Kiểm tra các trường thông tin có trống không
            if (name.isEmpty() || mobile.isEmpty() || address.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return
            }

            // Sử dụng đối tượng User để lưu vào Firestore
            val userData = User(uid = user.uid, name = name, mobile = mobile, address = address, email = email)

            // Lưu thông tin người dùng vào Firestore
            db.collection("users").document(user.uid).set(userData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Thông tin đã được lưu", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Lỗi khi lưu dữ liệu: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

            // Lưu ảnh avatar nếu người dùng đã chọn
            avatarUri?.let {
                val storageRef = storage.reference.child("avatars/${user.uid}.jpg")
                storageRef.putFile(it).addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        db.collection("users").document(user.uid).update("avatarUrl", uri.toString())
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Lỗi khi tải ảnh: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

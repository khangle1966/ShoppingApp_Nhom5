package com.nhom5.shoppingapp.ui.loginSignup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.R
import com.nhom5.shoppingapp.databinding.FragmentSignupBinding
import com.nhom5.shoppingapp.model.User

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        // Ẩn thông báo lỗi ban đầu
        binding.signupErrorTextView.visibility = View.GONE

        // Sự kiện nút đăng ký
        binding.signupSignupBtn.setOnClickListener {
            val name = binding.signupNameEditText.text.toString()
            val email = binding.signupEmailEditText.text.toString()
            val password = binding.signupPasswordEditText.text.toString()
            val confirmPassword = binding.signupCnfPasswordEditText.text.toString()
            val mobile = binding.signupMobileEditText.text.toString()

            if (kiemTraThongTin(name, email, password, confirmPassword, mobile)) {
                dangKyTaiKhoan(email, password)
            }
        }

        // Chuyển đến trang đăng nhập khi người dùng nhấn vào signup_login_text_view
        binding.signupLoginTextView.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }

        return binding.root
    }

    private fun kiemTraThongTin(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        mobile: String
    ): Boolean {
        return when {
            name.isEmpty() -> {
                hienThiLoi("Vui lòng nhập tên của bạn")
                false
            }
            email.isEmpty() -> {
                hienThiLoi("Vui lòng nhập email")
                false
            }
            mobile.isEmpty() -> {
                hienThiLoi("Vui lòng nhập số điện thoại")
                false
            }
            password.isEmpty() -> {
                hienThiLoi("Vui lòng nhập mật khẩu")
                false
            }
            confirmPassword != password -> {
                hienThiLoi("Mật khẩu xác nhận không khớp")
                false
            }
            else -> true
        }
    }

    private fun dangKyTaiKhoan(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid
                    val userEmail = firebaseUser?.email
                    val name = binding.signupNameEditText.text.toString()
                    val mobile = binding.signupMobileEditText.text.toString()

                    val user = User(
                        uid = userId.toString(),
                        mobile = mobile,
                        address = "",  // Không có địa chỉ mặc định
                        name = name,
                        email = userEmail.toString()
                    )

                    val db = FirebaseFirestore.getInstance()
                    userId?.let {
                        db.collection("users").document(it).set(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
                                } else {
                                    Toast.makeText(context, "Không thể lưu dữ liệu người dùng: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(context, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun hienThiLoi(message: String) {
        binding.signupErrorTextView.text = message
        binding.signupErrorTextView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

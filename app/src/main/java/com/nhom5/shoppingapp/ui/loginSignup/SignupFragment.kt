package com.nhom5.shoppingapp.ui.loginSignup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController // Import NavController
import com.google.firebase.auth.FirebaseAuth
import com.nhom5.shoppingapp.R // Import R
import com.nhom5.shoppingapp.databinding.FragmentSignupBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.model.User // Đảm bảo đường dẫn này đúng với nơi bạn lưu trữ lớp User

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

            if (kiemTraThongTin(name, email, password, confirmPassword)) {
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
        confirmPassword: String
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
                    // Đăng ký thành công, lấy thông tin người dùng
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid
                    val userEmail = firebaseUser?.email
                    val name = binding.signupNameEditText.text.toString()
                    val mobile = binding.signupMobileEditText.text.toString()

                    // Tạo đối tượng User
                    val user = User(
                        UID = userId.toString(),
                        Mobile = mobile,  // Tạm thời, sẽ cập nhật sau
                        Address = "",  // Tạm thời, sẽ cập nhật sau
                        Name = name,  // Lấy tên từ EditText
                        Email = userEmail.toString()
                    )

                    // Lưu đối tượng User vào Firebase Firestore
                    val db = FirebaseFirestore.getInstance()
                    userId?.let {
                        db.collection("users").document(it).set(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    // Lưu dữ liệu thành công
                                    Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Xử lý lỗi khi lưu dữ liệu
                                    Toast.makeText(context, "Không thể lưu dữ liệu người dùng: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    // Xử lý lỗi đăng ký
                    Toast.makeText(context, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



    private fun hienThiLoi(message: String) {
        binding.signupErrorTextView.text = message
        binding.signupErrorTextView.visibility = View.VISIBLE
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

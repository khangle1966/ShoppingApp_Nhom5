package com.nhom5.shoppingapp.ui.loginSignup

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.nhom5.shoppingapp.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        // Sự kiện nút đăng nhập
        binding.loginLoginBtn.setOnClickListener {
            val email = binding.loginEmailEditText.text.toString().trim()
            val password = binding.loginPasswordEditText.text.toString().trim()

            if (kiemTraThongTin(email, password)) {
                dangNhap(email, password)
            }
        }

        // Chuyển đến trang đăng ký
        binding.loginSignupTextView.setOnClickListener {
            (activity as? LoginSignupActivity)?.loadFragment(SignupFragment())
        }

        return binding.root
    }

    // Kiểm tra thông tin đăng nhập
    private fun kiemTraThongTin(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.loginErrorTextView.text = "Vui lòng nhập email"
                binding.loginErrorTextView.visibility = View.VISIBLE
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.loginErrorTextView.text = "Email không hợp lệ"
                binding.loginErrorTextView.visibility = View.VISIBLE
                false
            }
            password.isEmpty() -> {
                binding.loginErrorTextView.text = "Vui lòng nhập mật khẩu"
                binding.loginErrorTextView.visibility = View.VISIBLE
                false
            }
            else -> {
                binding.loginErrorTextView.visibility = View.GONE // Ẩn thông báo lỗi khi thông tin hợp lệ
                true
            }
        }
    }

    // Đăng nhập với Firebase Authentication
    private fun dangNhap(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.loginErrorTextView.visibility = View.GONE
                    Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                    // Chuyển đến màn hình khác (Ví dụ: trang chủ)
                } else {
                    binding.loginErrorTextView.text = "Đăng nhập thất bại: ${task.exception?.message}"
                    binding.loginErrorTextView.visibility = View.VISIBLE
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

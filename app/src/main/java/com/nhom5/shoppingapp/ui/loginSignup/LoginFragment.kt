package com.nhom5.shoppingapp.ui.loginSignup

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.nhom5.shoppingapp.databinding.FragmentLoginBinding
import android.content.Context
import com.nhom5.shoppingapp.ui.home.MainActivity // Import MainActivity

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
        loadLoginInfo()

        // Sự kiện nút đăng nhập
        binding.loginLoginBtn.setOnClickListener {
            val email = binding.loginEmailEditText.text.toString().trim()
            val password = binding.loginPasswordEditText.text.toString().trim()

            if (checkCredentials(email, password)) {
                login(email, password)
            } else {
                Toast.makeText(context, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show()
            }
        }

        // Chuyển đến trang đăng ký
        binding.loginSignupTextView.setOnClickListener {
            (activity as? LoginSignupActivity)?.loadFragment(SignupFragment())
        }

        return binding.root
    }

    // Kiểm tra thông tin đăng nhập
    private fun checkCredentials(email: String, password: String): Boolean {
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
    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()

                    // Lưu thông tin đăng nhập nếu chọn "Remember Me"
                    if (binding.loginRemSwitch.isChecked) {
                        saveLoginInfo(email, password)
                    } else {
                        clearLoginInfo() // Xóa thông tin đăng nhập nếu "Remember Me" không được chọn
                    }

                    // Chuyển tới MainActivity
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish() // Để ngăn quay lại màn hình đăng nhập
                } else {
                    binding.loginErrorTextView.text = "Đăng nhập thất bại: ${task.exception?.message}"
                    binding.loginErrorTextView.visibility = View.VISIBLE
                }
            }
    }

    // Lưu thông tin đăng nhập
    private fun saveLoginInfo(email: String, password: String) {
        val sharedPreferences = activity?.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString("email", email)
        editor?.putString("password", password)
        editor?.putBoolean("switch_remember_text", true)
        editor?.apply()
    }

    // Tải thông tin đăng nhập đã lưu
    private fun loadLoginInfo() {
        val sharedPreferences = activity?.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val rememberMe = sharedPreferences?.getBoolean("switch_remember_text", false) ?: false

        if (rememberMe) {
            val savedEmail = sharedPreferences?.getString("email", "")
            val savedPassword = sharedPreferences?.getString("password", "")
            binding.loginEmailEditText.setText(savedEmail)
            binding.loginPasswordEditText.setText(savedPassword)
            binding.loginRemSwitch.isChecked = true // Đặt trạng thái "Remember Me" là được chọn
        }
    }

    // Xóa thông tin đăng nhập đã lưu
    private fun clearLoginInfo() {
        val sharedPreferences = activity?.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.clear()
        editor?.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

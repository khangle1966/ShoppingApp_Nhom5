package com.nhom5.shoppingapp.ui.loginSignup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nhom5.shoppingapp.R
import androidx.fragment.app.Fragment

class LoginSignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signup)

        // Tải LoginFragment mặc định
        if (savedInstanceState == null) {
            loadFragment(LoginFragment())
        }
    }

    // Hàm thay đổi fragment
    fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}

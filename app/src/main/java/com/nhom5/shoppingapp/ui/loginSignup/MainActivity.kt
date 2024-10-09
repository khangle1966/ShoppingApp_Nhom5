package com.nhom5.shoppingapp.ui.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nhom5.shoppingapp.R

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up Navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.home_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.home_bottom_navigation)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        // Ẩn BottomNavigationView trên các màn hình cụ thể như LoginFragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.signupFragment -> {
                    // Ẩn BottomNavigationView
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    // Hiển thị BottomNavigationView trên các màn hình khác
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }
}

package com.nhom5.shoppingapp.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.nhom5.shoppingapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

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
    }
}

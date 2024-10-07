package com.nhom5.shoppingapp.ui.home

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.R
import com.nhom5.shoppingapp.databinding.FragmentHomeBinding
import com.nhom5.shoppingapp.model.Product
import com.nhom5.shoppingapp.ui.adapters.ProductAdapter
import com.nhom5.shoppingapp.ui.account.AccountFragment
import com.nhom5.shoppingapp.ui.loginSignup.LoginFragment
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment(R.layout.fragment_home), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var productAdapter: ProductAdapter
    private val productList = mutableListOf<Product>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)
        drawerLayout = binding.drawerLayout

        toggle = ActionBarDrawerToggle(
            activity as AppCompatActivity?,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Thiết lập NavigationView
        binding.navView.setNavigationItemSelectedListener(this)

        // Thiết lập RecyclerView cho sản phẩm
        productAdapter = ProductAdapter(productList)
        binding.productsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }

        // Lấy dữ liệu từ Firestore
        loadProducts()
    }

    private fun loadProducts() {
        firestore.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                productList.clear()
                for (document in documents) {
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
                }
                productAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi nếu cần
            }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_account -> {
                (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, AccountFragment())
                    .addToBackStack(null)
                    .commit()
            }
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, LoginFragment())
                    .commit()
            }
        }
        drawerLayout.closeDrawers()
        return true
    }
}

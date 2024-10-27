package com.nhom5.shoppingapp.ui.account

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.R
import com.nhom5.shoppingapp.databinding.FragmentAccountBinding
import com.nhom5.shoppingapp.model.User

class AccountFragment : Fragment(R.layout.fragment_account) {

    private lateinit var binding: FragmentAccountBinding
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAccountBinding.bind(view)

        // Ẩn nút Admin mặc định
        binding.accountAdminTv.visibility = View.GONE

        // Kiểm tra kiểu người dùng và hiển thị nút Admin nếu userType là admin
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    user?.let {
                        if (it.userType == "admin") {
                            binding.accountAdminTv.visibility = View.VISIBLE
                        }
                    }
                }
        }

        // Sự kiện khi người dùng nhấn nút Sign Out
        binding.accountSignOutTv.setOnClickListener {
            showSignOutDialog()
        }

        // Sự kiện khi người dùng nhấn vào mục Profile
        binding.accountProfileTv.setOnClickListener {
            openProfileFragment()
        }

        // Sự kiện khi người dùng nhấn vào mục Orders
        binding.accountOrdersTv.setOnClickListener {
            openOrdersFragment()
        }

        // Sự kiện khi người dùng nhấn vào mục Admin
        binding.accountAdminTv.setOnClickListener {
            openAdminFragment()
        }
    }

    // Hiển thị dialog xác nhận đăng xuất
    private fun showSignOutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign Out?")
            .setMessage("Bạn có chắc muốn đăng xuất khỏi ứng dụng không?")
            .setPositiveButton("SIGN OUT") { dialog, _ ->
                signOutUser()
                dialog.dismiss()
            }
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Phương thức đăng xuất người dùng
    private fun signOutUser() {
        // Đăng xuất khỏi Firebase
        FirebaseAuth.getInstance().signOut()

        // Điều hướng về LoginFragment và xóa lịch sử điều hướng trước đó
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, true)  // Xóa hết các fragment trước đó khỏi back stack
            .build()

        findNavController().navigate(R.id.action_accountFragment_to_loginFragment, null, navOptions)
    }

    // Điều hướng đến ProfileFragment
    private fun openProfileFragment() {
        findNavController().navigate(R.id.action_accountFragment_to_profileFragment)
    }

    // Điều hướng đến OrdersFragment
    private fun openOrdersFragment() {
        findNavController().navigate(R.id.action_accountFragment_to_ordersFragment)
    }

    // Điều hướng đến AdminFragment
    private fun openAdminFragment() {
        findNavController().navigate(R.id.action_accountFragment_to_adminFragment)
    }

    override fun onStart() {
        super.onStart()

        // Kiểm tra trạng thái đăng nhập
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // Điều hướng đến màn hình đăng nhập nếu chưa đăng nhập
            findNavController().navigate(R.id.action_accountFragment_to_loginFragment)
        }
    }
}

package com.nhom5.shoppingapp.ui.account

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.nhom5.shoppingapp.R
import com.nhom5.shoppingapp.databinding.FragmentAccountBinding

class AccountFragment : Fragment(R.layout.fragment_account) {

    private lateinit var binding: FragmentAccountBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAccountBinding.bind(view)

        // Sự kiện khi người dùng nhấn nút Sign Out
        binding.accountSignOutTv.setOnClickListener {
            showSignOutDialog()
        }
    }

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

    private fun signOutUser() {
        // Đăng xuất khỏi Firebase
        FirebaseAuth.getInstance().signOut()

        // Điều hướng về LoginFragment và xóa lịch sử điều hướng trước đó
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, true)  // Xóa hết các fragment trước đó khỏi back stack
            .build()

        findNavController().navigate(R.id.action_accountFragment_to_loginFragment, null, navOptions)
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

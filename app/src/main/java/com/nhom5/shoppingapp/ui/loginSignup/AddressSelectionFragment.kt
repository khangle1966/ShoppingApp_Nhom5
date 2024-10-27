package com.nhom5.shoppingapp.ui.address

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.databinding.FragmentAddressBinding
import com.nhom5.shoppingapp.model.User
import androidx.navigation.fragment.findNavController
import com.nhom5.shoppingapp.R

class AddressSelectionFragment : Fragment() {

    private lateinit var binding: FragmentAddressBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var addressAdapter: AddressAdapter
    private var addressList = mutableListOf<User>()
    private var selectedAddress: String? = null
    private var totalPrice: Float = 0.0f // Biến lưu trữ tổng giá trị

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nhận giá trị totalPrice từ CartFragment
        totalPrice = arguments?.getFloat("totalPrice") ?: 0.0f

        binding.addressAppBar.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadUserAddress()

        binding.addressNextBtn.setOnClickListener {
            navigateToPayment()
        }
    }

    private fun setupRecyclerView() {
        binding.addressAddressesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        addressAdapter = AddressAdapter(addressList, object : AddressAdapter.AddressActionListener {
            override fun onEdit(updatedAddress: User, position: Int) {
                updateUserAddressInFirestore(updatedAddress, position)
            }

            // Nhận địa chỉ đã chọn từ RecyclerView
            override fun onAddressSelected(address: String) {
                selectedAddress = address
            }
        })
        binding.addressAddressesRecyclerView.adapter = addressAdapter
    }

    private fun loadUserAddress() {
        val user = auth.currentUser
        if (user != null) {
            showLoader()

            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    val userData = document.toObject(User::class.java)
                    userData?.let {
                        addressList.add(it)
                        addressAdapter.notifyDataSetChanged()
                    }
                    hideLoader()
                }
                .addOnFailureListener { e ->
                    hideLoader()
                    Toast.makeText(requireContext(), "Lỗi khi tải địa chỉ: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserAddressInFirestore(user: User, position: Int) {
        val userId = auth.currentUser?.uid ?: return

        showLoader()

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                addressList[position] = user
                addressAdapter.notifyItemChanged(position)

                Toast.makeText(requireContext(), "Địa chỉ đã được cập nhật", Toast.LENGTH_SHORT).show()
                hideLoader()
            }
            .addOnFailureListener { e ->
                hideLoader()
                Toast.makeText(requireContext(), "Lỗi khi cập nhật địa chỉ: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToPayment() {
        if (selectedAddress != null) {
            // Truyền cả selectedAddress và totalPrice sang SelectPaymentFragment
            val action = AddressSelectionFragmentDirections
                .actionAddressSelectionFragmentToSelectPaymentFragment(selectedAddress!!, totalPrice)
            findNavController().navigate(action)
        } else {
            Toast.makeText(requireContext(), "Vui lòng chọn một địa chỉ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoader() {
        binding.loaderLayout.loaderLayout.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        binding.loaderLayout.loaderLayout.visibility = View.GONE
    }
}

package com.nhom5.shoppingapp.ui.address

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nhom5.shoppingapp.databinding.FragmentAddressBinding
import com.nhom5.shoppingapp.model.Address
import com.nhom5.shoppingapp.model.User
import androidx.navigation.fragment.findNavController

class AddressSelectionFragment : Fragment() {

    private lateinit var binding: FragmentAddressBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var address: Address? = null
    private var totalPrice: Float = 0.0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        totalPrice = arguments?.getFloat("totalPrice") ?: 0.0f

        binding.addressAppBar.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Tải dữ liệu Address duy nhất từ Firestore
        loadUserAddress()

        // Xử lý khi nhấn nút "Next"
        binding.addressNextBtn.setOnClickListener {
            val editedName = binding.addressCard.editPersonNameEditText.text.toString()
            val editedPhone = binding.addressCard.editMobileEditText.text.toString()
            val editedAddress = binding.addressCard.editAddressEditText.text.toString()

            if (editedName.isBlank() || editedPhone.isBlank() || editedAddress.isBlank()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Điều hướng sang SelectPaymentFragment
            val action = AddressSelectionFragmentDirections
                .actionAddressSelectionFragmentToSelectPaymentFragment(
                    shippingAddress = editedAddress,
                    customName = editedName,
                    customPhone = editedPhone,
                    totalPrice = totalPrice
                )
            findNavController().navigate(action)
        }
    }

    private fun loadUserAddress() {
        val userId = auth.currentUser?.uid ?: return

        showLoader()

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                user?.let {
                    address = Address(
                        name = user.name,
                        mobile = user.mobile,
                        address = user.address
                    )

                    binding.addressCard.editPersonNameEditText.setText(address?.name)
                    binding.addressCard.editMobileEditText.setText(address?.mobile)
                    binding.addressCard.editAddressEditText.setText(address?.address)
                }
                hideLoader()
            }
            .addOnFailureListener { exception ->
                hideLoader()
                Toast.makeText(requireContext(), "Lỗi khi tải địa chỉ: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoader() {
        binding.loaderLayout.loaderLayout.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        binding.loaderLayout.loaderLayout.visibility = View.GONE
    }
}

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
import com.nhom5.shoppingapp.model.Address
import androidx.navigation.fragment.findNavController
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.nhom5.shoppingapp.R
class AddressSelectionFragment : Fragment() {

    private lateinit var binding: FragmentAddressBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var addressAdapter: AddressAdapter
    private var addressList = mutableListOf<Address>()
    private var selectedAddress: Address? = null
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

        setupRecyclerView()
        loadUserAddresses()

        binding.addressCustomBtn.setOnClickListener {
            showCustomAddressDialog()
        }

        binding.addressNextBtn.setOnClickListener {
            if (selectedAddress != null) {
                navigateToPayment()
            } else {
                Toast.makeText(requireContext(), "Vui lòng chọn một địa chỉ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.addressAddressesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        addressAdapter = AddressAdapter(addressList, object : AddressAdapter.AddressActionListener {
            override fun onEdit(updatedAddress: Address, position: Int) {
                updateAddressInFirestore(updatedAddress, position)
            }

            override fun onAddressSelected(address: Address) {
                selectedAddress = address
            }
            override fun onDelete(address: Address, position: Int) {
                // Hiển thị xác nhận xóa
                AlertDialog.Builder(requireContext())
                    .setTitle("Xóa Địa Chỉ")
                    .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này không?")
                    .setPositiveButton("Xóa") { _, _ ->
                        deleteAddress(address, position)
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        })
        binding.addressAddressesRecyclerView.adapter = addressAdapter
    }
    private fun deleteAddress(address: Address, position: Int) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    // Xóa địa chỉ khỏi danh sách
                    user.addresses.removeAt(position - 1) // Trừ 1 vì địa chỉ mặc định ở vị trí 0

                    // Cập nhật Firestore
                    firestore.collection("users").document(userId).set(user)
                        .addOnSuccessListener {
                            // Cập nhật danh sách địa chỉ trong ứng dụng
                            addressList.removeAt(position)
                            addressAdapter.notifyItemRemoved(position)
                            Toast.makeText(requireContext(), "Địa chỉ đã được xóa.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Lỗi khi xóa địa chỉ: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Lỗi khi truy cập dữ liệu người dùng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserAddresses() {
        val userId = auth.currentUser?.uid ?: return

        showLoader()

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val userData = document.toObject(User::class.java)
                userData?.let { user ->
                    // Thêm địa chỉ mặc định
                    val defaultAddress = Address(
                        name = user.name,
                        mobile = user.mobile,
                        address = user.address
                    )
                    addressList.add(defaultAddress)

                    // Thêm các địa chỉ tùy chỉnh
                    addressList.addAll(user.addresses)

                    addressAdapter.notifyDataSetChanged()
                }
                hideLoader()
            }
            .addOnFailureListener { e ->
                hideLoader()
                Toast.makeText(requireContext(), "Lỗi khi tải địa chỉ: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCustomAddressDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_address, null)
        val nameEditText: EditText = dialogView.findViewById(R.id.edit_person_name_dialog)
        val mobileEditText: EditText = dialogView.findViewById(R.id.edit_mobile_dialog)
        val addressEditText: EditText = dialogView.findViewById(R.id.edit_address_dialog)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Nhập Địa Chỉ Mới")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val customName = nameEditText.text.toString()
                val customPhone = mobileEditText.text.toString()
                val customAddress = addressEditText.text.toString()

                if (customName.isEmpty() || customPhone.isEmpty() || customAddress.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show()
                } else {
                    val newAddress = Address(
                        name = customName,
                        mobile = customPhone,
                        address = customAddress
                    )

                    addressList.add(newAddress)
                    addressAdapter.notifyItemInserted(addressList.size - 1)

                    saveNewAddressToFirestore(newAddress)
                }
            }
            .setNegativeButton("Hủy", null)
            .create()

        dialog.show()
    }

    private fun saveNewAddressToFirestore(newAddress: Address) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    user.addresses.add(newAddress)
                    firestore.collection("users").document(userId).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Địa chỉ mới đã được lưu.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Lỗi khi lưu địa chỉ: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Lỗi khi truy cập dữ liệu người dùng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAddressInFirestore(updatedAddress: Address, position: Int) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    if (position == 0) {
                        // Cập nhật địa chỉ mặc định
                        user.name = updatedAddress.name
                        user.mobile = updatedAddress.mobile
                        user.address = updatedAddress.address
                    } else {
                        user.addresses[position - 1] = updatedAddress
                    }
                    firestore.collection("users").document(userId).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Địa chỉ đã được cập nhật", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Lỗi khi cập nhật địa chỉ: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Lỗi khi truy cập dữ liệu người dùng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToPayment() {
        val action = AddressSelectionFragmentDirections
            .actionAddressSelectionFragmentToSelectPaymentFragment(
                selectedAddress = selectedAddress?.address ?: "",
                totalPrice = totalPrice,
                customName = selectedAddress?.name ?: "",
                customPhone = selectedAddress?.mobile ?: ""
            )
        findNavController().navigate(action)
    }

    private fun showLoader() {
        binding.loaderLayout.loaderLayout.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        binding.loaderLayout.loaderLayout.visibility = View.GONE
    }
}

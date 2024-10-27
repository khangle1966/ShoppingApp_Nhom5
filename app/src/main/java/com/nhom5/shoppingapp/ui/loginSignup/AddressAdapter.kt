package com.nhom5.shoppingapp.ui.address

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nhom5.shoppingapp.databinding.LayoutAddressCardBinding
import com.nhom5.shoppingapp.model.User

class AddressAdapter(
    private val addressList: MutableList<User>,
    private val actionListener: AddressActionListener
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    interface AddressActionListener {
        fun onEdit(updatedAddress: User, position: Int)
        fun onAddressSelected(address: String) // Thêm callback để nhận địa chỉ đã chọn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = LayoutAddressCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = addressList[position]
        holder.bind(address, position)
    }

    override fun getItemCount() = addressList.size

    inner class AddressViewHolder(private val binding: LayoutAddressCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(address: User, position: Int) {
            // Hiển thị thông tin ban đầu bằng TextViews
            binding.addressPersonNameTv.text = address.name
            binding.addressCompleteAddressTv.text = address.address
            binding.addressMobileTv.text = address.mobile

            // Ẩn EditText ban đầu
            binding.editAddressEditText.visibility = View.GONE

            // Xử lý khi người dùng nhấn vào nút chọn địa chỉ
            binding.root.setOnClickListener {
                // Khi người dùng chọn địa chỉ, gọi callback để cập nhật địa chỉ đã chọn
                actionListener.onAddressSelected(address.address)
            }

            // Khi nhấn Edit, chỉ cho phép chỉnh sửa địa chỉ
            binding.addressEditBtn.setOnClickListener {
                if (binding.addressEditBtn.text == "Edit") {
                    enableEditing(address)
                } else {
                    // Khi nhấn Save, cập nhật địa chỉ mới
                    val updatedAddress = address.copy(
                        address = binding.editAddressEditText.text.toString()
                    )

                    // Cập nhật trong Firestore và danh sách ngay lập tức
                    actionListener.onEdit(updatedAddress, position)

                    // Cập nhật trực tiếp trong giao diện mà không cần reload
                    binding.addressCompleteAddressTv.text = updatedAddress.address
                    disableEditing()
                }
            }
        }

        private fun enableEditing(address: User) {
            // Ẩn TextView của địa chỉ và hiển thị EditText
            binding.addressCompleteAddressTv.visibility = View.GONE
            binding.editAddressEditText.visibility = View.VISIBLE

            binding.editAddressEditText.setText(address.address)

            // Đổi nút Edit thành Save
            binding.addressEditBtn.text = "Save"
        }

        private fun disableEditing() {
            // Hiển thị lại TextView và ẩn EditText
            binding.addressCompleteAddressTv.visibility = View.VISIBLE
            binding.editAddressEditText.visibility = View.GONE

            // Đổi nút Save thành Edit
            binding.addressEditBtn.text = "Edit"
        }
    }
}

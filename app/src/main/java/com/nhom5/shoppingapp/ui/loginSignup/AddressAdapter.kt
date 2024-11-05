package com.nhom5.shoppingapp.ui.address

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nhom5.shoppingapp.databinding.LayoutAddressCardBinding
import com.nhom5.shoppingapp.model.Address

class AddressAdapter(
    private val addressList: MutableList<Address>,
    private val actionListener: AddressActionListener
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    interface AddressActionListener {
        fun onEdit(updatedAddress: Address, position: Int)
        fun onAddressSelected(address: Address)
        fun onDelete(address: Address, position: Int)

    }

    private var selectedPosition = -1

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

        fun bind(address: Address, position: Int) {
            binding.addressPersonNameTv.text = address.name
            binding.addressCompleteAddressTv.text = address.address
            binding.addressMobileTv.text = address.mobile
            val isDefaultAddress = position == 0
            binding.addressDeleteBtn.visibility = if (isDefaultAddress) View.GONE else View.VISIBLE

            binding.addressCard.isChecked = position == selectedPosition

            binding.root.setOnClickListener {
                selectedPosition = adapterPosition
                notifyDataSetChanged()
                actionListener.onAddressSelected(address)
            }

            // Ẩn các trường EditText ban đầu
            binding.editPersonNameEditText.visibility = View.GONE
            binding.editAddressEditText.visibility = View.GONE
            binding.editMobileEditText.visibility = View.GONE
            binding.addressDeleteBtn.setOnClickListener {
                actionListener.onDelete(address, position)
            }

            binding.addressEditBtn.setOnClickListener {
                if (binding.addressEditBtn.text == "Edit") {
                    enableEditing(address)
                } else {
                    saveUpdatedAddress(address, position)
                }
            }
        }

        private fun enableEditing(address: Address) {
            binding.editPersonNameEditText.apply {
                visibility = View.VISIBLE
                setText(address.name)
            }
            binding.editAddressEditText.apply {
                visibility = View.VISIBLE
                setText(address.address)
            }
            binding.editMobileEditText.apply {
                visibility = View.VISIBLE
                setText(address.mobile)
            }

            binding.addressPersonNameTv.visibility = View.GONE
            binding.addressCompleteAddressTv.visibility = View.GONE
            binding.addressMobileTv.visibility = View.GONE

            binding.addressEditBtn.text = "Save"
        }

        private fun saveUpdatedAddress(address: Address, position: Int) {
            val updatedAddress = address.copy(
                name = binding.editPersonNameEditText.text.toString(),
                address = binding.editAddressEditText.text.toString(),
                mobile = binding.editMobileEditText.text.toString()
            )

            actionListener.onEdit(updatedAddress, position)

            binding.addressPersonNameTv.apply {
                text = updatedAddress.name
                visibility = View.VISIBLE
            }
            binding.addressCompleteAddressTv.apply {
                text = updatedAddress.address
                visibility = View.VISIBLE
            }
            binding.addressMobileTv.apply {
                text = updatedAddress.mobile
                visibility = View.VISIBLE
            }

            binding.editPersonNameEditText.visibility = View.GONE
            binding.editAddressEditText.visibility = View.GONE
            binding.editMobileEditText.visibility = View.GONE

            binding.addressEditBtn.text = "Edit"
        }
    }
}

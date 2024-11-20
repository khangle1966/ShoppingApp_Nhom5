package com.nhom5.shoppingapp.ui.address

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nhom5.shoppingapp.databinding.LayoutAddressCardBinding
import com.nhom5.shoppingapp.model.Address

class AddressAdapter(
    private val addressList: MutableList<Address>
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = LayoutAddressCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = addressList[position]
        holder.bind(address)
    }

    override fun getItemCount() = addressList.size

    inner class AddressViewHolder(private val binding: LayoutAddressCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(address: Address) {
            binding.editPersonNameEditText.setText(address.name)
            binding.editMobileEditText.setText(address.mobile)
            binding.editAddressEditText.setText(address.address)
        }

        fun getUpdatedAddress(): Address {
            return Address(
                name = binding.editPersonNameEditText.text.toString(),
                mobile = binding.editMobileEditText.text.toString(),
                address = binding.editAddressEditText.text.toString()
            )
        }
    }

    fun getEditedAddresses(): List<Address> {
        return (0 until itemCount).map { position ->
            notifyItemChanged(position)
            addressList[position].copy(
                name = addressList[position].name,
                mobile = addressList[position].mobile,
                address = addressList[position].address
            )
        }
    }
}

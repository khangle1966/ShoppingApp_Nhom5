package com.nhom5.shoppingapp.ui.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nhom5.shoppingapp.databinding.LayoutListItemBinding

class PaymentAdapter(private val paymentMethods: List<String>) :
    RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = LayoutListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val paymentMethod = paymentMethods[position]
        holder.bind(paymentMethod, position)
    }

    override fun getItemCount() = paymentMethods.size

    fun getSelectedPayment(): String? {
        return if (selectedPosition != -1) {
            paymentMethods[selectedPosition]
        } else {
            null
        }
    }

    inner class PaymentViewHolder(private val binding: LayoutListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(paymentMethod: String, position: Int) {
            binding.itemTitleTextView.text = paymentMethod

            // Highlight the selected payment method
            binding.itemCard.isChecked = position == selectedPosition

            binding.itemCard.setOnClickListener {
                selectedPosition = adapterPosition
                notifyDataSetChanged()
            }
        }
    }
}

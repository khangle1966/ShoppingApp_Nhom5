package com.nhom5.shoppingapp.ui.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nhom5.shoppingapp.databinding.LayoutOrderSummaryCardBinding
import com.nhom5.shoppingapp.model.Order

class OrdersAdapter(
    private var orders: List<Order>,
    private val onOrderClick: (String) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = LayoutOrderSummaryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    override fun getItemCount() = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(private val binding: LayoutOrderSummaryCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.orderSummaryIdTv.text = order.orderId
            binding.orderSummaryDateTv.text = order.orderDate.toString()
            binding.orderSummaryStatusValueTv.text = order.status
            binding.orderSummaryItemsCountTv.text = "${order.items.size} items purchased"
            binding.orderSummaryTotalAmountTv.text = "$${order.totalPrice}"

            // Khi người dùng nhấn vào đơn hàng
            binding.root.setOnClickListener {
                onOrderClick(order.orderId)
            }
        }
    }
}

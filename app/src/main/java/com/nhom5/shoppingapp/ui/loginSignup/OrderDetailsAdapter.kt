package com.nhom5.shoppingapp.ui.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nhom5.shoppingapp.databinding.CartListItemBinding
import com.nhom5.shoppingapp.model.CartItem

class OrderDetailsAdapter(
    private var items: List<CartItem>
) : RecyclerView.Adapter<OrderDetailsAdapter.OrderDetailsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailsViewHolder {
        val binding = CartListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderDetailsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderDetailsViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class OrderDetailsViewHolder(private val binding: CartListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItem) {

            binding.cartProductTitleTv.text = item.name
            binding.cartProductPriceTv.text = "$${item.price}"
            Glide.with(binding.root.context)
                .load(item.imageUrl)
                .into(binding.productImageView)
            binding.cartProductSizeTv.text = "${item.selectedSize}"
            binding.cartProductColorTv.text = "${item.selectedColor}"
            binding.cartProductQuantityTextView.text = "${item.quantity}"

            binding.cartProductLikeBtn.visibility = View.GONE
            binding.cartProductDeleteBtn.visibility = View.GONE
            binding.cartProductMinusBtn.visibility = View.GONE
            binding.cartProductPlusBtn.visibility=View.GONE
            // Set other views as necessary
        }
    }
}
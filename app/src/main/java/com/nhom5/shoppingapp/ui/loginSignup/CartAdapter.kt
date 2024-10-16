package com.nhom5.shoppingapp.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nhom5.shoppingapp.databinding.CartListItemBinding
import com.nhom5.shoppingapp.model.CartItem

class CartAdapter(
    private var cartItems: List<CartItem>,
    private val actionListener: CartItemActionListener
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    fun updateCartItems(newItems: List<CartItem>) {
        cartItems = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount() = cartItems.size

    inner class CartViewHolder(private val binding: CartListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cartItem: CartItem) {
            binding.cartProductTitleTv.text = cartItem.name
            binding.cartProductPriceTv.text = "$${cartItem.price}"
            binding.cartProductQuantityTextView.text = cartItem.quantity.toString()

            // Tải hình ảnh bằng Glide
            Glide.with(binding.root.context)
                .load(cartItem.imageUrl)
                .into(binding.productImageView)

            // Xử lý sự kiện khi nhấn nút xóa
            binding.cartProductDeleteBtn.setOnClickListener {
                actionListener.onDelete(cartItem)
            }

            // Xử lý sự kiện khi nhấn nút tăng số lượng
            binding.cartProductPlusBtn.setOnClickListener {
                actionListener.onIncrement(cartItem)
            }

            // Xử lý sự kiện khi nhấn nút giảm số lượng
            binding.cartProductMinusBtn.setOnClickListener {
                actionListener.onDecrement(cartItem)
            }
        }
    }
}

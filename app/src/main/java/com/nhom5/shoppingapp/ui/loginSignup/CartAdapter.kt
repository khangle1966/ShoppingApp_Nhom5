package com.nhom5.shoppingapp.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nhom5.shoppingapp.databinding.CartListItemBinding
import com.nhom5.shoppingapp.model.CartItem
import android.view.View


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
            // Hiển thị tên sản phẩm
            binding.cartProductTitleTv.text = cartItem.name

            // Hiển thị size (nếu có)
            val sizes = cartItem.size.joinToString(", ") // Convert list to comma-separated string
            if (sizes.isNotEmpty()) {
                binding.cartProductSizeTv.text = "Size: $sizes"
                binding.cartProductSizeTv.visibility = View.VISIBLE
            } else {
                binding.cartProductSizeTv.visibility = View.GONE
            }

            // Hiển thị màu (nếu có)
            val colors = cartItem.color.joinToString(", ")
            if (colors.isNotEmpty()) {
                binding.cartProductColorTv.text = "Color: $colors"
                binding.cartProductColorTv.visibility = View.VISIBLE
            } else {
                binding.cartProductColorTv.visibility = View.GONE
            }

            // Hiển thị giá sản phẩm
            binding.cartProductPriceTv.text = "$${cartItem.price}"

            // Hiển thị số lượng sản phẩm
            binding.cartProductQuantityTextView.text = cartItem.quantity.toString()

            // Tải hình ảnh sản phẩm
            Glide.with(binding.root.context)
                .load(cartItem.imageUrl)
                .into(binding.productImageView)

            // Xử lý sự kiện khi xóa sản phẩm
            binding.cartProductDeleteBtn.setOnClickListener {
                actionListener.onDelete(cartItem)
            }

            // Xử lý sự kiện khi tăng số lượng
            binding.cartProductPlusBtn.setOnClickListener {
                actionListener.onIncrement(cartItem)
            }

            // Xử lý sự kiện khi giảm số lượng
            binding.cartProductMinusBtn.setOnClickListener {
                actionListener.onDecrement(cartItem)
            }
        }
    }
}

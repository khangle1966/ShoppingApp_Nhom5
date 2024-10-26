package com.nhom5.shoppingapp.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nhom5.shoppingapp.databinding.CartListItemBinding
import com.nhom5.shoppingapp.model.CartItem
import android.view.View
import android.util.Log


class CartAdapter(
    private var cartItems: List<CartItem>,
    private val actionListener: CartItemActionListener
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // Hàm để cập nhật danh sách cartItems và làm mới RecyclerView
    fun updateCartItems(newItems: List<CartItem>) {
        cartItems = newItems
        // Kiểm tra danh sách mới
        for (item in cartItems) {
            Log.d("CartDebug", "CartItem in Adapter: Size = ${item.selectedSize}, Color = ${item.selectedColor}")
        }
        notifyDataSetChanged()
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding =
            CartListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount() = cartItems.size

    inner class CartViewHolder(private val binding: CartListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cartItem: CartItem) {
            binding.cartProductTitleTv.text = cartItem.name
            binding.cartProductPriceTv.text = "Unit Price: $${cartItem.price}"  // Thêm "Unit Price: " trước giá
            binding.cartProductQuantityTextView.text = cartItem.quantity.toString()

            // Hiển thị size đã chọn
            if (cartItem.selectedSize.isNotEmpty()) {
                binding.cartProductSizeTv.text = "Size: ${cartItem.selectedSize}"
                binding.cartProductSizeTv.visibility = View.VISIBLE
            } else {
                binding.cartProductSizeTv.visibility = View.GONE
            }

            // Hiển thị color đã chọn
            if (cartItem.selectedColor.isNotEmpty()) {
                binding.cartProductColorTv.text = "Color: ${cartItem.selectedColor}"
                binding.cartProductColorTv.visibility = View.VISIBLE
            } else {
                binding.cartProductColorTv.visibility = View.GONE
            }

            Glide.with(binding.root.context)
                .load(cartItem.imageUrl)
                .into(binding.productImageView)

            binding.cartProductDeleteBtn.setOnClickListener {
                actionListener.onDelete(cartItem)
            }

            binding.cartProductPlusBtn.setOnClickListener {
                actionListener.onIncrement(cartItem)
            }

            binding.cartProductMinusBtn.setOnClickListener {
                actionListener.onDecrement(cartItem)
            }
        }
    }

}



package com.nhom5.shoppingapp.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nhom5.shoppingapp.databinding.CartListItemBinding
import com.nhom5.shoppingapp.model.CartItem
import android.view.View
import android.util.Log
import java.text.NumberFormat
import java.util.Locale

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
            val numberFormat = NumberFormat.getNumberInstance(Locale.US) // Định dạng kiểu US (dấu phẩy ngăn cách)

            binding.cartProductTitleTv.text = cartItem.name
            binding.cartProductPriceTv.text = "Unit Price: ${numberFormat.format(cartItem.price)}$"

            binding.cartProductQuantityTextView.text = cartItem.quantity.toString()

            // Tính toán và hiển thị Amount
            val amount = cartItem.price * cartItem.quantity
            binding.cartProductAmountTv.text = "Amount: ${numberFormat.format(amount)}$"


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



package com.nhom5.shoppingapp.ui.cart

import com.nhom5.shoppingapp.model.CartItem

interface CartItemActionListener {
    fun onDelete(cartItem: CartItem)
    fun onIncrement(cartItem: CartItem)
    fun onDecrement(cartItem: CartItem)
}

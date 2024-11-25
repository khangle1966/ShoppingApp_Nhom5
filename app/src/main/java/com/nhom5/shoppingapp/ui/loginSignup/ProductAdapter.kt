package com.nhom5.shoppingapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nhom5.shoppingapp.databinding.ProductsListItemBinding
import com.nhom5.shoppingapp.model.Product
import com.nhom5.shoppingapp.R
import java.text.NumberFormat
import java.util.Locale
import android.view.View

class ProductAdapter(
    private val productList: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ProductsListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductsListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.binding.apply {
            val numberFormat = NumberFormat.getNumberInstance(Locale.US)

            productNameTv.text = product.name
            productPriceTv.text = "${numberFormat.format(product.price)}$"
            productActualPriceTv.text = "${numberFormat.format(product.actualPrice)}$"
            productOfferValueTv.text = "-${product.offerPercentage}%"
            productRatingBar.rating = product.rating

            if (product.imageUrl.isNotEmpty()) {
                Glide.with(productImageView.context)
                    .load(product.imageUrl[0])
                    .into(productImageView)
            } else {
                productImageView.setImageResource(R.drawable.heart_icon_drawable)
            }

            if (product.status == "Disabled") {
                // Set overlay and disable interaction
                soldOutOverlay.visibility = View.VISIBLE
                productCard.isClickable = false
                productCard.isEnabled = false
            } else {
                // Remove overlay and enable interaction
                soldOutOverlay.visibility = View.GONE
                productCard.isClickable = true
                productCard.isEnabled = true
            }

            root.setOnClickListener {
                if (product.status == "Available") {
                    onItemClick(product)
                }
            }
        }
    }



    override fun getItemCount() = productList.size
}

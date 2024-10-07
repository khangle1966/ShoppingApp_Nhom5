package com.nhom5.shoppingapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nhom5.shoppingapp.databinding.ProductsListItemBinding
import com.nhom5.shoppingapp.model.Product

class ProductAdapter(private val productList: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

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
            productNameTv.text = product.name
            productPriceTv.text = "$${product.price}"
            productActualPriceTv.text = "$${product.actualPrice}"
            productOfferValueTv.text = "-${product.offerPercentage}%"
            productRatingBar.rating = product.rating

            Glide.with(productImageView.context)
                .load(product.imageUrl)
                .into(productImageView)
        }
    }

    override fun getItemCount() = productList.size
}

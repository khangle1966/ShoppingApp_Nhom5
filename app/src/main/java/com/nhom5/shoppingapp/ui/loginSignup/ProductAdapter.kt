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
            val numberFormat = NumberFormat.getNumberInstance(Locale.US) // Định dạng số kiểu US

            productNameTv.text = product.name
            productPriceTv.text = "${numberFormat.format(product.price)}$"
            productActualPriceTv.text = "${numberFormat.format(product.actualPrice)}$"
            productOfferValueTv.text = "-${product.offerPercentage}%"
            productRatingBar.rating = product.rating

            // Kiểm tra xem mảng imageUrl có ít nhất 1 hình ảnh không
            if (product.imageUrl.isNotEmpty()) {
                // Lấy và hiển thị hình ảnh đầu tiên trong mảng imageUrl
                Glide.with(productImageView.context)
                    .load(product.imageUrl[0])  // Hiển thị hình ảnh đầu tiên
                    .into(productImageView)
            } else {
                // Nếu không có hình ảnh, có thể hiển thị hình ảnh mặc định
                productImageView.setImageResource(R.drawable.heart_icon_drawable)
            }

            // Thêm sự kiện click vào toàn bộ item
            root.setOnClickListener {
                onItemClick(product)
            }
        }
    }

    override fun getItemCount() = productList.size
}

package com.nhom5.shoppingapp.ui.adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nhom5.shoppingapp.R
import com.nhom5.shoppingapp.databinding.ProductsListItemBinding
import com.nhom5.shoppingapp.model.Product

class AdminProductAdapter(
    private val productList: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder>() {

    inner class AdminProductViewHolder(private val binding: ProductsListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.productNameTv.text = product.name
            binding.productPriceTv.text = "$${product.price}"

            if (product.imageUrl.isNotEmpty()) {
                // Lấy và hiển thị hình ảnh đầu tiên trong mảng imageUrl
                Glide.with(binding.productImageView.context)
                    .load(product.imageUrl[0])  // Hiển thị hình ảnh đầu tiên
                    .into(binding.productImageView)
            } else {
                // Nếu không có hình ảnh, có thể hiển thị hình ảnh mặc định
                binding.productImageView.setImageResource(R.drawable.heart_icon_drawable)
            }
            binding.productAddToCartButton.visibility = View.GONE
            binding.productDeleteButton.visibility = View.VISIBLE

            binding.productCard.setOnClickListener { onEditClick(product) }
            binding.productDeleteButton.setOnClickListener { onDeleteClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminProductViewHolder {
        val binding = ProductsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdminProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}

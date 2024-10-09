package com.nhom5.shoppingapp.ui.product

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.nhom5.shoppingapp.R

class ImageAdapter(
    private val images: MutableList<Uri>,
    private val onRemoveClick: (Uri) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.add_images_image_view)
        val removeButton: ImageView = itemView.findViewById(R.id.add_img_close_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_images_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = images[position]

        // Hiển thị ảnh đã chọn
        holder.imageView.setImageURI(imageUri)

        // Xử lý sự kiện khi nhấn nút xóa ảnh
        holder.removeButton.setOnClickListener {
            onRemoveClick(imageUri)
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    // Thêm ảnh vào danh sách và cập nhật RecyclerView
    fun addImage(imageUri: Uri) {
        images.add(imageUri)
        notifyItemInserted(images.size - 1)
    }

    // Xóa ảnh khỏi danh sách và cập nhật RecyclerView
    fun removeImage(imageUri: Uri) {
        val position = images.indexOf(imageUri)
        if (position != -1) {
            images.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}

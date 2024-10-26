// File DashedLineView.kt created 
package com.nhom5.shoppingapp.ui.customviews
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.nhom5.shoppingapp.R

class DashedLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.line_color) // Đặt màu cho đường nét đứt
        strokeWidth = 2f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f) // Tạo nét đứt với độ dài và khoảng cách
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val startX = 0f
        val stopX = width.toFloat()
        val centerY = height / 2f
        canvas.drawLine(startX, centerY, stopX, centerY, paint)
    }
}

package com.nhom5.shoppingapp.ui.admin.RevenueStatsFragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.nhom5.shoppingapp.R
import com.nhom5.shoppingapp.model.Order
import java.text.SimpleDateFormat
import java.util.*
import java.text.DecimalFormat

class RevenueStatsFragment : Fragment() {

    private lateinit var graphView: GraphView
    private lateinit var yearSpinner: Spinner
    private val orders = mutableListOf<Order>()

    // Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    private val TAG = "RevenueStatsFragment"  // Thêm TAG để log

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = inflater.inflate(R.layout.fragment_revenue_stats, container, false)

        // Khởi tạo GraphView và Spinner
        graphView = binding.findViewById(R.id.revenue_graph)
        yearSpinner = binding.findViewById(R.id.year_spinner)

        // Thiết lập Spinner cho năm
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear).toList() // 5 năm gần nhất
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter

        // Lắng nghe sự thay đổi của Spinner
        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedYear = years[position]
                Log.d(TAG, "Selected Year: $selectedYear")  // Log năm đã chọn
                loadOrders(selectedYear)  // Tải đơn hàng cho năm đã chọn
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        // Lấy đơn hàng trong Firestore
        loadOrders(currentYear)

        return binding
    }

    private fun loadOrders(year: Int) {
        Log.d(TAG, "Loading orders for year: $year")  // Log khi bắt đầu tải đơn hàng
        firestore.collection("orders")
            .get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "Orders fetched: ${result.size()}")  // Kiểm tra số lượng đơn hàng trả về
                orders.clear()
                for (document in result) {
                    val order = document.toObject(Order::class.java)
                    val orderDate = order.orderDate  // Ví dụ: "05 November 2024"
                    val orderYear = extractYearFromDate(orderDate)
                    if (orderYear == year) {
                        orders.add(order)
                    }
                }
                updateChart()  // Cập nhật biểu đồ với dữ liệu đã tải
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching orders: ", exception)
            }
    }

    private fun updateChart() {
        val monthlyRevenue = calculateMonthlyRevenue(orders)

        // Log doanh thu hàng tháng
        Log.d(TAG, "Monthly Revenue: $monthlyRevenue")

        // Xóa các series cũ trong biểu đồ
        graphView.removeAllSeries()

        // Tạo dữ liệu cho GraphView (biểu đồ cột)
        val series = BarGraphSeries<DataPoint>()

        monthlyRevenue.forEachIndexed { index, revenue ->
            // Convert revenue to Double if it's necessary
            series.appendData(DataPoint((index + 1).toDouble(), revenue), true, 12)
        }

        // Thêm series vào GraphView
        graphView.addSeries(series)

        // Thiết lập các thuộc tính cho GraphView (có thể tùy chỉnh)
        graphView.gridLabelRenderer.isHorizontalLabelsVisible = true
        graphView.gridLabelRenderer.isVerticalLabelsVisible = true
        graphView.gridLabelRenderer.horizontalLabelsColor = Color.BLACK
        graphView.gridLabelRenderer.verticalLabelsColor = Color.BLACK
        graphView.gridLabelRenderer.setHorizontalLabelsAngle(45)  // Xoay nhãn tháng để dễ đọc
        graphView.viewport.isScrollable = true  // Cho phép cuộn
        graphView.viewport.setScrollableY(true)
        graphView.viewport.isXAxisBoundsManual = true  // Cấu hình thủ công cho trục X
        graphView.viewport.setMinX(1.0)  // Thiết lập min cho trục X (tháng 1)
        graphView.viewport.setMaxX(12.0) // Thiết lập max cho trục X (tháng 12)
        graphView.viewport.isYAxisBoundsManual = true  // Cấu hình thủ công cho trục Y
        graphView.viewport.setMinY(0.0)  // Thiết lập min cho trục Y (doanh thu)
        graphView.viewport.setMaxY(monthlyRevenue.maxOrNull()?.times(1.2) ?: 1000.0) // Tăng một chút để đảm bảo có không gian hiển thị

        // Thiết lập màu sắc cho các cột trong series
        series.setValueDependentColor { data ->
            Color.rgb(
                (data.x.toInt() * 20) % 255, // Thay đổi màu sắc để có sự đa dạng
                (Math.abs(data.y) * 10).toInt() % 255,
                (data.y.toInt() * 30) % 255
            )
        }

        // Cài đặt spacing giữa các cột bar
        series.spacing = 30

        // Hiển thị giá trị trên đỉnh của các cột bar
        series.isDrawValuesOnTop = true
        series.valuesOnTopColor = Color.RED
        series.valuesOnTopSize = 50f // Kích thước của giá trị hiển thị trên đỉnh (tuỳ chỉnh)

        // Tùy chỉnh các thuộc tính cho GraphView
        graphView.gridLabelRenderer.verticalLabelsColor = Color.BLACK

        // Cập nhật nhãn trục X là tên các tháng

        // Cập nhật nhãn trục X với tháng
        graphView.gridLabelRenderer.horizontalAxisTitle = "Tháng"

        // Đảm bảo rằng GraphView có thể cuộn được
        graphView.viewport.isScrollable = true
        graphView.viewport.setScrollableY(true)
    }




    private fun calculateMonthlyRevenue(orders: List<Order>): List<Double> {
        val monthlyRevenue = MutableList(12) { 0.0 }

        // SimpleDateFormat để phân tích chuỗi "05 November 2024"
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)

        // Định dạng số để cộng doanh thu
        val decimalFormat = DecimalFormat("#.##")  // Giới hạn đến 2 chữ số thập phân

        // Lấy doanh thu cho từng tháng từ orderDate là chuỗi "dd MMMM yyyy"
        orders.forEach { order ->
            try {
                // Chuyển đổi chuỗi thành đối tượng Date
                val date = dateFormat.parse(order.orderDate)

                // Lấy tháng từ đối tượng Date (tháng bắt đầu từ 0, nên không cần trừ 1)
                val calendar = Calendar.getInstance()
                calendar.time = date
                val month = calendar.get(Calendar.MONTH) // Lấy tháng từ 0 đến 11

                // Cộng doanh thu vào tháng tương ứng, đảm bảo doanh thu được làm tròn
                val formattedRevenue = decimalFormat.format(order.totalPrice.toDouble()).toDouble()
                monthlyRevenue[month] += formattedRevenue

            } catch (e: Exception) {
                e.printStackTrace() // Nếu không thể phân tích chuỗi, in lỗi ra log
            }
        }

        return monthlyRevenue
    }


    // Phương thức để lấy năm từ chuỗi orderDate
    private fun extractYearFromDate(orderDate: String): Int {
        // Kiểm tra nếu chuỗi ngày rỗng hoặc null
        if (orderDate.isNullOrEmpty()) {
            Log.e(TAG, "Invalid or empty order date: $orderDate")
            return 0 // Trả về giá trị mặc định nếu ngày không hợp lệ
        }

        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
        return try {
            val date = dateFormat.parse(orderDate)
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.get(Calendar.YEAR) // Trả về năm từ ngày
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date: $orderDate", e)
            0 // Trả về giá trị mặc định nếu không thể phân tích ngày
        }
    }
}

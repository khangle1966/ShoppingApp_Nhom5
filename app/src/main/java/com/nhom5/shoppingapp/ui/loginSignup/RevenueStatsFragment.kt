package com.nhom5.shoppingapp.ui.admin.RevenueStatsFragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.nhom5.shoppingapp.R
import java.text.SimpleDateFormat
import java.util.*

class RevenueStatsFragment : Fragment(R.layout.fragment_revenue_stats) {

    private lateinit var selectDateButton: Button
    private lateinit var dailyGraphView: GraphView
    private lateinit var monthlyGraphView: GraphView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectDateButton = view.findViewById(R.id.selectDateButton)
        dailyGraphView = view.findViewById(R.id.dailyGraphView)
        monthlyGraphView = view.findViewById(R.id.monthlyGraphView)

        selectDateButton.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                updateGraphs(selectedDate.time)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun updateGraphs(selectedDate: Date) {
        getDailyRevenueData(selectedDate) { dailyData ->
            updateDailyGraph(dailyData, selectedDate)
        }

        getMonthlyRevenueData(selectedDate) { monthlyData ->
            updateMonthlyGraph(monthlyData, selectedDate)
        }
    }

    private fun updateDailyGraph(dailyData: Map<Date, Double>, selectedDate: Date) {
        Log.d("DailyGraph", "Received dailyData: $dailyData")

        val dailySeries = LineGraphSeries<DataPoint>()
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

        val labels = mutableListOf<String>()
        var xValue = 0.0
        dailyData.forEach { (date, revenue) ->
            Log.d("DailyGraph", "Processing date: ${dateFormat.format(date)}, revenue: $revenue")

            dailySeries.appendData(DataPoint(xValue, revenue), true, dailyData.size)
            labels.add(dateFormat.format(date))
            xValue++
        }

        dailyGraphView.removeAllSeries()
        dailyGraphView.addSeries(dailySeries)

        Log.d("DailyGraph", "Labels for X-axis: $labels")

        // Cấu hình nhãn cho trục X
        val staticLabelsFormatter = StaticLabelsFormatter(dailyGraphView)
        staticLabelsFormatter.setHorizontalLabels(labels.toTypedArray())
        dailyGraphView.gridLabelRenderer.labelFormatter = staticLabelsFormatter
        dailyGraphView.gridLabelRenderer.textSize = 30f
        dailyGraphView.gridLabelRenderer.numHorizontalLabels = labels.size

        // Hiển thị các điểm dữ liệu
        dailySeries.isDrawDataPoints = true
        dailySeries.dataPointsRadius = 10f

        // Vẽ nhãn trực tiếp trên các điểm
        dailySeries.setOnDataPointTapListener { _, dataPoint ->
            val revenue = "%.2f".format(dataPoint.y)
            val date = labels[dataPoint.x.toInt()]
            Toast.makeText(requireContext(), "Ngày: $date\nDoanh thu: $revenue", Toast.LENGTH_SHORT).show()
        }

        // Đặt tiêu đề
        val selectedDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dailyGraphView.title = "Doanh thu theo ngày kể từ ngày ${selectedDateFormat.format(selectedDate)}"
    }



    private fun updateMonthlyGraph(monthlyData: Map<Date, Double>, selectedDate: Date) {
        val monthlySeries = LineGraphSeries<DataPoint>()
        val dateFormat = SimpleDateFormat("MM/yy", Locale.getDefault())

        val labels = mutableListOf<String>()
        var xValue = 0.0

        monthlyData.forEach { (date, revenue) ->
            monthlySeries.appendData(DataPoint(xValue, revenue), true, monthlyData.size)
            labels.add(dateFormat.format(date))
            xValue++
        }

        monthlyGraphView.removeAllSeries()
        monthlyGraphView.addSeries(monthlySeries)

        // Đặt nhãn cho trục X
        val staticLabelsFormatter = StaticLabelsFormatter(monthlyGraphView)
        staticLabelsFormatter.setHorizontalLabels(labels.toTypedArray())
        monthlyGraphView.gridLabelRenderer.labelFormatter = staticLabelsFormatter
        monthlyGraphView.gridLabelRenderer.textSize = 30f
        monthlyGraphView.gridLabelRenderer.numHorizontalLabels = labels.size

        // Hiển thị giá trị trên các điểm
        monthlySeries.isDrawDataPoints = true
        monthlySeries.dataPointsRadius = 10f

        // Thêm sự kiện khi chạm vào điểm để hiển thị doanh thu
        monthlySeries.setOnDataPointTapListener { _, dataPoint ->
            val revenue = "%.2f".format(dataPoint.y) // Format giá trị doanh thu
            val month = labels[dataPoint.x.toInt()] // Lấy nhãn tháng tương ứng
            Toast.makeText(requireContext(), "Tháng: $month\nDoanh thu: $revenue", Toast.LENGTH_SHORT).show()
        }

        // Đặt tiêu đề
        val selectedMonthFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        monthlyGraphView.title = "Doanh thu theo tháng kể từ tháng ${selectedMonthFormat.format(selectedDate)}"
    }

    
    private fun getDailyRevenueData(selectedDate: Date, callback: (Map<Date, Double>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)


        val dailyData = mutableMapOf<Date, Double>()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        for (i in 0..6) {
            val currentDate = calendar.time
            val formattedDate = dateFormat.format(currentDate)

            db.collection("orders")
                .whereEqualTo("orderDate", formattedDate)
                .get()
                .addOnSuccessListener { documents ->
                    var totalRevenue = 0.0
                    for (document in documents) {
                        totalRevenue += document.getDouble("totalPrice") ?: 0.0
                    }

                    // Thêm log để kiểm tra dữ liệu trả về
                    Log.d("RevenueData", "Date: $formattedDate, Revenue: $totalRevenue")

                    dailyData[currentDate] = totalRevenue
                    if (dailyData.size == 7) {
                        // Sắp xếp dữ liệu trước khi trả về
                        callback(dailyData.toSortedMap())
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("RevenueData", "Failed to fetch data for $formattedDate", exception)
                    dailyData[currentDate] = 0.0
                    if (dailyData.size == 7) {
                        // Sắp xếp dữ liệu trước khi trả về
                        callback(dailyData.toSortedMap())
                    }
                }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }


    private fun getMonthlyRevenueData(selectedDate: Date, callback: (Map<Date, Double>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val monthlyData = mutableMapOf<Date, Double>()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) // Định dạng ngày trong Firestore
        val monthFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault()) // Định dạng tháng cho log

        for (i in 0..5) {
            // Định nghĩa khoảng thời gian của tháng
            val startOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
            }.time
            val endOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }.time

            // Log khoảng thời gian của tháng
            Log.d("MonthlyRevenue", "Fetching data for month: ${monthFormat.format(startOfMonth)} (${dateFormat.format(startOfMonth)} - ${dateFormat.format(endOfMonth)})")

            val tempCalendar = Calendar.getInstance()
            tempCalendar.time = startOfMonth
            var totalRevenue = 0.0

            while (tempCalendar.time <= endOfMonth) {
                val currentDate = tempCalendar.time
                val formattedDate = dateFormat.format(currentDate)

                db.collection("orders")
                    .whereEqualTo("orderDate", formattedDate)
                    .get()
                    .addOnSuccessListener { documents ->
                        var dailyRevenue = 0.0
                        for (document in documents) {
                            dailyRevenue += document.getDouble("totalPrice") ?: 0.0
                        }

                        // Log doanh thu của ngày hiện tại
                        Log.d("MonthlyRevenue", "Date: $formattedDate, Daily Revenue: $dailyRevenue")

                        if (!monthlyData.containsKey(startOfMonth)) {
                            monthlyData[startOfMonth] = 0.0
                        }
                        monthlyData[startOfMonth] = monthlyData[startOfMonth]!! + dailyRevenue

                        // Nếu là ngày cuối cùng trong tháng, log tổng doanh thu
                        if (currentDate == endOfMonth) {
                            Log.d("MonthlyRevenue", "Month: ${monthFormat.format(startOfMonth)}, Total Revenue: ${monthlyData[startOfMonth]}")
                            if (monthlyData.size == 6) {
                                callback(monthlyData.toSortedMap())
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("MonthlyRevenue", "Failed to fetch data for $formattedDate", exception)
                    }

                tempCalendar.add(Calendar.DAY_OF_MONTH, 1) // Chuyển sang ngày tiếp theo
            }

            calendar.add(Calendar.MONTH, 1) // Chuyển sang tháng tiếp theo
        }
    }


}
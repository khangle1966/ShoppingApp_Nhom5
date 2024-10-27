package com.nhom5.shoppingapp.ui.order

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.nhom5.shoppingapp.databinding.FragmentOrderSuccessBinding
import androidx.navigation.fragment.findNavController
import com.nhom5.shoppingapp.R

class OrderSuccessFragment : Fragment() {

    private lateinit var binding: FragmentOrderSuccessBinding
    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrderSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hiển thị loader khi đang xử lý đơn hàng
        showLoader()

        // Giả lập việc hoàn tất đơn hàng và hiển thị thông báo thành công sau vài giây
        simulateOrderProcessing()

        // Xử lý sự kiện khi người dùng nhấn nút "Back to Home"
        binding.backToHomeBtn.setOnClickListener {
            // Điều hướng về trang Home
            findNavController().navigate(R.id.action_orderSuccessFragment_to_homeFragment)
        }

        // Bắt đầu bộ đếm thời gian để tự động chuyển về Home sau 5 giây
        startRedirectCountdown()
    }

    private fun showLoader() {
        binding.loaderLayout.loaderCard.visibility = View.VISIBLE
        binding.orderConstraintGroup.visibility = View.GONE // Ẩn nội dung chính
    }

    private fun hideLoader() {
        binding.loaderLayout.loaderCard.visibility = View.GONE
        binding.orderConstraintGroup.visibility = View.VISIBLE // Hiển thị nội dung chính
    }

    private fun simulateOrderProcessing() {
        // Giả lập thời gian chờ để xử lý đơn hàng, ở đây ta dùng 2 giây để mô phỏng
        binding.root.postDelayed({
            // Ẩn loader sau khi xử lý đơn hàng
            hideLoader()

            // Hiển thị thông báo đơn hàng thành công
            Toast.makeText(requireContext(), "Đơn hàng của bạn đã được đặt thành công!", Toast.LENGTH_SHORT).show()

        }, 2000)
    }

    private fun startRedirectCountdown() {
        timer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.redirectHomeTimerTv.text = "Tự động quay lại trang chủ sau ${millisUntilFinished / 1000} giây"
            }

            override fun onFinish() {
                // Tự động điều hướng về trang Home khi hết thời gian
                findNavController().navigate(R.id.action_orderSuccessFragment_to_homeFragment)
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hủy bộ đếm thời gian nếu người dùng rời khỏi fragment
        timer?.cancel()
    }
}

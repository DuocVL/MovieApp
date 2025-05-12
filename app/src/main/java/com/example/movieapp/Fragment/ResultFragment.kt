package com.example.movieapp.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.movieapp.Activities.PackagePaymentActivity
import com.example.movieapp.databinding.FragmentResultBinding

class ResultFragment : Fragment(){
    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ResultFragment", "onViewCreated called")
        val packagePaymentActivity = activity as? PackagePaymentActivity
        val statusPayment = packagePaymentActivity?.getStatusPayment() ?: false
        val orderId = packagePaymentActivity?.getOrderId() ?: ""

        if (statusPayment) {
            binding.statusPayment.text = "Thanh toán thành công với mã đơn hàng: $orderId"
        } else {
            binding.statusPayment.text = "Thanh toán thất bại"
        }

        binding.buttonBack.setOnClickListener {
            if (statusPayment) {
                packagePaymentActivity?.showPackageStatusUI()
            }else{
                packagePaymentActivity?.setStatusPackage()
            }
        }
    }

}
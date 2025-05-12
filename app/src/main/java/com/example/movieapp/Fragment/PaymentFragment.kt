package com.example.movieapp.Fragment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.movieapp.Activities.PackagePaymentActivity
import com.example.movieapp.AppSessionViewModel
import com.example.movieapp.Dataclass.PackagePayment
import com.example.movieapp.databinding.FragmentPaymentBinding

class PaymentFragment : Fragment() {
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var appSessionViewModel: AppSessionViewModel
    private lateinit var packageSelected: PackagePayment
    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        getPackageSelected()
        Log.d("PaymentFragment", "onCreateView called $packageSelected")
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("PaymentFragment", "onViewCreated called $packageSelected")

        appSessionViewModel = ViewModelProvider(requireActivity()).get(AppSessionViewModel::class.java)
        webView = binding.webView
        webView.settings.javaScriptEnabled = true

        getURL()
    }


    private fun getURL(){
        val serverUrl = Uri.parse("https://web-production-ef928.up.railway.app")
            .buildUpon()
            .appendPath("create-payment-link")
            .appendQueryParameter("userId", appSessionViewModel.getUserId())
            .appendQueryParameter("packageId", packageSelected.id)
            .build().toString()

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                Log.d("PaymentFragment", "Redirect URL: $url")

                if (url.contains("/payment-success") || url.contains("/payment-cancel")) {
                    val uri = Uri.parse(url)
                    val code = uri.getQueryParameter("code")
                    val id = uri.getQueryParameter("id")
                    val cancel = uri.getQueryParameter("cancel")
                    val status = uri.getQueryParameter("status")
                    val orderCode = uri.getQueryParameter("orderCode")

                    if(code == "00"){
                        if(cancel == "false"){
                            if(status == "PAID"){
                                Toast.makeText(requireContext(), "✅ Thanh toán thành công! Mã đơn hàng: $orderCode", Toast.LENGTH_SHORT).show()
                                showPaymentResult(true, orderCode)
                            }else if(status == "PENDING"){
                                Toast.makeText(requireContext(), "Đang chờ thanh toán", Toast.LENGTH_SHORT).show()
                            }else if(status == "PROCESSING"){
                                Toast.makeText(requireContext(), "Đang xử lý", Toast.LENGTH_SHORT).show()
                            }else if(status == "CANCELLED"){
                                Toast.makeText(requireContext(), "❌ Thanh toán bị hủy", Toast.LENGTH_SHORT).show()
                                showPaymentResult(false, orderCode)
                            }
                        }else{
                            Toast.makeText(requireContext(), "❌ Thanh toán bị hủy", Toast.LENGTH_SHORT).show()
                            showPaymentResult(false, orderCode)
                        }
                    }else if(code == "01"){
                        Toast.makeText(requireContext(), "❌ Thanh toán thất bại", Toast.LENGTH_SHORT).show()
                        showPaymentResult(false, orderCode)
                    }
                    return true // chặn WebView load thêm
                }
                return false
            }
        }

        webView.loadUrl(serverUrl)
    }



    private fun getPackageSelected() {
        packageSelected =
            (activity as? PackagePaymentActivity)?.getSelectedPackage() ?: PackagePayment(
                "1",
                "Gói theo tháng",
                "30.000đ/tháng",
                "1"
            )
    }

    private fun showPaymentResult(success: Boolean, orderCode: String?) {
        (activity as? PackagePaymentActivity)?.apply {
            setStatusPayment(success)
            setOrderId(orderCode ?: "")
            goToResult()
        }
    }


    override fun onResume() {
        super.onResume()
        getPackageSelected()
        Log.d("PaymentFragment", "onResume called $packageSelected")
        getURL()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


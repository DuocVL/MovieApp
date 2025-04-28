package com.example.movieapp.Fragment

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
import com.example.movieapp.Activities.PackagePaymentActivity
import com.example.movieapp.Dataclass.PackagePayment
import com.example.movieapp.databinding.FragmentPaymentBinding
import org.json.JSONObject

class PaymentFragment : Fragment() {
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("PaymentFragment", "onViewCreated called $packageSelected")
        webView = binding.webView
        webView.settings.javaScriptEnabled = true

        val amount = 5000
        val description = "Thanh toán đơn hàng"
        val orderCode = 75

        val serverUrl = Uri.parse("https://web-production-ef928.up.railway.app/")
            .buildUpon()
            .appendPath("create-payment-link")
            .appendQueryParameter("amount", amount.toString())
            .appendQueryParameter("description", description)
            .appendQueryParameter("orderCode", orderCode.toString())
            .build().toString()

        webView.webViewClient = object : WebViewClient() {
            // Android: PaymentFragment (shouldOverrideUrlLoading part - NO CHANGE NEEDED)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                Log.d("PaymentFragment", "Redirect URL: $url")

                // Check if the URL contains either success or cancel paths
                if (url.contains("/payment-success") || url.contains("/payment-cancel")) {
                    // Attempt to extract query parameters
                    val uri = Uri.parse(url)
                    val status = uri.getQueryParameter("status") // Will now be "true" or "false"
                    val orderCode = uri.getQueryParameter("orderCode") // Will now contain the order code

                    // Check if *both* status and orderCode were successfully extracted
                    if (status != null && orderCode != null) {
                        // Handle the payment result based on the 'status' parameter
                        val isSuccess = status.equals("true", ignoreCase = true)
                        showPaymentResult(success = isSuccess, orderCode = orderCode)
                        return true // Prevent WebView from loading the /payment-success|cancel URL
                    } else {
                        // Handle cases where parameters might be missing (shouldn't happen with server changes)
                        Log.e("PaymentFragment", "Missing status or orderCode in redirect URL: $url")
                        Toast.makeText(requireContext(), "Lỗi: Không thể xác định trạng thái thanh toán từ URL.", Toast.LENGTH_LONG).show()
                        // Optionally close the fragment even if parameters are missing
                        parentFragmentManager.popBackStack()
                        return true // Prevent WebView loading
                    }
                }
                // Allow WebView to load other URLs (e.g., the PayOS checkout page itself)
                return false
            }
        }
        webView.loadUrl(serverUrl)
    }


    fun getPackageSelected() {
        packageSelected =
            (activity as? PackagePaymentActivity)?.getSelectedPackage() ?: PackagePayment(
                "1",
                "Gói theo tháng",
                "30.000đ/tháng",
                "1"
            )
    }

    private fun showPaymentResult(success: Boolean, orderCode: String?) {
        val message = if (success) {
            "Thanh toán thành công (chờ xác nhận)! Mã đơn hàng: $orderCode" // Adjusted message
        } else {
            "Thanh toán thất bại hoặc đã bị hủy! Mã đơn hàng: $orderCode"
        }

        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

        // Add logic to close the fragment / navigate back / update UI
        // Example:
        // Consider adding a small delay before popping to allow the user to read the Toast
        view?.postDelayed({
            parentFragmentManager.popBackStack()
            // Or navigate to a specific "Order Confirmation" screen
        }, 2000) // 2 seconds delay
    }

    override fun onResume() {
        super.onResume()
        getPackageSelected()
        Log.d("PaymentFragment", "onResume called $packageSelected")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


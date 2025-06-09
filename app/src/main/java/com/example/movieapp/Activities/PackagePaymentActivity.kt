package com.example.movieapp.Activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.movieapp.Adapters.PackagePagerAdapter
import com.example.movieapp.AppSessionViewModel
import com.example.movieapp.Dataclass.PackagePayment
import com.example.movieapp.databinding.ActivityPackagePaymentBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class PackagePaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPackagePaymentBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var firestore: FirebaseFirestore
    private lateinit var appSessionViewModel: AppSessionViewModel
    private lateinit var type: String

    private lateinit var statusPackage: TextView
    private lateinit var timeStartText: TextView
    private lateinit var timeEndText: TextView

    private var selectedPackage: PackagePayment? = null
    private var statusPayment = false
    private var orderId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackagePaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        appSessionViewModel = AppSessionViewModel(application)
        tabLayout = binding.tabLayout
        viewPager = binding.viewPager
        statusPackage = binding.statusPayment
        timeStartText = binding.timeStart
        timeEndText = binding.timeEnd

        showPackageStatusUI()

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.buttonBuy.setOnClickListener {
            binding.resultPayment.visibility = View.GONE
            binding.tabLayout.visibility = View.VISIBLE
            binding.viewPager.visibility = View.VISIBLE

        }

        viewPager.adapter = PackagePagerAdapter(this)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                when(position){
                    1 -> if(selectedPackage == null) goToSelectPackage()
                    2 -> if(selectedPackage == null) goToSelectPackage()
                }
            }
        })

        TabLayoutMediator(tabLayout,viewPager){tab,position->
            tab.text = when(position){
                0 -> "Chọn gói"
                1 -> "Thanh toán"
                else -> "Kết quả"
            }
        }.attach()

    }

    fun showPackageStatusUI() {
        binding.resultPayment.visibility = View.VISIBLE
        binding.tabLayout.visibility = View.GONE
        binding.viewPager.visibility = View.GONE
        val userId = appSessionViewModel.getUserId()
        //val userId = "2"
        firestore.collection("users")
            .document(userId)
            .collection("payments")
            .document("status")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val packageId = doc.getString("packageId") ?: "?"
                    val startTime = doc.getLong("startTime") ?: 0L
                    val endTime = doc.getLong("endTime") ?: 0L

                    val packageName = when (packageId) {
                        "1" -> "Gói theo tháng"
                        "2" -> "Gói theo quý"
                        "3" -> "Gói theo năm"
                        else -> "Không xác định"
                    }

                    val startDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(startTime))
                    val endDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(endTime))

                    if(Date().time > endTime){
                        statusPackage.text = "Gói đã hết hạn"
                    }else{
                        statusPackage.text = "Gói đang hoạt động"
                    }
                    timeStartText.text = "Thời gian bắt đầu: $startDate"
                    timeEndText.text = "Thời gian kết thúc: $endDate"
                } else {
                    statusPackage.text = "Bạn chưa mua gói nào"
                    timeStartText.text = ""
                    timeEndText.text = ""
                }
            }
            .addOnFailureListener {
                statusPackage.text = "Bạn chưa mua gói thue bao nao"
                timeStartText.text = ""
                timeEndText.text = ""
                Log.e("PackagePaymentActivity", "Lỗi đọc Firestore: ${it.message}")
            }
    }

    fun setStatusPackage(){
        binding.resultPayment.visibility = View.VISIBLE
        binding.tabLayout.visibility = View.GONE
        binding.viewPager.visibility = View.GONE
    }


    fun setSelectedPackage(packagePayment: PackagePayment){
        selectedPackage = packagePayment
    }

    fun getSelectedPackage(): PackagePayment? {
        return selectedPackage
    }

    fun goToSelectPackage() {
        viewPager.currentItem = 0
    }

    fun setOrderId(id: String){
        orderId = id
    }

    fun getOrderId(): String{
        return orderId
    }

    fun setStatusPayment(status: Boolean){
        Log.d("PackagePaymentActivity", "setStatusPayment called with status: $status")
        statusPayment = status
    }

    fun getStatusPayment(): Boolean{
        Log.d("PackagePaymentActivity", "getStatusPayment called with status: $statusPayment")
        return statusPayment
    }

    fun goToPayment(){
        viewPager.currentItem = 1
    }

    fun goToResult(){
        viewPager.currentItem = 2
    }
}
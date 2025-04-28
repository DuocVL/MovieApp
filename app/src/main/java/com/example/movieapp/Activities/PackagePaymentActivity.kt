package com.example.movieapp.Activities

import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.movieapp.Adapters.PackagePagerAdapter
import com.example.movieapp.Dataclass.PackagePayment
import com.example.movieapp.databinding.ActivityPackagePaymentBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PackagePaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPackagePaymentBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private var selectedPackage: PackagePayment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackagePaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tabLayout = binding.tabLayout
        viewPager = binding.viewPager

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

    fun setSelectedPackage(packagePayment: PackagePayment){
        selectedPackage = packagePayment

    }

    fun getSelectedPackage(): PackagePayment? {
        return selectedPackage
    }

    fun goToSelectPackage() {
        viewPager.currentItem = 0
    }


    fun goToPayment(){
        viewPager.currentItem = 1
    }

    fun goToResult(){
        viewPager.currentItem = 2
    }
}
package com.example.movieapp.Activities

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.movieapp.AppSessionViewModel
import com.example.movieapp.Fragment.CategoryFragment
import com.example.movieapp.Fragment.DowloadFragment
import com.example.movieapp.Fragment.HomeFragment
import com.example.movieapp.Fragment.ProfileFragment
import com.example.movieapp.R
import com.example.movieapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appSessionViewModel: AppSessionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Hiển thị giao diện HomeFragment mặc dịnh
        loadFragment(HomeFragment())
        appSessionViewModel = AppSessionViewModel(application)
        val userId = appSessionViewModel.getUserId()
        val isAnonymous = appSessionViewModel.isAnonymous()
        Log.d("AppSessionViewModel", "User ID: $userId, Anonymous: $isAnonymous")
        
        //Xử lý sự kiện khi chọn item trong BottomNavigationView
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_category -> loadFragment(CategoryFragment())
                R.id.nav_dowload -> loadFragment(DowloadFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            //Trả về true để hiển thị item được chọn
            true
        }

    }

    //Hiển th Fragment lên giao diện
    private fun loadFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container,fragment)
            .commit()
    }
}
package com.example.movieapp.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.movieapp.R
import com.example.movieapp.databinding.ActivityIntroBinding
import com.example.movieapp.databinding.ActivityMainBinding

class IntroActivity : ComponentActivity() {

    // Sử dụng lại các hằng số từ SplashActivity
    companion object {
        private const val PREFS_NAME = "AppIntroPrefs"
        private const val KEY_IS_FIRST_TIME = "isFirstTimeLaunch"
    }

    private lateinit var binding : ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.getStartBt.setOnClickListener{
           // Đánh dấu là đã xem Intro
            val prefs : SharedPreferences = getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE)
            val editor :SharedPreferences.Editor = prefs.edit()
            editor.putBoolean(KEY_IS_FIRST_TIME,false)
            editor.apply()

            //Chuyen man hinh sang Login
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
            finish()
        }
    }
}


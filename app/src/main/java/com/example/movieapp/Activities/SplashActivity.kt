package com.example.movieapp.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieapp.SessionManager

class SplashActivity : AppCompatActivity(){
    companion object {
        // Tên file SharedPreferences
        private const val PREFS_NAME = "AppIntroPrefs"
        // Key để lưu trạng thái đã xem intro hay chưa
        private const val KEY_IS_FIRST_TIME = "isFirstTimeLaunch"
    }

    //Luu thong tin dang nhap
    private lateinit var sessionManager: SessionManager
    private var loginAnonymous: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Không cần setContentView vì màn hình này chỉ để điều hướng

        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstTime = prefs.getBoolean(KEY_IS_FIRST_TIME, true) // Lấy giá trị, mặc định là true (lần đầu)

        // Khởi tạo SessionManager
        sessionManager = SessionManager(applicationContext)

        if (isFirstTime) {
            // Nếu là lần đầu -> Chuyển đến IntroActivity
            val intent = Intent(this,IntroActivity::class.java)
            startActivity(intent)
        } else {
            if(sessionManager.isLoggedIn()){
                // Nếu không phải lần đầu -> Chuyển đến MainActivity
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
            }else{
                // Nếu không phải lần đầu -> Chuyển đến MainActivity
                val intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
            }
        }

        // Kết thúc SplashActivity để người dùng không thể quay lại màn hình này
        finish()
    }
}
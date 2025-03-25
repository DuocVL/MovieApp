package com.example.movieapp.Activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.movieapp.Adapters.ImagePagerAdapter
import com.example.movieapp.R
import com.example.movieapp.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Danh sách URL ảnh
        val imageUrls = listOf(
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTbEb5k7WIog4zOTAWbYbCDS-Uk85C5ure_4w&s",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ3WVixuBZeI8Dvzd3ebXQi-CqjXnOWgAjbxvmfgcLw9t9aVcRUbIpsLu-eaoYR7nJR41Q&usqp=CAU",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQGOTgdgRwO9O2FZaBoGpHUwZ2OODt87ON-CQDw-aQ7d8l-Ryvcx-iqSHdK8KfuJjD5Ako&usqp=CAU"
        )

        // Thiết lập ViewPager2
        val adapter = ImagePagerAdapter(imageUrls)
        binding.viewPager.adapter = adapter

        setContentView(R.layout.activity_main)
    }
}
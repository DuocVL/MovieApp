package com.example.movieapp.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.movieapp.R

class IntroActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_intro)

        val getStartBt : Button = findViewById(R.id.getStartBt)
        getStartBt.setOnClickListener {
            Toast.makeText(this,"Button Clicked!",Toast.LENGTH_SHORT).show()
            //val intent = Intent(this, LoginActivity::class.java)
            val intent = Intent(this,SignupActivity::class.java)
            startActivity(intent)
        }
    }

}


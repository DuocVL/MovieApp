package com.example.movieapp.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.movieapp.R

class LoginActivity : AppCompatActivity() {
    private lateinit var userEdt : EditText
    private lateinit var passEdt : EditText
    private lateinit var loginButton : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        userEdt = findViewById(R.id.editTextUser)
        passEdt = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.LoginButton)
        loginButton.setOnClickListener {
            if(userEdt.text.toString().isEmpty() || passEdt.text.toString().isEmpty()){
                Toast.makeText(this,"Please Fill your user and password",Toast.LENGTH_SHORT).show()
            }else if(userEdt.text.toString().equals("test") && passEdt.text.toString().equals("12345")){
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                Toast.makeText(this,"Your user and password is not correct",Toast.LENGTH_SHORT).show()
            }
        }
    }
}
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
    private lateinit var donHaveAccount: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        userEdt = findViewById(R.id.editTextUser)
        passEdt = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.LoginButton)
        donHaveAccount = findViewById(R.id.donAccount)
        donHaveAccount.setOnClickListener{
            Toast.makeText(this,"da nhan",Toast.LENGTH_SHORT).show()
            val intent = Intent(this,SignupActivity::class.java)
            Toast.makeText(this,"da xong",Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
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
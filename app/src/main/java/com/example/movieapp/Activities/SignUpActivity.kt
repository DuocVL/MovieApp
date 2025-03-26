package com.example.movieapp.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.movieapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.add.setOnClickListener {
            val email = binding.emailaddress.text.toString()
            val password = binding.editTextPassword.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()){
                if(password == confirmPassword){

                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.e("SignUpActivity", "Đăng ký thất bại: ", task.exception)
                            }else{
                                val intent = Intent(this,MainActivity::class.java)
                                startActivity(intent)
                            }
                        }

                }else{
                    Toast.makeText(this,"Confirm Password khac Password",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Empty",Toast.LENGTH_SHORT).show()
            }
        }
    }
}
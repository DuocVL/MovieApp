package com.example.movieapp.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieapp.databinding.ActivityOtpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider

class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        // Lấy verificationId từ Intent
        verificationId = intent.getStringExtra("verificationId")
        binding.backButton.setOnClickListener {
            val intent = Intent(this, PhoneNumberActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.login.setOnClickListener {
            val otpCode = binding.pinView.text.toString().trim()
            if (otpCode.isNotEmpty()) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, otpCode)
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Đăng nhập thất bại: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }else{
                Toast.makeText(this,"Vui long nhap ma xac nhan",Toast.LENGTH_SHORT).show()
            }
        }

    }
}
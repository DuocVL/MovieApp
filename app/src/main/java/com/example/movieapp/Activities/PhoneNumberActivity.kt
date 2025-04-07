package com.example.movieapp.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieapp.R
import com.example.movieapp.databinding.ActivityPhonenumberBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneNumberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhonenumberBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPhonenumberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.backButton.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right)
        }

        binding.next.setOnClickListener {
            val phoneNumber = binding.phoneNumber.text.toString().trim()
            if(phoneNumber.isNotEmpty()){
                val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber("+1$phoneNumber")
                    .setTimeout(60L,TimeUnit.SECONDS)//Dat thoi gian cho verification code
                    .setActivity(this)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            // Bỏ qua việc đăng nhập tự động
                            Toast.makeText(this@PhoneNumberActivity, "Mã đã được gửi đến thiết bị này. Vui lòng nhập mã.", Toast.LENGTH_LONG).show()
                            // Chúng ta sẽ không gọi signInWithPhoneAuthCredential ở đây nữa
                        }

                        override fun onVerificationFailed(exception: FirebaseException) {
                            // Xác minh thất bại
                            Toast.makeText(this@PhoneNumberActivity, "Gửi mã thất bại: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                            // Mã đã được gửi thành công
                            this@PhoneNumberActivity.verificationId = verificationId
                            Toast.makeText(this@PhoneNumberActivity, "Mã xác nhận đã được gửi", Toast.LENGTH_SHORT).show()

                            // Chuyển sang màn hình nhập OTP
                            val intent = Intent(this@PhoneNumberActivity, OTPActivity::class.java)
                            intent.putExtra("verificationId", verificationId)
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                    }).build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }else{
                Toast.makeText(this,"Vui lòng nhập số điện thoại",Toast.LENGTH_SHORT).show()
            }
        }
    }
}
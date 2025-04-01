package com.example.movieapp.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieapp.R
import com.example.movieapp.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth


class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signUp.setOnClickListener{
            val emailAddress : String = binding.editTextEmailAddress.text.toString()
            val password : String = binding.editTextPassword.text.toString()
            val confimPassword = binding.editTextConfirmPassword.text.toString()
            if(emailAddress.isNotEmpty() && password.isNotEmpty() && confimPassword.isNotEmpty()){
                if(password.length < 6 && confimPassword.length < 6){
                    Toast.makeText(this,"Mat khau it nhat 6 ki tu",Toast.LENGTH_SHORT).show()
                }else{
                    if(password == confimPassword){
                        firebaseAuth.createUserWithEmailAndPassword(emailAddress,password).addOnCompleteListener {
                            if(it.isSuccessful){
                                val user = firebaseAuth.currentUser
                                user?.sendEmailVerification()?.addOnCompleteListener {
                                    if(it.isSuccessful){
                                        Toast.makeText(this,"Gui email xac minh thanh cong!",Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this,LoginActivity::class.java)
                                        startActivity(intent)
                                        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
                                    }else{
                                        Toast.makeText(this,"Gui email xac minh khong thanh cong",Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }else{
                                Toast.makeText(this,"Tao tai khoan khong thanh cong",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        Toast.makeText(this,"Mat khau nhap lai khong trung khop",Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this,"Nhap day du cac truong",Toast.LENGTH_SHORT).show()
            }
        }

        binding.haveAccount.setOnClickListener{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

    }
}

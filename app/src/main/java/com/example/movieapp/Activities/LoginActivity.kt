package com.example.movieapp.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieapp.R
import com.example.movieapp.SessionManager
import com.example.movieapp.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khoi tao binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khoi tao firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Khoi tao googleSignInClient
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        //Khoi tao sessionManager
        sessionManager = SessionManager(application)

        binding.login.setOnClickListener {

            val email :String = binding.editTextEmailAddress.text.toString()
            val password :String = binding.editTextPassword.text.toString()
            if(email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.isNotEmpty()){
                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                    val user = firebaseAuth.currentUser
                    if (it.isSuccessful) {
                        if (user != null && user.isEmailVerified) {
                            Toast.makeText(this, "Dang nhap thanh cong", Toast.LENGTH_SHORT).show()
                            assignSessionManager()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        } else {
                            Toast.makeText(this, "Email chua duoc xac minh", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Dang nhap loi", Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this,"Nhap day du cac truong",Toast.LENGTH_SHORT).show()
            }
        }

        binding.createAccount.setOnClickListener {
            val intent = Intent(this,SignupActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        }

        binding.forgotYourPassword.setOnClickListener {
            val email :String = binding.editTextEmailAddress.text.toString()
            if(email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(this,"Email dat lai mat khau da duoc gui",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"Loi dat lai mat khau!",Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this,"Vui long nhap email hop le!",Toast.LENGTH_SHORT).show()
            }


        }
        binding.buttonGoogle.setOnClickListener{
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent,RC_SIGN_IN)
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Đăng nhập thành công: ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
                    finish()
                } else {
                    Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    // Gan gia tri sessionManager
    private fun assignSessionManager() {
        val userId = firebaseAuth.currentUser?.uid
        if(userId != null){
            sessionManager.saveAuthToken(userId)
        }
    }
}
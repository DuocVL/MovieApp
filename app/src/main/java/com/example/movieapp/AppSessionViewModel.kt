package com.example.movieapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth

class AppSessionViewModel(application: Application) : AndroidViewModel(application) {
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var userId: String = firebaseAuth.currentUser?.uid ?: ""

    fun getUserId(): String {
        return userId
    }
}
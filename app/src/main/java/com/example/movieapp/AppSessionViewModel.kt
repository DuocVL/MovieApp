package com.example.movieapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppSessionViewModel(application: Application) : AndroidViewModel(application) {
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseFirestore :FirebaseFirestore = FirebaseFirestore.getInstance()
    private var userId: String = firebaseAuth.currentUser?.uid ?: ""
    private var isAnonymous: Boolean = firebaseAuth.currentUser?.isAnonymous ?: true
    private var packageVip : Boolean = false
    private var startTime : Long = 0
    private var endTime : Long = 0

    fun getPackageVip(callback: (Boolean) -> Unit){
        firebaseFirestore.collection("users")
            .document(userId)
            .collection("payments")
            .document("status")
            .get()
            .addOnSuccessListener { document ->
                startTime = document.getLong("startTime") ?: 0
                endTime = document.getLong("endTime") ?: 0
                if(endTime > System.currentTimeMillis()){
                    callback(true)
                }else{
                    callback(false)
                }
                Log.d("AppSessionViewModel", "startTime: $startTime, endTime: $endTime, packageVip: $packageVip, ${System.currentTimeMillis()}")
            }
            .addOnFailureListener {
                Log.e("AppSessionViewModel", "Error getting documents: ", it)
                callback(false)
            }
        return
    }

    fun getUserId(): String {
        return userId
    }

    fun isAnonymous(): Boolean {
        return isAnonymous
    }
}
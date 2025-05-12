package com.example.movieapp

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val title = data["title"] ?: "Thông báo"
        val body = data["body"] ?: ""
        val movieId = data["movieId"] ?: ""
        val imageUrl = data["imageUrl"] ?: ""
        val userId = AppSessionViewModel(application).getUserId()
        Log.d("MyFirebaseMessagingService", "$data")

        val doc = mapOf(
            "title" to title,
            "body" to body,
            "timestamp" to System.currentTimeMillis(),
            "movieId" to movieId,
            "imageUrl" to imageUrl,
            "read" to false
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("notifications")
            .add(doc)
    }

}
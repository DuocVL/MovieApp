package com.example.movieapp.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieapp.Adapters.NotificationAdapter
import com.example.movieapp.AppSessionViewModel
import com.example.movieapp.Dataclass.Notification
import com.example.movieapp.SessionManager
import com.example.movieapp.databinding.ActivityNotificationBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var adapter: NotificationAdapter
    private val notificationList = mutableListOf<Notification>()
    private lateinit var userId: String
    private lateinit var appSessionViewModel: AppSessionViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appSessionViewModel = AppSessionViewModel(application)
        sessionManager = SessionManager(this)

        if(appSessionViewModel.isAnonymous()){
            binding.loginButton.visibility = View.VISIBLE
            binding.statusNotification.visibility = View.VISIBLE
            binding.listNotification.visibility = View.GONE
            binding.statusNotification.text = "Không có thông báo nào"
            binding.loginButton.setOnClickListener {
                sessionManager.clearSession()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        adapter = NotificationAdapter(notificationList) { notification ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("notifications")
                .document(notification.id)
                .update("read", true)
            val intent = Intent(this,MovieDetailActivity::class.java)
            intent.putExtra("movieId",notification.movieId)
            intent.putExtra("type",notification.type)
            startActivity(intent)
        }
        binding.listNotification.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.listNotification.adapter = adapter

        userId = AppSessionViewModel(application).getUserId()
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                notificationList.clear()
                for (document in snapshot!!.documents) {
                    val id = document.id
                    val title = document.getString("title") ?: ""
                    val body = document.getString("body") ?: ""
                    val timestamp = document.getLong("timestamp") ?: 0L
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val movieId = document.getString("movieId") ?: ""
                    val type = document.getString("type") ?: ""
                    val read = document.getBoolean("read") ?: false
                    val notification = Notification(id, title, body, timestamp, imageUrl, movieId, type,read)
                    notificationList.add(notification)
                    Log.d("NotificationActivity", "Notification added: $notification")
                }
                adapter.notifyDataSetChanged()
            }

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}
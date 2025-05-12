package com.example.movieapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.Dataclass.Notification
import com.example.movieapp.R
import com.example.movieapp.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(private val notifications: List<Notification>,private val onItemClick: (Notification) -> Unit) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.binding.titleTextView.text = notification.title
        holder.binding.summaryTextView.text = notification.body
        holder.binding.timeTextView.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(notification.timestamp))
        Glide.with(holder.itemView.context)
            .load(notification.imageUrl)
            .into(holder.binding.notificationImageView)

        if (notification.read) {
            holder.binding.indicator.background.setTint(android.graphics.Color.GREEN)
        } else {
            holder.binding.indicator.background.setTint(android.graphics.Color.RED)
        }

        holder.itemView.setOnClickListener {
            onItemClick(notification)
        }
    }

    override fun getItemCount(): Int {
        return notifications.size
    }
}
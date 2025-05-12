package com.example.movieapp.Dataclass


data class Notification(
    val id: String,
    val title: String = "",
    val body: String = "",
    val timestamp: Long = 0L,
    val imageUrl: String,
    val movieId: String,
    val type: String? = null,
    val read: Boolean = false
)

package com.example.movieapp.Dataclass

import com.google.firebase.Timestamp

data class Comment(
    val commentId: String,
    val userId: String,
    val username: String,
    val movieId: String,
    val content: String,
    val timestamp: Timestamp,
    val like: Int
)

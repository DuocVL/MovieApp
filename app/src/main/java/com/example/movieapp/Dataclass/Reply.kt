package com.example.movieapp.Dataclass

import com.google.firebase.Timestamp

data class Reply(
    val commentId: String,
    val userId: String,
    val usernameReply: String,
    val username: String,
    val movieId: String,
    val content: String,
    val timestamp: Timestamp,
)

package com.example.movieapp.Dataclass

import com.google.firebase.firestore.FieldValue

data class Comment(
    val commentId: String,
    val userId: String,
    val username: String,
    val movieId: String,
    val content: String,
    val timestamp: FieldValue,
)

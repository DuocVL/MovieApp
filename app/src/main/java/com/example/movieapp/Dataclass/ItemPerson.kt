package com.example.movieapp.Dataclass

data class ItemPerson(
    val id: Int,
    val name: String,
    val role: String, // character (với diễn viên) hoặc "Đạo diễn"
    val imageUrl: String
)


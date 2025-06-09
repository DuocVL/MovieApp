package com.example.movieapp.Dataclass

data class ItemMovie(
    val id: Int,
    val title: String,
    val type: String,
    val releaseDate: String,
    val rating: Double,
    val posterUrl: String?,
    val vip : Boolean = false
)

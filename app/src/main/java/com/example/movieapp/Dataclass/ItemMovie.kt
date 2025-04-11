package com.example.movieapp.Dataclass

data class ItemMovie(
    val id: Int,
    val title: String,
    val type: String, // "movie" hoặc "tv"
    val runtime: Int,
    val releaseDate: String,
    val rating: Double,
    val posterUrl: String?
)

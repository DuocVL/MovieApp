package com.example.movieapp.Dataclass

data class MovieWatching(
    val movieId: String,
    val type: String,
    val progress: Long,
    val duration: Long,
    val episode: Int,
    val bannerUrl: String? = null,
)

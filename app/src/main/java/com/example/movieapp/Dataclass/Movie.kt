package com.example.movieapp.Dataclass

data class Movie(
    val id: Int,
    val title: String,
    val runtime: Int,
    val releaseDate: String,
    val genres: List<String>,
    val overview: String,
    val posterPath: String,
    val backdropPath: String,
    val voteAverage: Double,
    val voteCount: Int,
    val video: String?,
    val actors: List<Actor>,
    val directors: List<Director>
)

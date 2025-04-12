package com.example.movieapp.Dataclass

import java.io.Serializable

data class Movie(
    val id: Int,
    val title: String,
    val type: String,
    val runtime: Int,
    val country: String,
    val releaseDate: String,
    val genres: List<String>,
    val overview: String,
    val posterPath: String,
    val backdropPath: String,
    val voteAverage: Double,
    val voteCount: Int,
    val keyVideo: String?,
    val actors: List<Actor>,
    val directors: List<Director>
) : Serializable

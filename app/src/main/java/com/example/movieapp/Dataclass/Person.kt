package com.example.movieapp.Dataclass

data class Person(
    val id: Int,
    val name: String,
    val birthday: String?,
    val deathday: String?,
    val place_of_birth: String?,
    val known_for_department: String?,
    val also_known_as: List<String>,
    val biography: String,
    val profile_path: String?,
    val movies: List<ItemMovie>
)

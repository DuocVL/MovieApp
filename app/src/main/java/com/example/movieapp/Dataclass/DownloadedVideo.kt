package com.example.movieapp.Dataclass

import androidx.room.Entity

@Entity(tableName = "downloaded_videos", primaryKeys = ["id", "episode"])
data class DownloadedVideo(
    val id: String,
    val title: String,
    val type: String, // "movie" or "tv"
    val episode: Int,
    val localPosterPath: String,
    val localVideoPath: String
)


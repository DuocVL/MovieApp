package com.example.movieapp.Dataclass

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_videos")
data class DownloadedVideo(
    @PrimaryKey val id: String,
    val title: String,
    val type: String, // "movie" or "tv"
    val episode: Int?,
    val posterUrl: String,
    val localVideoPath: String
)


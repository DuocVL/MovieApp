package com.example.movieapp

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.movieapp.Dataclass.DownloadedVideo

class DownloadViewModel : ViewModel() {
    val downloadedVideos = mutableStateListOf<DownloadedVideo>()

    fun addVideo(video: DownloadedVideo) {
        downloadedVideos.add(video)
    }

    fun isDownloaded(id: String): Boolean {
        return downloadedVideos.any { it.id == id }
    }
}

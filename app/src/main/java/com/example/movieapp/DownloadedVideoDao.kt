package com.example.movieapp

import androidx.room.*
import com.example.movieapp.Dataclass.DownloadedVideo

@Dao
interface DownloadedVideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: DownloadedVideo)

    @Query("SELECT * FROM downloaded_videos")
    suspend fun getAll(): List<DownloadedVideo>

    @Query("SELECT * FROM downloaded_videos WHERE id = :id AND episode = :episode")
    suspend fun getById(id: String,episode: Int): DownloadedVideo?

    @Query("SELECT * FROM downloaded_videos WHERE id = :id")
    suspend fun getById(id: String): DownloadedVideo?

    @Query("DELETE FROM downloaded_videos WHERE id = :id AND episode = :episode")
    suspend fun deleteById(id: String,episode: Int)

}

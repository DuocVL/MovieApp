package com.example.movieapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.Dataclass.DownloadedVideo
import com.example.movieapp.databinding.ItemDownloadedVideoBinding

class DownloadedVideoAdapter(private val downloadedVideos: List<DownloadedVideo>) : RecyclerView.Adapter<DownloadedVideoAdapter.MovieDownloadViewHolder>() {
    inner class MovieDownloadViewHolder(val binding: ItemDownloadedVideoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieDownloadViewHolder {
        val binding = ItemDownloadedVideoBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MovieDownloadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieDownloadViewHolder, position: Int) {
        holder.binding.idMovie.text = downloadedVideos[position].id

    }

    override fun getItemCount() = downloadedVideos.size
}
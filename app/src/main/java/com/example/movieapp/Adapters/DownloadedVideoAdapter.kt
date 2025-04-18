package com.example.movieapp.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.Dataclass.DownloadedVideo
import com.example.movieapp.databinding.ItemDownloadedVideoBinding
import java.io.File

class DownloadedVideoAdapter(private val downloadedVideos: List<DownloadedVideo>,private val onItemClick: (DownloadedVideo) -> Unit) : RecyclerView.Adapter<DownloadedVideoAdapter.MovieDownloadViewHolder>() {
    inner class MovieDownloadViewHolder(val binding: ItemDownloadedVideoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieDownloadViewHolder {
        val binding = ItemDownloadedVideoBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MovieDownloadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieDownloadViewHolder, position: Int) {
        Log.d("DownloadedVideoAdapter", "Binding video at position $position size ${downloadedVideos.size}")
        holder.binding.movieName.text = downloadedVideos[position].title
        holder.binding.episode.text = "Tập ${downloadedVideos[position].episode}"
        val file = File(downloadedVideos[position].localPosterPath)
        Glide.with(holder.itemView.context)
            .load(file)
            .into(holder.binding.posterMovie)

        holder.itemView.setOnClickListener {
            onItemClick(downloadedVideos[position])
        }
    }

    override fun getItemCount() = downloadedVideos.size
}
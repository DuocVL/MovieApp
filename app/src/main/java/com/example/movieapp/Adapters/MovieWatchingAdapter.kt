package com.example.movieapp.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.Dataclass.MovieWatching
import com.example.movieapp.databinding.ItemMovieWatchingBinding
import java.util.concurrent.TimeUnit

class MovieWatchingAdapter(private val listMovieWatching: List<MovieWatching>,private val onItemClickListener: (MovieWatching) -> Unit) : RecyclerView.Adapter<MovieWatchingAdapter.MovieWatchingViewHolder>() {

    inner class MovieWatchingViewHolder(val binding: ItemMovieWatchingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieWatchingViewHolder {
        val binding = ItemMovieWatchingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieWatchingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieWatchingViewHolder, position: Int) {
        val item = listMovieWatching[position]
        Log.d("MovieWatchingAdapter", "Binding item: $item")
        holder.binding.durationMovie.text = formatMillisecondsToHHMMSS(item.duration)
        val progress = (item.progress*100 / item.duration).toInt()
        Log.d("MovieWatchingAdapter", "Progress: $progress")
        holder.binding.progressBar.max = 100
        holder.binding.progressBar.progress = progress
        holder.binding.root.setOnClickListener {
            onItemClickListener(item)
        }
        Glide.with(holder.itemView.context)
            .load(item.bannerUrl)
            .into(holder.binding.posterMovie)

    }

    fun formatMillisecondsToHHMMSS(milliseconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60

        val string = ""
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            return String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun getItemCount(): Int {
        return listMovieWatching.size
    }
}
package com.example.movieapp.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.Dataclass.ItemMovie
import com.example.movieapp.databinding.ItemMovieBinding

class MovieAdapter(private val movies : List<ItemMovie>, private val onMovieClick : (ItemMovie) -> Unit) :RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {
    inner class MovieViewHolder(val binding : ItemMovieBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        if(!movie.posterUrl.isNullOrEmpty() && movie.posterUrl != "null"){
            Glide.with(holder.itemView.context)
                .load(movie.posterUrl)
                .into(holder.binding.posterImage)
            Log.d("MovieAdapter", "Poster URL: ${movie.posterUrl}")
        }
        holder.binding.movieTitle.text = movie.title
        holder.binding.rating.text = movie.rating.toString()
        holder.binding.releaseYear.text = movie.releaseDate
        holder.binding.movieType.text = if (movie.type == "movie") "Phim lẻ" else "TV"

        holder.itemView.setOnClickListener {
            onMovieClick(movie)
        }
    }
    override fun getItemCount(): Int = movies.size
}
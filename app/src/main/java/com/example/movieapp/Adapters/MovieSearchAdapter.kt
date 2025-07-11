package com.example.movieapp.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.Dataclass.Movie
import com.example.movieapp.databinding.ItemMovieSearchBinding

class MovieSearchAdapter(
    private val movies: List<Movie>,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieSearchAdapter.MovieSearchViewHolder>() {

    inner class MovieSearchViewHolder(val binding: ItemMovieSearchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieSearchViewHolder {
        val binding = ItemMovieSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieSearchViewHolder, position: Int) {
        Log.d("MovieSearchAdapter", "Binding position: $position")
        val item = movies[position]
        Log.d("MovieSearchAdapter", "Binding item: $item")
        if (!item.posterPath.isNullOrBlank() && item.posterPath != "null") {
            Glide.with(holder.itemView.context)
                .load(item.posterPath)
                .into(holder.binding.posterMovie)
        }
        holder.binding.movieName.text = item.title
        holder.binding.genre.text = item.genres.joinToString(", ")
        holder.binding.voteAverage.text = item.voteAverage.toString()
        holder.binding.year.text = item.releaseDate.takeIf { it.length >= 4 }?.substring(0, 4) ?: "N/A"
        holder.binding.country.text = item.country
        holder.binding.overview.text = item.overview
        if(item.vip) holder.binding.vipLabel.visibility = View.VISIBLE

        holder.itemView.setOnClickListener {
            onMovieClick(item)
        }
    }

    override fun getItemCount(): Int = movies.size
}
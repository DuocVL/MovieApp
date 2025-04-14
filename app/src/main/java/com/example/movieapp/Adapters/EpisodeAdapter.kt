package com.example.movieapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.Dataclass.Episode
import com.example.movieapp.databinding.ItemEpisodeBinding

class EpisodeAdapter(private val episodes : List<Episode>,private val onItemClick : (String) -> Unit) :RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {
    inner class EpisodeViewHolder(val binding: ItemEpisodeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return EpisodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.binding.episodeButton.setText("Tập ${position+1}")
        holder.binding.episodeButton.setOnClickListener {
            onItemClick(episodes[position].url)
        }
    }

    override fun getItemCount() = episodes.size
}
package com.example.movieapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.databinding.ItemBannerBinding

class BannerAdapter(private val banners: List<String>,
                    private val movieIds: List<String>,
                    private val onItemClick: (String) -> Unit)
    : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    inner class BannerViewHolder(val binding : ItemBannerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return BannerViewHolder(binding)
    }

    //Hàm này sẽ được gọi mỗi khi cần hiển thị dữ liệu tại vị trí position
    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(banners[position])// URL ảnh từ TMDB
            .into(holder.binding.imgBanner)

        holder.itemView.setOnClickListener{
            onItemClick(movieIds[position])
        }
    }

    override fun getItemCount() = banners.size
}
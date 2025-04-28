package com.example.movieapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.Dataclass.PackagePayment
import com.example.movieapp.databinding.ItemPackageBinding

class PackageAdapter(private val packages: List<PackagePayment>,private val onItemClick: (PackagePayment) -> Unit) : RecyclerView.Adapter<PackageAdapter.PackageViewHolder>() {
    inner class PackageViewHolder(val binding : ItemPackageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val binding = ItemPackageBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PackageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        holder.binding.tenGoiCuoc.text = packages[position].name
        holder.binding.giaCuoc.text = packages[position].expense
        when(position){
            1 -> holder.binding.cardView.setCardBackgroundColor(holder.itemView.context.getColor(com.example.movieapp.R.color.orange))
            2 -> holder.binding.cardView.setCardBackgroundColor(holder.itemView.context.getColor(com.example.movieapp.R.color.blue))
            3 -> holder.binding.cardView.setCardBackgroundColor(holder.itemView.context.getColor(com.example.movieapp.R.color.red))
        }

        holder.itemView.setOnClickListener {
            onItemClick(packages[position])
        }

    }


    override fun getItemCount(): Int {
        return packages.size
    }
}
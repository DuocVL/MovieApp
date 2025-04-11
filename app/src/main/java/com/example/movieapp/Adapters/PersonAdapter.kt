package com.example.movieapp.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieapp.Dataclass.Person
import com.example.movieapp.R
import com.example.movieapp.databinding.ItemPersonBinding

class PersonAdapter(private val people: List<Person>) : RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {
    inner class PersonViewHolder(val binding: ItemPersonBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val binding = ItemPersonBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PersonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = people[position]
        holder.binding.name.text = person.name
        holder.binding.role.text = person.role

        if(person.imageUrl.isNullOrEmpty() || person.imageUrl == "null"){
            holder.binding.image.setImageResource(R.drawable.icon_profile)
        }else{
            Glide.with(holder.itemView.context)
                .load(person.imageUrl)
                .into(holder.binding.image)
        }

    }

    override fun getItemCount(): Int = people.size
}
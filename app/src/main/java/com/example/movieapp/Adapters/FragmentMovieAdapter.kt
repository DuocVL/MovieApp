package com.example.movieapp.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentMovieAdapter(fragmentManager: FragmentManager,lifecycle: Lifecycle,private val fragmentList: List<Fragment>):
FragmentStateAdapter(fragmentManager,lifecycle){

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

}
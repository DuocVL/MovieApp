package com.example.movieapp.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.movieapp.Fragment.PaymentFragment
import com.example.movieapp.Fragment.ResultFragment
import com.example.movieapp.Fragment.SelectPackageFragment

class PackagePagerAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SelectPackageFragment()
            1 -> PaymentFragment()
            else -> ResultFragment()
        }
    }
}
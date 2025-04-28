package com.example.movieapp.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.movieapp.Activities.SearchActivity
import com.example.movieapp.Adapters.FragmentMovieAdapter
import com.example.movieapp.databinding.FragmentCategoryBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CategoryFragment : Fragment()  {
    private var _binding : FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchButton.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }

        tabLayout = binding.tabLayout
        viewPager2 = binding.viewPager

        //Danh sách các Fragment muốn hi thi
        val fragmentList = listOf(
            MovieTabLayoutFragment(1),
            MovieTabLayoutFragment(2),
            MovieTabLayoutFragment(3)
        )

        //Khoiwr tạo adapter
        val adapter = FragmentMovieAdapter(childFragmentManager,lifecycle,fragmentList)
        viewPager2.adapter = adapter

        //Kết nối TabLayout với ViewPager2
        TabLayoutMediator(tabLayout,viewPager2){ tab,position ->
            when(position){
                0 -> tab.text = "Phim le"
                1 -> tab.text = "Phim bo"
                2 -> tab.text = "Phim hoat hinh"

            }
        }.attach()
        
    }
}
package com.example.movieapp.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.movieapp.Adapters.BannerAdapter
import com.example.movieapp.databinding.FragmentHomeBinding
import com.google.firebase.database.FirebaseDatabase
import com.example.movieapp.BuildConfig

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    //Hàm tạo giao diện cho Fragment.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    //Hàm xử lý sau khi Fragment đã tạo xong view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    //Được gọi khi View của Fragment bị hủy.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Hàm này lấy danh sách ID phim từ Firebase Realtime Database
    private fun getBannerMovie(){

        val db = FirebaseDatabase.getInstance().reference.child("banner_movies") // Trỏ tới nhánh "banner_movies"

        db.get().addOnSuccessListener { snapshot ->
            //Lấy danh sách các movieId dưới dạng chuỗi
            val movieIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }

            val banners = mutableListOf<String>() // Danh sách chứa URL poster từ TMDB

            for( id in movieIds ){
                fetchBannerMovie(id){posterUrl ->
                    banners.add(posterUrl)
                    if(banners.size == movieIds.size){
                        setupBannerSlider(banners)
                    }

                }
            }
        }
    }

    // Hàm gọi API TMDB để lấy poster từ movieId
    private fun fetchBannerMovie(movieId: String,callback : (String) -> Unit){
        val a = 
    }

    // Hàm khởi tạo banner slider sau khi đã có danh sách URL poster
    private fun setupBannerSlider(bannerUrls: List<String>) {
        val adapter = BannerAdapter(bannerUrls) // Adapter dùng cho ViewPager/RecyclerView
        binding.bannerSlider.adapter = adapter // Gán adapter cho slider
    }


}
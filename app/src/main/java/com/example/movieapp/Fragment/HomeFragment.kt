package com.example.movieapp.Fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.movieapp.Adapters.BannerAdapter
import com.example.movieapp.databinding.FragmentHomeBinding
import com.google.firebase.database.FirebaseDatabase
import com.example.movieapp.BuildConfig
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DatabaseReference
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    // 👇 Đặt các biến auto slide ở đây (ngoài hàm onCreateView/onViewCreated)
    private var currentPage = 0
    private val delay: Long = 10000 // 10 giây
    private val sliderHandler = Handler(Looper.getMainLooper())

    private val sliderRunnable = object : Runnable {
        override fun run() {
            val itemCount = binding.bannerSlider.adapter?.itemCount ?: 0
            if (itemCount > 0) {
                currentPage = (currentPage + 1) % itemCount
                binding.bannerSlider.setCurrentItem(currentPage, true)
                sliderHandler.postDelayed(this, delay)
            }
        }
    }

    //Hàm tạo giao diện cho Fragment.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    //Hàm xử lý sau khi Fragment đã tạo xong view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val URL_DB = BuildConfig.URL_DB
        database = FirebaseDatabase.getInstance(URL_DB)
        databaseReference = database.reference
        getBannerMovie()
    }

    //Được gọi khi View của Fragment bị hủy.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        sliderHandler.removeCallbacks(sliderRunnable)
    }
    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, delay)
    }
    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    // Hàm này lấy danh sách ID phim từ Firebase Realtime Database
    private fun getBannerMovie(){
        val bannerMoviesRef = databaseReference.child("banner_movies") // Trỏ tới nhánh "banner_movies"

        bannerMoviesRef.get().addOnSuccessListener { snapshot ->
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

        val TMDB_API_KEY = BuildConfig.TMDB_API_KEY
        val URL = "https://api.themoviedb.org/3/movie/$movieId?api_key=$TMDB_API_KEY&language=en-US"

        val request = Request.Builder().url(URL).build()

        //Gửi request HTTP bằng OkHttp
        OkHttpClient().newCall(request).enqueue(object : Callback{
            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())//Chuyển response thành JSON
                val posterPath = json.getString("poster_path") //Lấy poster_path
                val fullPosterUrl = "https://image.tmdb.org/t/p/w780$posterPath" //Tạo URL đầy đủ

                // Gọi lại callback trên UI thread để cập nhật giao diện
                activity?.runOnUiThread {
                    callback(fullPosterUrl)
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                // Ghi log khi gọi API thất bại
                Log.e("TMDB", "Failed to fetch", e)
            }
        })
    }

    // Hàm khởi tạo banner slider sau khi đã có danh sách URL poster
    private fun setupBannerSlider(bannerUrls: List<String>) {
        val adapter = BannerAdapter(bannerUrls) // Adapter dùng cho ViewPager/RecyclerView
        binding.bannerSlider.adapter = adapter // Gán adapter cho slider

        //Tạo các điểm đánh dấu cho các slide
        TabLayoutMediator(binding.bannerIndicator,binding.bannerSlider){tab,position ->}.attach()

        // Bắt đầu chạy tự động
        sliderHandler.postDelayed(sliderRunnable, delay)
    }


}
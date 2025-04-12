package com.example.movieapp.Fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieapp.Activities.MovieDetailActivity
import com.example.movieapp.Activities.SearchActivity
import com.example.movieapp.Adapters.BannerAdapter
import com.example.movieapp.Adapters.MovieAdapter
import com.example.movieapp.databinding.FragmentHomeBinding
import com.google.firebase.database.FirebaseDatabase
import com.example.movieapp.BuildConfig
import com.example.movieapp.Dataclass.ItemMovie
import com.example.movieapp.Dataclass.Movie
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
    val TMDB_API_KEY = BuildConfig.TMDB_API_KEY



//    private val sliderRunnable = object : Runnable {
//        override fun run() {
//            val itemCount = binding.bannerSlider.adapter?.itemCount ?: 0
//            if (itemCount > 0) {
//                currentPage = (currentPage + 1) % itemCount
//                binding.bannerSlider.setCurrentItem(currentPage, true)
//                sliderHandler.postDelayed(this, delay)
//            }
//        }
//    }

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

        binding.editTextSearch.setOnTouchListener { v,event ->
            if(event.action == android.view.MotionEvent.ACTION_DOWN){
                val intent = Intent(requireContext(), SearchActivity::class.java)
                startActivity(intent)
                true
            }else{
                false
            }
        }
    }

//    //Được gọi khi View của Fragment bị hủy.
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//        sliderHandler.removeCallbacks(sliderRunnable)
//    }
//    override fun onResume() {
//        super.onResume()
//        sliderHandler.postDelayed(sliderRunnable, delay)
//    }
//    override fun onPause() {
//        super.onPause()
//        sliderHandler.removeCallbacks(sliderRunnable)
//    }

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
                        setupBannerSlider(banners,movieIds)
                    }
                }
            }
        }
    }

    // Hàm gọi API TMDB để lấy poster từ movieId
    private fun fetchBannerMovie(movieId: String,callback : (String) -> Unit){

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

    private fun fetchTopRatedMovies ( url : String,callback: (List<ItemMovie>) -> Unit) {

        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Failed to fetch", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val results = json.getJSONArray("results")
                val movies = mutableListOf<ItemMovie>()
                for (i in 0 until minOf(5,results.length())){
                    val item = results.getJSONObject(i)
                    val id = item.getInt("id")
                    val title = item.getString("title")
                    val posterPath = item.getString("poster_path")
                    val fullPosterUrl = if (posterPath != null) "https://image.tmdb.org/t/p/w500$posterPath" else null
                    val releaseDate = item.getString("release_date")
                    val voteAverage = item.getDouble("vote_average")

                    movies.add(ItemMovie(id,title,"movie",releaseDate,voteAverage,fullPosterUrl))
                }
                activity?.runOnUiThread {
                    callback(movies)
                }
            }
        })
    }

    private fun fetchTopRatedTVShows(url: String, callback: (List<ItemMovie>) -> Unit) {
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Failed to fetch TV shows", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val results = json.getJSONArray("results")
                val tvShows = mutableListOf<ItemMovie>()
                for (i in 0 until minOf(5, results.length())) {
                    val item = results.getJSONObject(i)
                    val id = item.getInt("id")
                    val name = item.getString("name") // khác với movie: dùng name thay vì title
                    val posterPath = item.optString("poster_path", null)
                    val fullPosterUrl = if (posterPath != null) "https://image.tmdb.org/t/p/w500$posterPath" else null
                    val firstAirDate = item.optString("first_air_date", "")
                    val voteAverage = item.getDouble("vote_average")

                    tvShows.add(ItemMovie(id, name,"tv", firstAirDate, voteAverage, fullPosterUrl))
                }
                activity?.runOnUiThread {
                    callback(tvShows)
                }
            }
        })
    }




    // Hàm khởi tạo banner slider sau khi đã có danh sách URL poster
    private fun setupBannerSlider(bannerUrls: List<String>,movieIds: List<String>) {
        val adapter = BannerAdapter(bannerUrls,movieIds){movieId ->
            // Xử lý khi người dùng nhấn vào banner
            val intent = Intent(requireContext(), MovieDetailActivity::class.java)
            intent.putExtra("movieId", movieId)
            startActivity(intent)
        } // Adapter dùng cho ViewPager/RecyclerView

        binding.bannerSlider.adapter = adapter // Gán adapter cho slider

        // Hiển thị danh sách phim hay nhat
        val URL_TopRate = "https://api.themoviedb.org/3/movie/top_rated?api_key=$TMDB_API_KEY&language=vi-VN&page=1"
        fetchTopRatedMovies(URL_TopRate) { movies ->
            activity?.runOnUiThread {
                val adapterTopRated = MovieAdapter(movies){movie ->
                    val intent = Intent(requireContext(), MovieDetailActivity::class.java)
                    intent.putExtra("movieId", movie.id.toString())
                    intent.putExtra("type", movie.type) // truyền thêm loại
                    startActivity(intent)
                }
                binding.bestMovie.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                binding.bestMovie.adapter = adapterTopRated

            }
        }

        //Hiển thị danh sách phi hành dộng hay nhất
        val URL_Action = "https://api.themoviedb.org/3/discover/movie?api_key=$TMDB_API_KEY&language=vi-VN&sort_by=vote_average.desc&include_adult=false&include_video=false&page=1&with_genres=28&vote_count.gte=5000"
        fetchTopRatedMovies(URL_Action){movies ->
            activity?.runOnUiThread {
                val adapterAction = MovieAdapter(movies) { movie ->
                    val intent = Intent(requireContext(), MovieDetailActivity::class.java)
                    intent.putExtra("movieId", movie.id.toString())
                    intent.putExtra("type", movie.type) // truyền thêm loại
                    startActivity(intent)
                }
                binding.actionmovie.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                binding.actionmovie.adapter = adapterAction
            }
        }

        //Hiển thị các phim dài tập hay nhaat
        val URL_Long = "https://api.themoviedb.org/3/tv/top_rated?api_key=$TMDB_API_KEY&language=vi-VN&page=1"
        fetchTopRatedTVShows(URL_Long) { shows ->
            activity?.runOnUiThread {
                val adapterLong = MovieAdapter(shows) { show ->
                    val intent = Intent(requireContext(), MovieDetailActivity::class.java)
                    intent.putExtra("movieId", show.id.toString())
                    intent.putExtra("type", show.type) // truyền thêm loại
                    startActivity(intent)
                }
                binding.movieLong.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                binding.movieLong.adapter = adapterLong
            }
        }

        // Bắt đầu chạy tự động
        //sliderHandler.postDelayed(sliderRunnable, delay)
    }

}
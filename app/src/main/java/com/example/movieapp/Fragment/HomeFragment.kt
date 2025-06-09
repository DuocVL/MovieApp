package com.example.movieapp.Fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieapp.Activities.LoginActivity
import com.example.movieapp.Activities.MovieDetailActivity
import com.example.movieapp.Activities.NotificationActivity
import com.example.movieapp.Activities.PackagePaymentActivity
import com.example.movieapp.Activities.SearchActivity
import com.example.movieapp.Activities.WatchMovieActivity
import com.example.movieapp.Adapters.BannerAdapter
import com.example.movieapp.Adapters.MovieAdapter
import com.example.movieapp.Adapters.MovieWatchingAdapter
import com.example.movieapp.AppSessionViewModel
import com.example.movieapp.databinding.FragmentHomeBinding
import com.google.firebase.database.FirebaseDatabase
import com.example.movieapp.BuildConfig
import com.example.movieapp.Dataclass.Actor
import com.example.movieapp.Dataclass.Director
import com.example.movieapp.Dataclass.ItemMovie
import com.example.movieapp.Dataclass.Movie
import com.example.movieapp.Dataclass.MovieWatching
import com.example.movieapp.R
import com.example.movieapp.SessionManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
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
    private val TMDB_API_KEY = BuildConfig.TMDB_API_KEY
    private lateinit var userId : String
    private lateinit var appSessionViewModel: AppSessionViewModel
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var listMovieWatching: List<MovieWatching>
    private lateinit var listMovie : MutableMap<String,Movie>
    private lateinit var sessionManager: SessionManager


    //Hàm tạo giao diện cho Fragment.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    //Hàm xử lý sau khi Fragment đã tạo xong view
    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val URL_DB = BuildConfig.URL_DB
        database = FirebaseDatabase.getInstance(URL_DB)
        databaseReference = database.reference
        appSessionViewModel = AppSessionViewModel(requireActivity().application)
        userId = appSessionViewModel.getUserId()
        firebaseFirestore = FirebaseFirestore.getInstance()
        listMovie = mutableMapOf()
        if(!appSessionViewModel.isAnonymous()){
            getMovieWatching { list ->
                activity?.runOnUiThread {
                    listMovieWatching = list
                    val adapter = MovieWatchingAdapter(listMovieWatching){movie ->
                        val intent = Intent(requireContext(), WatchMovieActivity::class.java)
                        intent.putExtra("movie",listMovie[movie.movieId])
                        startActivity(intent)
                    }
                    binding.movieWatching.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    binding.movieWatching.adapter = adapter
                }
            }
        }
        getBannerMovie()
        getMovieVip {  list ->
            val adapter = MovieAdapter(list.toMutableList()) { movie ->
                val intent = Intent(requireContext(), MovieDetailActivity::class.java)
                intent.putExtra("movieId", movie.id.toString())
                intent.putExtra("type", movie.type) // truyền thêm loại
                intent.putExtra("vip", movie.vip)
                startActivity(intent)
            }
            binding.movieVipList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.movieVipList.adapter = adapter
        }
        sessionManager = SessionManager(requireContext())
        binding.buyPackageButton.setOnClickListener {
            if(appSessionViewModel.isAnonymous()){
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Thông báo")
                    .setMessage("Bạn cần đăng nhập để mua gói ?")
                    .setIcon(R.drawable.ic_help)
                    .setPositiveButton("Đăng nhập") { _, _ ->
                        sessionManager.clearSession()
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                    }
                    .setNegativeButton("Hủy"){ _, _ ->
                        // Không làm gì khi người dùng chọn hủy
                    }
                dialog.show()
            }
            else{
                val intent = Intent(requireContext(),PackagePaymentActivity::class.java)
                startActivity(intent)
            }
        }

        binding.searchButton.setOnClickListener {
            val intent = Intent(requireContext(),SearchActivity::class.java)
            startActivity(intent)
        }

        binding.notificationButton.setOnClickListener {
            val intent = Intent(requireContext(), NotificationActivity::class.java)
            startActivity(intent)
        }
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
                        setupBannerSlider(banners,movieIds)
                    }
                }
            }
        }
    }

    //Kiem tra da mua movie chua
    private fun checkBuyMovie(movieId : String,callback: (Boolean) -> Unit){
        firebaseFirestore.collection("users")
            .document(userId)
            .collection("buyMovie")
            .document(movieId)
            .get()
            .addOnSuccessListener {
                val status = it.getBoolean("status") ?: false
                activity?.runOnUiThread {
                    callback(status)
                }
            }
            .addOnFailureListener {
                Log.e("MovieDetail", "CHua mua phim nay", it)
                activity?.runOnUiThread {
                    callback(false)
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
                for (i in 0 until minOf(10,results.length())){
                    val item = results.getJSONObject(i)
                    val id = item.getInt("id")
                    val title = item.getString("title")
                    val posterPath = item.getString("poster_path")
                    val fullPosterUrl = if (posterPath != null) "https://image.tmdb.org/t/p/w500$posterPath" else null
                    val releaseDate = item.getString("release_date")
                    val voteAverage = item.getDouble("vote_average")
                    checkVip(id.toString(),"movie"){ status ->
                        movies.add(ItemMovie(id,title,"movie",releaseDate,voteAverage,fullPosterUrl,status))
                        if(movies.size == minOf(10,results.length())){
                            activity?.runOnUiThread {
                                callback(movies)
                            }
                        }
                    }
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
                for (i in 0 until minOf(10, results.length())) {
                    val item = results.getJSONObject(i)
                    val id = item.getInt("id")
                    val name = item.getString("name") // khác với movie: dùng name thay vì title
                    val posterPath = item.optString("poster_path", null)
                    val fullPosterUrl = if (posterPath != null) "https://image.tmdb.org/t/p/w500$posterPath" else null
                    val firstAirDate = item.optString("first_air_date", "")
                    val voteAverage = item.getDouble("vote_average")
                    checkVip(id.toString(),"tv"){ status ->
                        tvShows.add(ItemMovie(id,name,"tv",firstAirDate,voteAverage,fullPosterUrl,status))
                        if(tvShows.size == minOf(10,results.length())){
                            activity?.runOnUiThread {
                                callback(tvShows)
                            }
                        }
                    }
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
                val adapterTopRated = MovieAdapter(movies.toMutableList()){movie ->
                    val intent = Intent(requireContext(), MovieDetailActivity::class.java)
                    intent.putExtra("movieId", movie.id.toString())
                    intent.putExtra("type", movie.type) // truyền thêm loại
                    intent.putExtra("vip", movie.vip)
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
                val adapterAction = MovieAdapter(movies.toMutableList()) { movie ->
                    val intent = Intent(requireContext(), MovieDetailActivity::class.java)
                    intent.putExtra("movieId", movie.id.toString())
                    intent.putExtra("type", movie.type) // truyền thêm loại
                    intent.putExtra("vip", movie.vip)
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
                val adapterLong = MovieAdapter(shows.toMutableList()) { show ->
                    val intent = Intent(requireContext(), MovieDetailActivity::class.java)
                    intent.putExtra("movieId", show.id.toString())
                    intent.putExtra("type", show.type) // truyền thêm loại
                    intent.putExtra("vip", show.vip)
                    startActivity(intent)
                }
                binding.movieLong.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                binding.movieLong.adapter = adapterLong
            }
        }

        // Bắt đầu chạy tự động
        //sliderHandler.postDelayed(sliderRunnable, delay)
    }

    private fun getMovieWatching(callback: (List<MovieWatching>) -> Unit){
        firebaseFirestore.collection("users")
            .document(userId)
            .collection("movieProgress")
            .get()
            .addOnSuccessListener { documents ->
                val listMovieWatching = mutableListOf<MovieWatching>()
                for(document in documents){
                    val movieId = document.id
                    val episode = document.getLong("episode")?.toInt() ?: 0
                    val duration = document.getLong("duration") ?: 0
                    val progress = document.getLong("progress") ?: 0
                    val type = document.getString("type") ?: "movie"
                    getBannerMovie(movieId,type) { bannerUrl ->
                        checkVip(movieId,type){ vip ->
                            checkBuyMovie(movieId) { statusBuy ->
                                listMovieWatching.add(MovieWatching(movieId,type, progress,duration,episode,bannerUrl,vip))
                                getMovieDetails(movieId,type,vip,statusBuy){ movie->
                                    listMovie[movieId] = movie
                                    if(listMovieWatching.size == documents.size()){
                                        activity?.runOnUiThread {
                                            callback(listMovieWatching)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        return
    }

    //Hàm lay bannerMovie
    private fun getBannerMovie(movieId: String,type: String ,callback: (String) -> Unit) {
        val url = "https://api.themoviedb.org/3/$type/$movieId?api_key=$TMDB_API_KEY&language=vi-VN"
        val request = Request.Builder().url(url).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Failed to fetch", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val backdropPath = json.optString("backdrop_path", "")
                val fullBackdropUrl = "https://image.tmdb.org/t/p/w780$backdropPath"
                activity?.runOnUiThread {
                    callback(fullBackdropUrl)
                }
            }
        })
        return
    }

    private fun getMovieDetails(movieId: String,type: String,statusVip: Boolean = false,statusBuy: Boolean,callback: (Movie) -> Unit) {
        val url = "https://api.themoviedb.org/3/$type/$movieId?api_key=$TMDB_API_KEY&language=vi-VN"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())

                val id = json.getInt("id")
                val title = json.optString("title", json.optString("name", ""))
                val runtime = if(type.equals("movie")) json.optInt("runtime", 0) else json.optInt("number_of_episodes", 0)
                val releaseDate = json.optString("release_date", json.optString("first_air_date", ""))
                val genres = json.getJSONArray("genres").let {
                    List(it.length()) { index -> it.getJSONObject(index).getString("name") }
                }
                val countries = json.getJSONArray("production_countries")
                val country = if (countries.length() > 0) {
                    countries.getJSONObject(0).getString("name")
                } else {
                    "Thế giới"
                }

                val overview = json.optString("overview")
                val posterPath = json.optString("poster_path", "")
                val fullPosterUrl = "https://image.tmdb.org/t/p/w780$posterPath"
                val backdropPath = json.optString("backdrop_path", "")
                val fullBackdropUrl = "https://image.tmdb.org/t/p/w780$backdropPath"
                val voteAverage = json.optDouble("vote_average", 0.0)
                val voteCount = json.optInt("vote_count", 0)
                fetchCredits(movieId,type) { actors, directors ->
                    fetchVideoKey(movieId,type) { keyVideo ->
                        val movie = Movie(
                            id,
                            title,
                            type,
                            runtime,
                            country,
                            releaseDate,
                            genres,
                            overview,
                            fullPosterUrl,
                            fullBackdropUrl,
                            voteAverage,
                            voteCount,
                            keyVideo,
                            actors,
                            directors,
                            statusVip,
                            statusBuy
                        )
                        activity?.runOnUiThread {
                            callback(movie)
                        }
                    }
                }
            }


            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Failed to fetch movie details", e)
            }
        })
    }

    private fun fetchCredits(movieId: String,type: String, callback: (List<Actor>, List<Director>) -> Unit) {
        val url = "https://api.themoviedb.org/3/$type/$movieId/credits?api_key=$TMDB_API_KEY&language=vi-VN"
        val request = Request.Builder().url(url).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Failed to fetch credits", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val baseImageUrl = "https://image.tmdb.org/t/p/w185"
                val actors = mutableListOf<Actor>()
                val directors = mutableListOf<Director>()
                val castArray = json.getJSONArray("cast")
                val crewArray = json.getJSONArray("crew")

                for (i in 0 until minOf(10, castArray.length())) {
                    val item = castArray.getJSONObject(i)
                    val id = item.optInt("id")
                    val name = item.optString("name")
                    val character = item.optString("character")
                    val profilePathRaw = item.optString("profile_path", null)
                    val profilePath = if (profilePathRaw != null) "$baseImageUrl$profilePathRaw" else null
                    actors.add(Actor(id,name, character, profilePath))
                }

                for (i in 0 until  crewArray.length()) {
                    val item = crewArray.getJSONObject(i)
                    if (item.optString("job") == "Director") {
                        val name = item.optString("name")
                        val id = item.optInt("id")
                        val profilePathRaw = item.optString("profile_path", null)
                        val profilePath = if (profilePathRaw != null && profilePathRaw != "null") "$baseImageUrl$profilePathRaw" else null
                        directors.add(Director(id,name, profilePath))
                    }
                }
                activity?.runOnUiThread {
                    callback(actors, directors)
                }
            }
        })
    }

    private fun fetchVideoKey(movieId: String, type: String, callback: (String?) -> Unit) {
        val url = "https://api.themoviedb.org/3/$type/$movieId/videos?api_key=$TMDB_API_KEY"
        val request = Request.Builder().url(url).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Failed to fetch video key", e)
                activity?.runOnUiThread { callback(null) }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val key = getBestVideoKey(json)
               activity?.runOnUiThread { callback(key) }
            }
        })
    }
    private fun getBestVideoKey(json: JSONObject): String? {
        val results = json.optJSONArray("results") ?: return null
        for (i in 0 until results.length()) {
            val video = results.getJSONObject(i)
            if (video.optString("type") == "Trailer" && video.optBoolean("official", false)) {
                return video.optString("key")
            }
        }
        for (i in 0 until results.length()) {
            val video = results.getJSONObject(i)
            if (video.optString("type") == "Trailer") {
                return video.optString("key")
            }
        }
        return if (results.length() > 0) results.getJSONObject(0).optString("key") else null
    }

    private fun getMovieVip(callback: (List<ItemMovie>) -> Unit) {
        firebaseFirestore.collection("vip")
            .get()
            .addOnSuccessListener { documents ->
                val listMovieIdVip = mutableListOf<Pair<String,String>>()
                val listMovieVip = mutableListOf<ItemMovie>()
                for(document in documents){
                    Log.d("HomeFragment","$document")
                    val movieId = document.getString("movieId") ?: ""
                    val type = document.getString("type") ?: "movie"
                    val vip = document.getBoolean("vip") ?: false
                    Log.d("HomeFragment","$movieId,$type,$vip")
                    if(vip){
                        listMovieIdVip.add(Pair(movieId,type))
                        getItemMovie(movieId,type){
                            listMovieVip.add(it)
                            if(listMovieIdVip.size == documents.size()){
                                activity?.runOnUiThread {
                                    Log.d("HomeFragment","$listMovieVip")
                                    callback(listMovieVip)
                                }
                            }
                        }
                    }

                }
            }
            .addOnFailureListener {
                Log.d("WatchMovieActivity", "get failed with ", it)
                callback(emptyList())
            }
        return
    }

    private fun checkVip(movieId: String,typeMovie: String,callback: (Boolean) -> Unit){
        firebaseFirestore.collection("vip")
            .document("$movieId")
            .get()
            .addOnSuccessListener { document ->
                if(document == null || !document.exists() ){
                    callback(false)
                    return@addOnSuccessListener
                }
                val vip = document.getBoolean("vip") ?: false
                val type = document.getString("type") ?: "movie"
                val id = document.getString("movieId") ?: ""
                if(type.equals(typeMovie) && id.equals(movieId)){
                    callback(vip)
                }else{
                    callback(false)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
        return
    }

    private fun getItemMovie(movieId: String,type: String,callback: (ItemMovie) -> Unit) {
        val url = "https://api.themoviedb.org/3/$type/$movieId?api_key=$TMDB_API_KEY&language=vi-VN"
        val request = Request.Builder().url(url).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Failed to fetch", e)
            }
            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val title = if(type.equals("movie")) json.getString("title") else json.getString("name")
                val posterPath = json.getString("poster_path")
                val fullPosterUrl = "https://image.tmdb.org/t/p/w500$posterPath"
                val releaseDate = json.optString("release_date", json.optString("first_air_date", ""))
                val voteAverage = json.getDouble("vote_average")
                val itemMovie = ItemMovie(movieId.toInt(),title,type,releaseDate,voteAverage,fullPosterUrl,true)
                activity?.runOnUiThread {
                    callback(itemMovie)
                }
            }
        })
    }

}
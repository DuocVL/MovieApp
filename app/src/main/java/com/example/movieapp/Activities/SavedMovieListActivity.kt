package com.example.movieapp.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieapp.Adapters.MovieSearchAdapter
import com.example.movieapp.BuildConfig
import com.example.movieapp.Dataclass.Movie
import com.example.movieapp.Dataclass.WatchLaterMovie
import com.example.movieapp.databinding.ActivitySavedMovieListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class SavedMovieListActivity : AppCompatActivity() {

    private lateinit var  binding : ActivitySavedMovieListBinding

    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val firestore = FirebaseFirestore.getInstance()
    private val TMDB_API_KEY = BuildConfig.TMDB_API_KEY



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedMovieListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        fetchSavedMovies(callback = { savedMovies ->
            runOnUiThread {
                val adapter = MovieSearchAdapter(savedMovies) { movie ->
                    val intent = Intent(this, MovieDetailActivity::class.java)
                    intent.putExtra("movieId", movie.id.toString())
                    intent.putExtra("type", movie.type) // truyền thêm loại
                    startActivity(intent)
                }
                binding.listSaveMovie.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                binding.listSaveMovie.adapter = adapter
            }
        })
    }

    fun fetchSavedMovies(callback: (List<Movie>) -> Unit) {
        currentUser?.uid?.let { userId ->
            firestore.collection("users")
                .document("$userId")
                .collection("watchLater")
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        val savedMovies = mutableListOf<Movie>()
                        for (document in documents) {
                            val movieId = document.id
                            val type = document.getString("type")
                            getDetailMovie(movieId,type!!) { movie ->
                                movie?.let { savedMovies.add(it) }
                                if (savedMovies.size == documents.size()) {
                                    callback(savedMovies)
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Xử lý lỗi
                    Log.e("SavedMovieListActivity", "Error fetching saved movies: $exception")
                }
        }
    }

    fun getDetailMovie(movieId: String,type: String,callback: (Movie?) -> Unit) {
        val url = "https://api.themoviedb.org/3/$type/$movieId?api_key=$TMDB_API_KEY&language=vi-VN"
        val request = Request.Builder().url(url).build()

        var movie : Movie? = null
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Failed to fetch movie details", e)
                callback(null)
            }
            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val id = json.optInt("id")
                val title = if(type.equals("movie")) json.optString("title") else json.optString("name")
                val posterPath = json.optString("poster_path")
                val fullPosterUrl = "https://image.tmdb.org/t/p/w500$posterPath"
                val releaseDate = if(type.equals("movie")) json.optString("release_date") else json.optString("first_air_date")
                val voteAverage = json.optDouble("vote_average")
                val overview = json.optString("overview")
                val genreIds = json.optJSONArray("genre_ids")
                val genreIdList = mutableListOf<Int>()
                if (genreIds != null) {
                    for (j in 0 until genreIds.length()) {
                        genreIdList.add(genreIds.getInt(j))
                    }
                }
                val genres = genreIdsToString(genreIdList)
                val originalLanguage = json.optString("original_language")
                movie = Movie(id, title,type,0,originalLanguage,releaseDate,genres,overview, fullPosterUrl,"",voteAverage,0,null, listOf(), listOf())
                callback(movie)
            }
        })
    }


    private fun genreIdsToString(genreIds: List<Int>): List<String> {
        val genreMap = mapOf(
            28 to "Hành động",
            12 to "Phiêu lưu",
            16 to "Hoạt hình",
            35 to "Hài",
            80 to "Tội phạm",
            99 to "Tài liệu",
            18 to "Chính kịch",
            10751 to "Gia đình",
            14 to "Giả tưởng",
            36 to "Lịch sử",
            27 to "Kinh dị",
            10402 to "Nhạc kịch",
            9648 to "Bí ẩn",
            10749 to "Lãng mạn",
            878 to "Khoa học viễn tưởng",
            10770 to "TV Movie",
            53 to "Gây cấn",
            10752 to "Chiến tranh",
            37 to "Viễn Tây",
            10759 to "Hành động & Phiêu lưu (TV)",
            10762 to "Trẻ em",
            10763 to "Tin tức",
            10764 to "Thực tế",
            10765 to "Khoa học viễn tưởng & Giả tưởng (TV)",
            10766 to "Phim dài tập",
            10767 to "Talk Show"
        )
        return genreIds.mapNotNull { genreMap[it] }
    }
}
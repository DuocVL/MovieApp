package com.example.movieapp.Activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieapp.Adapters.MovieSearchAdapter
import com.example.movieapp.BuildConfig
import com.example.movieapp.Dataclass.Movie
import com.example.movieapp.databinding.ActivitySearchBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class SearchActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySearchBinding
    private val TMDB_API_KEY = BuildConfig.TMDB_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.editTextSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Ẩn bàn phím
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                val keyword = v.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    // Gọi tìm kiếm hoặc chuyển màn
                    searchMovies(keyword,"movie"){ movies ->
                        runOnUiThread {
                            val adapter = MovieSearchAdapter(movies) { movie ->
                                val intent = Intent(this, MovieDetailActivity::class.java)
                                intent.putExtra("movieId", movie.id.toString())
                                intent.putExtra("type", movie.type) // truyền thêm loại
                                startActivity(intent)
                            }
                            binding.movie.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                            binding.movie.adapter = adapter

                        }
                    }
                    searchMovies(keyword,"tv"){ movies ->
                        runOnUiThread {
                            val adapter = MovieSearchAdapter(movies) { movie ->
                                val intent = Intent(this, MovieDetailActivity::class.java)
                                intent.putExtra("movieId", movie.id.toString())
                                intent.putExtra("type", movie.type) // truyền thêm loại
                                startActivity(intent)
                            }
                            binding.tv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                            binding.tv.adapter = adapter
                        }
                    }
                }
                true
            } else {
                false
            }
        }


    }

    private fun searchMovies(query: String,type: String,callback: (List<Movie>) -> Unit){
        // Thực hiện tìm kiếm ở đây
        val url = "https://api.themoviedb.org/3/search/$type?api_key=$TMDB_API_KEY&language=vi-VN&query=${Uri.encode(query)}&vote_count.gte=200"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Failed to fetch movies", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val results = json.getJSONArray("results")
                val movies = mutableListOf<Movie>()
                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)
                    val id = item.optInt("id")
                    val title = if(type.equals("movie")) item.optString("title") else item.optString("name")
                    val posterPath = item.optString("poster_path")
                    val fullPosterUrl = "https://image.tmdb.org/t/p/w500$posterPath"
                    val releaseDate = if(type.equals("movie")) item.optString("release_date") else item.optString("first_air_date")
                    val voteAverage = item.optDouble("vote_average")
                    val overview = item.optString("overview")
                    val genreIds = item.optJSONArray("genre_ids")
                    val genreIdList = mutableListOf<Int>()
                    if (genreIds != null) {
                        for (j in 0 until genreIds.length()) {
                            genreIdList.add(genreIds.getInt(j))
                        }
                    }
                    val genres = genreIdsToString(genreIdList)
                    val originalLanguage = item.optString("original_language")
                    movies.add(Movie(id, title,type,0,originalLanguage,releaseDate,genres,overview, fullPosterUrl,"",voteAverage,0,null, listOf(), listOf()))
                }
                runOnUiThread {
                    callback(movies)
                }
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
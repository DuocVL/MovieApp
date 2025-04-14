package com.example.movieapp.Activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.movieapp.Adapters.PersonAdapter
import com.example.movieapp.BuildConfig
import com.example.movieapp.Dataclass.Actor
import com.example.movieapp.Dataclass.Director
import com.example.movieapp.Dataclass.Movie
import com.example.movieapp.Dataclass.ItemPerson
import com.example.movieapp.R
import com.example.movieapp.databinding.ActivityMovieDetailBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private lateinit var movie: Movie
    private var movieId: String? = null
    private var type: String = "movie" // Gán mặc định là "movie"
    private val TMDB_API_KEY = BuildConfig.TMDB_API_KEY

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        movieId = intent.getStringExtra("movieId")
        type = intent.getStringExtra("type") ?: "movie" // Lấy loại từ intent (mặc định là "movie")

        if (movieId == null) {
            Log.e("MovieDetail", "Không có movieId được truyền vào")
        } else {
            getMovieDetails(movieId!!)
        }

        binding.buttonTrailer.setOnClickListener {
            if(movie.keyVideo != null){
                openYoutube(this, movie.keyVideo!!)
            }else{
                Toast.makeText(this, "Không có trailer", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonWatch.setOnClickListener {
            val intent = Intent(this, WatchMovieActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }

        binding.dropdown.setOnClickListener {
            // Quay trở lại màn hình trước đó
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
            finish()
        }
    }

    private fun getMovieDetails(movieId: String) {
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

                fetchCredits(movieId) { actors, directors ->
                    fetchVideoKey(movieId) { keyVideo ->
                        movie = Movie(
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
                            directors
                        )
                        runOnUiThread {
                            showMovieDetails()
                        }
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Failed to fetch movie details", e)
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

    private fun fetchCredits(movieId: String, callback: (List<Actor>, List<Director>) -> Unit) {
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
                runOnUiThread {
                    callback(actors, directors)
                }
            }
        })
    }

    private fun showMovieDetails() {
        binding.nameMovie.text = movie.title
        binding.time.text = if(movie.type.equals("movie")) "${movie.runtime} phút" else "${movie.runtime} tập"
        binding.rate.text = movie.voteAverage.toString()
        binding.numberVote.text = "(${movie.voteCount})"
        binding.date.text = movie.releaseDate
        binding.categoryList.text = movie.genres.joinToString(", ")
        binding.content.text = movie.overview

        Glide.with(this).load(movie.posterPath).into(binding.postermovie)

        val people = mutableListOf<ItemPerson>().apply {
            addAll(movie.directors.map { ItemPerson(it.id,it.name, "Đạo diễn", it.profilePath ?: "") })
            addAll(movie.actors.map { ItemPerson(it.id,it.name, it.character, it.profilePath ?: "") })
        }

        val adapter = PersonAdapter(people){ person ->
            val intent = Intent(this, PersonActivity::class.java)
            intent.putExtra("id", person.id)
            startActivity(intent)
        }
        binding.people.adapter = adapter
        binding.people.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    // Hàm mở video trên YouTube
    fun openYoutube(context: Context, key: String) {
        val intentApp = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$key"))
        val intentWeb = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$key"))

        // Nếu máy có YouTube app
        try {
            context.startActivity(intentApp)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(intentWeb)
        }
    }

    private fun fetchVideoKey(movieId: String, callback: (String?) -> Unit) {
        val url = "https://api.themoviedb.org/3/$type/$movieId/videos?api_key=$TMDB_API_KEY"
        val request = Request.Builder().url(url).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Failed to fetch video key", e)
                runOnUiThread { callback(null) }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val key = getBestVideoKey(json)
                runOnUiThread { callback(key) }
            }
        })
    }
}

package com.example.movieapp.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.movieapp.Adapters.PersonAdapter
import com.example.movieapp.BuildConfig
import com.example.movieapp.Dataclass.Actor
import com.example.movieapp.Dataclass.Director
import com.example.movieapp.Dataclass.Movie
import com.example.movieapp.Dataclass.Person
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        movieId = intent.getStringExtra("movieId")
        type = intent.getStringExtra("type") ?: "movie"

        if (movieId == null) {
            Log.e("MovieDetail", "Không có movieId được truyền vào")
        } else {
            getMovieDetails(movieId!!)
        }

        binding.dropdown.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
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
                val runtime = json.optInt("runtime", 0)
                val releaseDate = json.optString("release_date", json.optString("first_air_date", ""))
                val genres = json.getJSONArray("genres").let {
                    List(it.length()) { index -> it.getJSONObject(index).getString("name") }
                }
                val overview = json.optString("overview")
                val posterPath = json.optString("poster_path", "")
                val fullPosterUrl = "https://image.tmdb.org/t/p/w780$posterPath"
                val backdropPath = json.optString("backdrop_path", "")
                val fullBackdropUrl = "https://image.tmdb.org/t/p/w780$backdropPath"
                val voteAverage = json.optDouble("vote_average", 0.0)
                val voteCount = json.optInt("vote_count", 0)
                var video = getBestVideoKey(json)?.let {
                    "https://www.youtube.com/watch?v=$it"
                }

                fetchCredits(movieId) { actors, directors ->
                    movie = Movie(
                        id,
                        title,
                        runtime,
                        releaseDate,
                        genres,
                        overview,
                        fullPosterUrl,
                        fullBackdropUrl,
                        voteAverage,
                        voteCount,
                        video,
                        actors,
                        directors
                    )
                    runOnUiThread {
                        showMovieDetails()
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
                    val name = item.optString("name")
                    val character = item.optString("character")
                    val profilePathRaw = item.optString("profile_path", null)
                    val profilePath = if (profilePathRaw != null) "$baseImageUrl$profilePathRaw" else null
                    actors.add(Actor(name, character, profilePath))
                }

                for (i in 0 until crewArray.length()) {
                    val item = crewArray.getJSONObject(i)
                    if (item.optString("job") == "Director") {
                        val name = item.optString("name")
                        val profilePathRaw = item.optString("profile_path", null)
                        val profilePath = if (profilePathRaw != null && profilePathRaw != "null") "$baseImageUrl$profilePathRaw" else null
                        directors.add(Director(name, profilePath))
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
        binding.time.text = "${movie.runtime} phút"
        binding.rate.text = movie.voteAverage.toString()
        binding.numberVote.text = "(${movie.voteCount})"
        binding.date.text = movie.releaseDate
        binding.categoryList.text = movie.genres.joinToString(", ")
        binding.content.text = movie.overview

        Glide.with(this).load(movie.posterPath).into(binding.postermovie)

        val people = mutableListOf<Person>().apply {
            addAll(movie.actors.map { Person(it.name, it.character, it.profilePath ?: "") })
            addAll(movie.directors.map { Person(it.name, "Đạo diễn", it.profilePath ?: "") })
        }

        val adapter = PersonAdapter(people)
        binding.people.adapter = adapter
        binding.people.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }
}

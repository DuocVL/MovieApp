package com.example.movieapp.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.movieapp.Adapters.MovieAdapter
import com.example.movieapp.BuildConfig
import com.example.movieapp.Dataclass.ItemMovie
import com.example.movieapp.Dataclass.Person
import com.example.movieapp.R
import com.example.movieapp.databinding.ActivityPersonBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class PersonActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPersonBinding
    private var id: Int? = null
    private lateinit var TMDB_API_KEY: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        TMDB_API_KEY = BuildConfig.TMDB_API_KEY
        id = intent.getIntExtra("id", -1)
        if (id == -1 && id == null) {
            finish()
            return
        }
        binding.buttonBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right)
            finish()
        }
        getPersonDetails(id!!) { person ->
            binding.Name.text = person.name
            binding.birthday.text = person.birthday
            if(person.deathday.isNullOrEmpty() || person.deathday == "null"){
                binding.deathday.visibility = View.GONE
            }else{
                binding.deathday.visibility = View.VISIBLE
                binding.deathday.text = person.deathday
            }
            binding.placeOfBirth.text = person.place_of_birth
            binding.knownForDepartment.text = person.known_for_department
            binding.alsoKnownAs.text = person.also_known_as.joinToString(", ")
            binding.biography.text = person.biography
            if(!person.profile_path.isNullOrEmpty() && person.profile_path != "null"){
                Glide.with(this)
                    .load(person.profile_path)
                    .into(binding.imageProfile)
            }
            val adapter = MovieAdapter(person.movies){ movie ->
                val intent = Intent(this, MovieDetailActivity::class.java)
                intent.putExtra("movieId", movie.id.toString())
                intent.putExtra("type", movie.type)
                startActivity(intent)
            }
            binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.recyclerView.adapter = adapter
        }
    }

    fun getPersonDetails(id: Int, callback: (Person) -> Unit) {
        val urlVi = "https://api.themoviedb.org/3/person/$id?api_key=$TMDB_API_KEY&language=vi-VN"
        val urlEn = "https://api.themoviedb.org/3/person/$id?api_key=$TMDB_API_KEY&language=en-US"

        fun parseAndCallback(json: JSONObject, biographyFallback: String? = null) {
            val name = json.optString("name")
            val birthday = json.optString("birthday")
            val deathday = json.optString("deathday")
            val placeOfBirth = json.optString("place_of_birth")
            var biography = json.optString("biography")
            if (biography.isNullOrBlank() && !biographyFallback.isNullOrBlank()) {
                biography = biographyFallback
            }
            val profilePath = json.optString("profile_path")
            val fullProfileUrl = "https://image.tmdb.org/t/p/w500$profilePath"
            val alsoKnownAsArray = json.optJSONArray("also_known_as")
            val alsoKnownAs = mutableListOf<String>()
            if (alsoKnownAsArray != null) {
                for (i in 0 until alsoKnownAsArray.length()) {
                    alsoKnownAs.add(alsoKnownAsArray.getString(i))
                }
            }
            val knownForDepartment = json.optString("known_for_department")

            // Gọi 2 lần getMovie và ghép lại
            getMovie(id, "movie") { movies ->
                getMovie(id, "tv") { tvshows ->
                    val allMovies = movies.toMutableList()
                    allMovies.addAll(tvshows)
                    val person = Person(id, name, birthday, deathday, placeOfBirth, knownForDepartment, alsoKnownAs, biography, fullProfileUrl, allMovies)
                    runOnUiThread {
                        callback(person)
                    }
                }
            }
        }

        val client = OkHttpClient()
        val requestVi = Request.Builder().url(urlVi).build()
        client.newCall(requestVi).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string())
                val bioVi = json.optString("biography")
                if (bioVi.isNullOrBlank() || bioVi.length < 50) {
                    // fallback sang tiếng Anh
                    val requestEn = Request.Builder().url(urlEn).build()
                    client.newCall(requestEn).enqueue(object : Callback {
                        override fun onResponse(call: Call, response: Response) {
                            val jsonEn = JSONObject(response.body?.string())
                            parseAndCallback(json, jsonEn.optString("biography"))
                        }
                        override fun onFailure(call: Call, e: IOException) {
                            parseAndCallback(json)
                        }
                    })
                } else {
                    parseAndCallback(json)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Không thể lấy thông tin người", e)
            }
        })
    }


    fun getMovie(id: Int,type: String,callback: (List<ItemMovie>) -> Unit){
        val url = "https://api.themoviedb.org/3/person/$id/${type}_credits?api_key=$TMDB_API_KEY&language=vi-VN"
        val request = Request.Builder().url(url).build()
        val movies = mutableListOf<ItemMovie>()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Failed to fetch movies", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val castArray = json.optJSONArray("cast")

                if (castArray != null) {
                    for (i in 0 until castArray.length()) {
                        val item = castArray.getJSONObject(i)
                        val id = item.optInt("id")
                        val title = item.optString("title")
                        val posterPath = item.optString("poster_path")
                        val fullPosterUrl = "https://image.tmdb.org/t/p/w500$posterPath"
                        val releaseDate = item.optString("release_date")
                        val rating = item.optDouble("vote_average")
                        val voteCount = item.optInt("vote_count")

                        if (voteCount >= 1 && rating > 0) {
                            movies.add(ItemMovie(id, title, type, releaseDate, rating, fullPosterUrl))
                        }
                    }
                }
                runOnUiThread {
                    callback(movies)
                }
            }
        })
    }
}
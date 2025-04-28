package com.example.movieapp.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.Activities.MovieDetailActivity
import com.example.movieapp.Adapters.MovieAdapter
import com.example.movieapp.BuildConfig
import com.example.movieapp.Dataclass.ItemMovie
import com.example.movieapp.R
import com.example.movieapp.databinding.FragmentTablayoutMovieBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar

class MovieTabLayoutFragment(private val mode: Int) : Fragment() {

    private var _binding : FragmentTablayoutMovieBinding? = null
    private val binding get() = _binding!!

    private lateinit var spinnerCountry: Spinner
    private lateinit var spinnerGenre: Spinner
    private lateinit var spinnerAge: Spinner
    private lateinit var spinnerFees: Spinner
    private lateinit var spinnerYear: Spinner
    private lateinit var spinnerSort: Spinner
    private lateinit var buttonFilter: Button
    private lateinit var listMovie: RecyclerView

    private lateinit var TMDB_API_KEY : String

    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout cho fragment này
        _binding = FragmentTablayoutMovieBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerCountry = binding.spinnerCountry
        spinnerGenre = binding.spinnerGenre
        spinnerAge = binding.spinnerAge
        spinnerFees = binding.spinnerFees
        spinnerYear = binding.spinnerYear
        spinnerSort = binding.spinnerSort
        buttonFilter = binding.buttonFilter
        listMovie = binding.listMovie
        TMDB_API_KEY = BuildConfig.TMDB_API_KEY

        listMovie.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (!isLoading && !isLastPage && lastVisibleItem + 2 >= totalItemCount) {
                    loadMoreMovies()
                }
            }
        })

        detail()

        buttonFilter.setOnClickListener {
            currentPage = 1
            isLastPage = false
            isLoading = false
            setDetail()
        }

    }

    fun loadMoreMovies(){
        isLoading = true
        currentPage++
        getDetailTMDB("movie") { movies ->
            if (movies.isEmpty()) {
                isLastPage = true
            } else {
                val adapter = listMovie.adapter as MovieAdapter
                adapter.addMovies(movies)
                isLoading = false
            }
        }

    }

    fun detail(){
        // Tạo dữ liệu cho Spinner Quốc gia
        val countryItems = resources.getStringArray(R.array.country_array)
        val countryAdapter = ArrayAdapter(requireContext(),R.layout.spinner_item, countryItems)
        countryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerCountry.adapter = countryAdapter
        spinnerCountry.setSelection(0)

        // Tạo dữ liệu cho Spinner Thể loại
        val genreItems = resources.getStringArray(R.array.genre_array)
        val genreAdapter = ArrayAdapter(requireContext(),R.layout.spinner_item, genreItems)
        genreAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerGenre.adapter = genreAdapter
        spinnerGenre.setSelection(0)

        // Tạo dữ liệu cho Spinner Độ tuổi
        val ageItems = resources.getStringArray(R.array.age_array)
        val ageAdapter = ArrayAdapter(requireContext(),R.layout.spinner_item, ageItems)
        ageAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerAge.adapter = ageAdapter
        spinnerAge.setSelection(0)

        // Tạo dữ liệu cho Spinner Mức phí
        val feesItems = resources.getStringArray(R.array.fees_array)
        val feesAdapter = ArrayAdapter(requireContext(),R.layout.spinner_item, feesItems)
        feesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerFees.adapter = feesAdapter
        spinnerFees.setSelection(0)

        // Tạo dữ liệu cho Spinner Năm phát hành (ví dụ: 5 năm gần nhất)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        var yearItems : MutableList<String> = mutableListOf()
        yearItems.add("Tất cả")
        for (i in 0..4) {
            yearItems.add((currentYear - i).toString())
        }
        val yearAdapter = ArrayAdapter(requireContext(),R.layout.spinner_item, yearItems)
        yearAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter
        spinnerYear.setSelection(0)

        // Tạo dữ liệu cho Spinner Sắp xếp theo
        val sortItems = resources.getStringArray(R.array.sort_array)
        val sortAdapter = ArrayAdapter(requireContext(),R.layout.spinner_item, sortItems)
        sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerSort.adapter = sortAdapter
        spinnerSort.setSelection(0)

        setDetail()
    }

    fun setDetail(){
        if(mode == 1){
            getDetailTMDB("movie"){ movies ->
                setListMovie(movies)
            }
        }else if(mode == 2){
            getDetailTMDB("tv"){ movies ->
                setListMovie(movies)
            }
        }else if(mode == 3){
            val listMovie = mutableListOf<ItemMovie>()
            getDetailTMDB("movie"){ movies ->
                listMovie.addAll(movies)
                getDetailTMDB("tv"){ movie ->
                    listMovie.addAll(movie)
                    listMovie.sortByDescending {it.rating }
                    Log.d("MovieTabLayoutFragment","$listMovie")
                    setListMovie(listMovie)
                }
            }

        }
    }

    fun setListMovie(movies: List<ItemMovie>){
        Log.d("MovieTabLayoutFragment","$movies")
        val adapter = MovieAdapter(movies.toMutableList()){ movie ->
            val intent = Intent(requireContext(), MovieDetailActivity::class.java)
            intent.putExtra("movieId", movie.id.toString())
            intent.putExtra("type", movie.type) // truyền thêm loại
            startActivity(intent)
        }
        val gridLayout = GridLayoutManager(requireContext(), 3)
        listMovie.layoutManager = gridLayout
        listMovie.adapter = adapter
    }

    fun getDetailTMDB(type:String,callback: (List<ItemMovie>) -> Unit){
        val url = buildTmdbDiscoverUrl(type)
        Log.d("MovieTabLayoutFragment",url)
        val request = Request.Builder().url(url).build()

        OkHttpClient.Builder().build().newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Failed to fetch", e)
                activity?.runOnUiThread {
                    callback(emptyList())
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val results = json.getJSONArray("results")
                val movies = mutableListOf<ItemMovie>()
                for (i in 0 until minOf(15,results.length())){
                    val item = results.getJSONObject(i)
                    val id = item.getInt("id")
                    val title = if (type == "movie") item.getString("title") else item.getString("name")
                    val posterPath = item.getString("poster_path")
                    val fullPosterUrl = if (posterPath != null) "https://image.tmdb.org/t/p/w500$posterPath" else null
                    val releaseDate = if (type == "movie") item.getString("release_date") else item.getString("first_air_date")
                    val voteAverage = item.getDouble("vote_average")

                    movies.add(ItemMovie(id,title,type,releaseDate,voteAverage,fullPosterUrl))
                }
                activity?.runOnUiThread {
                    callback(movies)
                }
            }
        })
        return
    }

    private val mapAgeCode = mapOf(
        "Mọi lứa tuổi" to "P",
        "13+" to "PG",
        "16+" to "PG-13",
        "18+" to "R"
    )

    private val languageCodeMap = mapOf(
        "Việt Nam" to "vi",
        "Hàn Quốc" to "ko",
        "Mỹ" to "us",
        "Trung Quốc" to "cn",
        "Nhật Bản" to "ja"
    )

    private val sortMap = mapOf(
        "Mới nhất" to "release_date.desc",
        "Cũ nhất" to "release_date.asc",
        "Phổ biến nhất" to "popularity.desc",
        "Đánh giá cao" to "vote_average.desc",
        "Nhiều người đánh giá" to "vote_count.desc",
    )

    fun buildTmdbDiscoverUrl(type: String): String {
        val country = spinnerCountry.selectedItem.toString()
        val genre = spinnerGenre.selectedItem.toString()
        val age = spinnerAge.selectedItem.toString()
        val fees = spinnerFees.selectedItem.toString()
        val year = spinnerYear.selectedItem.toString()
        val sort = spinnerSort.selectedItem.toString()


        var baseUrl = "https://api.themoviedb.org/3/discover/$type"
        val params = mutableListOf("api_key=$TMDB_API_KEY")

        if (country != "Tất cả") {
            val languageCode = languageCodeMap[country] ?: "en"
            params.add("region=VN&with_original_language=$languageCode")
        }

        if (genre != "Tất cả") {
            val genreId = genreStringToId(genre)
            if (mode == 3) params.add("with_genres=$genreId,16") else params.add("with_genres=$genreId")
        } else if (mode == 3) {
            params.add("with_genres=16")
        }

        if (age != "Tất cả") {
            val ageCode = mapAgeCode[age] ?: "P"
            params.add("certification_country=US")
            params.add("certification.lte=$ageCode")
        }

        if (year != "Tất cả") {
            var baseString = ""
            if(type == "tv") baseString = "first_air_date" else baseString = "primary_release_date"
            params.add("$baseString.gte=$year-01-01")
            params.add("$baseString.lte=$year-12-31")
        }

        if (sort != "Mặc định") {
            val sortValue = sortMap[sort] ?: "popularity.desc"
            params.add("sort_by=$sortValue")
        }else{
            params.add("sort_by=vote_count.desc")
        }
        baseUrl = "$baseUrl?${params.joinToString("&")}"
        baseUrl = "$baseUrl&include_adult=false&language=vi-VN&page=$currentPage"

        return baseUrl
    }



    private fun genreStringToId(genreString: String): Int? {
        val genreMap = mapOf(
            "Hành động" to 28,
            "Phiêu lưu" to 12,
            "Hoạt hình" to 16,
            "Hài" to 35,
            "Tội phạm" to 80,
            "Tài liệu" to 99,
            "Chính kịch" to 18,
            "Gia đình" to 10751,
            "Giả tưởng" to 14,
            "Lịch sử" to 36,
            "Kinh dị" to 27,
            "Nhạc kịch" to 10402,
            "Bí ẩn" to 9648,
            "Lãng mạn" to 10749,
            "Khoa học viễn tưởng" to 878,
            "TV Movie" to 10770,
            "Gây cấn" to 53,
            "Chiến tranh" to 10752,
            "Viễn Tây" to 37,
            "Hành động & Phiêu lưu (TV)" to 10759,
            "Trẻ em" to 10762,
            "Tin tức" to 10763,
            "Thực tế" to 10764,
            "Khoa học viễn tưởng & Giả tưởng (TV)" to 10765,
            "Phim dài tập" to 10766,
            "Talk Show" to 10767
        )
        return genreMap[genreString] ?: null
    }


}
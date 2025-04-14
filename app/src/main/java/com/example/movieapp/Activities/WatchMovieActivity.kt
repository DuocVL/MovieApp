package com.example.movieapp.Activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer
import com.example.movieapp.Dataclass.Movie
import com.example.movieapp.R
import androidx.media3.common.MediaItem
import com.example.movieapp.databinding.ActivityWatchMovieBinding
import androidx.media3.ui.PlayerView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieapp.Adapters.EpisodeAdapter
import com.example.movieapp.Adapters.MovieAdapter
import com.example.movieapp.BuildConfig
import com.example.movieapp.Dataclass.Episode
import com.example.movieapp.Dataclass.ItemMovie
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


@UnstableApi
class WatchMovieActivity : AppCompatActivity() {


    private lateinit var playerView: PlayerView
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var controlsLayout: ConstraintLayout
    private lateinit var fullscreenButton: ImageButton
    private lateinit var replayButton: ImageButton
    private lateinit var forwardButton: ImageButton

    private var movie: Movie? = null
    private var isFullscreen = false

    private lateinit var binding: ActivityWatchMovieBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWatchMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            movie = intent.getParcelableExtra("movie",Movie::class.java)
        }else{
            movie = intent.getParcelableExtra("movie")
        }


        playerView = binding.playerView
        controlsLayout = binding.playerView.findViewById(R.id.watchMovieControls)
        fullscreenButton = controlsLayout.findViewById(R.id.btnFullscreen)

        // khởi tạo và Gán ExoPlayer cho PlayerView
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer
        // Lấy video URL từ Firestore
        getVideoUrlFromFirestore()

        if(movie != null){
            setDeailMovie(movie!!)
        }else{
            Log.e("WatchMovieActivity", "Movie is null, unable to load video.")
        }


//        // Click vào video để ẩn/hiện controls
//        playerView.setOnClickListener {
//            toggleControlsVisibility()
//        }
//
        // Click để chuyển chế độ fullscreen
        fullscreenButton.setOnClickListener {
            toggleFullscreen()
        }
//
//        //Click để pause/play
//        pauseButton.setOnClickListener {
//            if(exoPlayer.isPlaying){
//                exoPlayer.pause()
//                pauseButton.setImageResource(R.drawable.play_circle)
//            }else{
//                exoPlayer.play()
//                pauseButton.setImageResource(R.drawable.pause)
//            }
//        }
//
//        //Tua lùi 10 giây
//        replayButton.setOnClickListener {
//            val position = exoPlayer.currentPosition // Lấy vị trí hiện tại của video
//            exoPlayer.seekTo((position - 10000).coerceAtLeast(0)) // Quay lại 10 giây trước hoặc về vị trí ban đầu
//        }
//
//        //Tua tiến 10 giây
//        forwardButton.setOnClickListener {
//            val position = exoPlayer.currentPosition // Lấy vị trí hiện tại của video
//            val duration = exoPlayer.duration// Lấy độ dài của video
//            exoPlayer.seekTo((position + 10000).coerceAtMost(duration))//Tien den 10s hoac cuoi video
//        }
    }


    // Tự động chuyển chế độ toàn màn hình khi xoay ngang
    @UnstableApi
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && isFullscreen) {
            // Đặt resizeMode thành FILL khi vào chế độ toàn màn hình
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            enterFullscreen()
        }else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && isFullscreen){
            // Đặt resizeMode thành FIT khi ra khỏi chế độ toàn màn hình
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }

    private fun setDeailMovie(movie : Movie){
        binding.Title.setText(movie.title)
        binding.overview.setText(movie.overview)
        val episodes : MutableList<Episode> = mutableListOf()
        episodes.addAll(
            listOf(
                Episode(1,"https://videos.pexels.com/video-files/31532147/13439835_2560_1440_60fps.mp4"),
                Episode(2,"https://videos.pexels.com/video-files/29660257/12759646_2560_1440_60fps.mp4"),
                Episode(3,"https://videos.pexels.com/video-files/31245234/13344953_2560_1440_30fps.mp4")
            )
        )
        val adapter = EpisodeAdapter(episodes) { url ->
            setupVideoPlayer(url)
        }
        binding.episodes.adapter = adapter
        binding.episodes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)

        getRecomentMovie(movie.id.toString(),movie.type,callback = { movies ->
            val adapter = MovieAdapter(movies) { movie ->
                val intent = Intent(this, MovieDetailActivity::class.java)
                intent.putExtra("movieId", movie.id.toString())
                intent.putExtra("type", movie.type)
                startActivity(intent)
            }
            binding.recommentmovie.adapter = adapter
            binding.recommentmovie.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)

        })
    }

    private fun getRecomentMovie(movieId : String,type : String,callback: (List<ItemMovie>) -> Unit){
        val url = "https://api.themoviedb.org/3/$type/$movieId/recommendations?api_key=${BuildConfig.TMDB_API_KEY}&language=vi-VN&page=1"
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TMDB", "Failed to fetch movie details", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val results = json.getJSONArray("results")
                val movies = mutableListOf<ItemMovie>()
                for(i in 0 until minOf(8,results.length())){
                    val movieObject = results.getJSONObject(i)
                    val id = movieObject.getInt("id")
                    val title = movieObject.getString("title")
                    val posterPath = movieObject.getString("poster_path")
                    val fullPosterUrl = "https://image.tmdb.org/t/p/w500$posterPath"
                    val releaseDate = movieObject.getString("release_date")
                    val voteAverage = movieObject.getDouble("vote_average")
                    val movie = ItemMovie(id,title,"movie",releaseDate,voteAverage,fullPosterUrl)

                    movies.add(movie)
                    runOnUiThread {
                        callback(movies)
                    }
                }
            }
        })
    }

    private fun getVideoUrlFromFirestore() {
//        val db = FirebaseFirestore.getInstance()
//        db.collection("videos")
//            .get()
//            .addOnSuccessListener { result ->
//                for (document in result) {
//                    videoUrl = document.getString("videoURL") // Lấy videoUrl
//                    if (videoUrl != null) {
//                        Log.d("Firestore", "Video URL: $videoUrl")
//                        setupVideoPlayer(videoUrl)
//                    } else {
//                        Log.w("Firestore", "videoUrl is null in document ${document.id}")
//                    }
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.w("Firestore", "Error getting documents: ", exception)
//            }
        setupVideoPlayer("https://videos.pexels.com/video-files/30792455/13170533_2560_1440_30fps.mp4")
    }

    private fun setupVideoPlayer(videoUrl: String?) {
        videoUrl?.let {
            // Tạo MediaItem từ URL video
            val mediaItem = MediaItem.fromUri(it)

            // Cài đặt nguồn phát video
            exoPlayer.setMediaItem(mediaItem)

            // Phát video
            exoPlayer.prepare()
            exoPlayer.play()
        } ?: run {
            Log.e("WatchMovieActivity", "Video URL is null, unable to load video.")
        }
    }

    private fun toggleFullscreen(){
        if(isFullscreen){
            exitFullscreen()
            fullscreenButton.setImageResource(R.drawable.full_screen)
        }else{
            enterFullscreen()
            fullscreenButton.setImageResource(R.drawable.small_screen)
        }
    }

    private fun enterFullscreen(){
        isFullscreen = true
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }else{
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        val params = playerView.layoutParams
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        playerView.layoutParams = params
    }

    private fun exitFullscreen(){
        isFullscreen = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }else{
            @Suppress("DEPRECATION")
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        val scale = resources.displayMetrics.density
        val heightInDp = (300 * scale + 0.5f).toInt()
        val params = playerView.layoutParams
        params.height = heightInDp
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        playerView.layoutParams = params
    }


    override fun onPause() {
        super.onPause()
        exoPlayer.pause() // Dừng ExoPlayer khi activity bị pause
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release() // Giải phóng tài nguyên của ExoPlayer khi activity bị destroy
    }
}

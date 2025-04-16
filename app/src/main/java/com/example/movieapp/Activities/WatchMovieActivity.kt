package com.example.movieapp.Activities

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer
import com.example.movieapp.Dataclass.Movie
import com.example.movieapp.R
import androidx.media3.common.MediaItem
import com.example.movieapp.databinding.ActivityWatchMovieBinding
import androidx.media3.ui.PlayerView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieapp.Adapters.EpisodeAdapter
import com.example.movieapp.AppDatabase
import com.example.movieapp.BuildConfig
import com.example.movieapp.Dataclass.DownloadedVideo
import com.example.movieapp.Dataclass.Episode
import com.example.movieapp.Dataclass.ItemMovie
import com.example.movieapp.Dataclass.WatchLaterMovie
import com.example.movieapp.DownloadViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@UnstableApi
class WatchMovieActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var controlsLayout: ConstraintLayout
    private lateinit var fullscreenButton: ImageButton

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = auth.currentUser

    private var movie: Movie? = null
    private var isFullscreen = false
    private var isExpanded = false //Mo rong hay khong
    private var initialMaxLines = 3 // Số dòng tối đa ban đầu


    private var saveStatus : Boolean = false

    private lateinit var binding: ActivityWatchMovieBinding

    private var dowloadViewModel = DownloadViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWatchMovieBinding.inflate(layoutInflater)


        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            movie = intent.getParcelableExtra("movie",Movie::class.java)
        }else{
            movie = intent.getParcelableExtra("movie")
        }
        Toast.makeText(this,"${movie?.id}",Toast.LENGTH_SHORT).show()

        playerView = binding.playerView
        controlsLayout = binding.playerView.findViewById(R.id.watchMovieControls)
        fullscreenButton = controlsLayout.findViewById(R.id.btnFullscreen)

        if (currentUser != null && movie?.id != null) {
            checkIfMovieSaved()
            //updateFavoriteButtonState()
        } else {
            // Xử lý trường hợp không có người dùng hoặc ID phim
        }

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

        // Click để chuyển chế độ fullscreen
        fullscreenButton.setOnClickListener {
            toggleFullscreen()
        }

        //Click bookmark để lưu vào danh sách xem sau
        binding.bookmarkButton.setOnClickListener {
            // Thực hiện hành động lưu vào danh sách xem sau
            saveWatchLater(movie!!)
        }

        //Click tai xuong để tải xuống
        binding.dowloadButton.setOnClickListener {
//            binding.downloadProgressBar.visibility = View.VISIBLE
//            binding.dowloadButton.visibility = View.GONE
//            if(!dowloadViewModel.isDownloaded(movie?.id.toString())){
//                dowloadVideo(this,"https://videos.pexels.com/video-files/31245234/13344953_2560_1440_30fps.mp4","${movie?.id.toString()}"){ filePath ->
//                    val video = DownloadedVideo(movie?.id.toString(),movie?.type.toString(),filePath,1)
//                    Log.d("WatchMovieActivity", "id movie: ${movie?.id}")
//                    dowloadViewModel.addVideo(video)
//                    Toast.makeText(this, "Tải thành công!", Toast.LENGTH_SHORT).show()
//                    binding.downloadProgressBar.visibility = View.GONE
//                    binding.dowloadButton.visibility = View.VISIBLE
//                    binding.dowloadButton.setImageResource(R.drawable.download_done)
//                }
//            }else{
//                Toast.makeText(this,"Đã tải xuống",Toast.LENGTH_SHORT).show()
//            }
            if(movie == null){
                Log.e("WatchMovieActivity", "Movie is null, unable to load video.")
            }
            val movieId = movie?.id.toString()
            val videoUrl = "https://videos.pexels.com/video-files/31245234/13344953_2560_1440_30fps.mp4"
            binding.downloadProgressBar.visibility = View.VISIBLE
            binding.dowloadButton.visibility = View.GONE
            dowloadVideo(this,videoUrl,movieId){ filePath ->
                val videoData = DownloadedVideo(
                    id = movieId,
                    title = movie?.title ?: "",
                    type = movie?.type ?: "",
                    episode = 1,
                    posterUrl = movie?.posterPath ?: "",
                    localVideoPath = filePath
                )

                // ✅ DÙNG coroutine lifecycleScope thay vì Thread
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getDatabase(this@WatchMovieActivity).downloadedVideoDao().insert(videoData)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WatchMovieActivity, "Tải thành công!", Toast.LENGTH_SHORT).show()
                        binding.downloadProgressBar.visibility = View.GONE
                        binding.dowloadButton.visibility = View.VISIBLE
                        binding.dowloadButton.setImageResource(R.drawable.download_done)
                    }
                }
            }
        }
    }

    private fun saveWatchLater(movie: Movie){
        if(saveStatus){
            currentUser?.uid?.let { userId ->
                movie?.id?.let { movieId ->
                    firestore.collection("users")
                        .document(userId)
                        .collection("watchLater")
                        .document(movieId.toString())
                        .delete()
                        .addOnSuccessListener {
                            saveStatus = false
                            updateFavoriteButtonState()
                        }

                }
            }
        }else{
            currentUser?.uid?.let { userId ->
                movie?.id?.let { movieId ->
                    val watchLaterMovie = WatchLaterMovie(movieId.toString(),movie.type,"1")
                    firestore.collection("users")
                        .document(userId)
                        .collection("watchLater")
                        .document(movieId.toString())
                        .set(watchLaterMovie)
                        .addOnSuccessListener {
                            saveStatus = true
                            updateFavoriteButtonState()
                        }
                        .addOnFailureListener {
                            saveStatus = false
                        }
                }
            }

        }
    }

    private fun checkIfMovieSaved() {
        currentUser?.uid?.let { userId ->
            movie?.id?.let { movieId ->
                firestore.collection("users")
                    .document(userId)
                    .collection("watchLater")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            if (document.id == movieId.toString()) {
                                saveStatus = true
                                updateFavoriteButtonState()
                                return@addOnSuccessListener
                            }
                        }
                        saveStatus = false
                        updateFavoriteButtonState()
                    }
                    .addOnFailureListener {
                        Log.e("WatchMovieActivity", "Error checking if movie is saved", it)
                        saveStatus = false
                        updateFavoriteButtonState()
                    }
            }
        }
    }

    private fun updateFavoriteButtonState() {
        if (saveStatus) {
            binding.bookmarkButton.setImageResource(R.drawable.bookmark) // Icon đã lưu
            binding.bookmarkButton.tag = "saved"
        } else {
            binding.bookmarkButton.setImageResource(R.drawable.bookmark_border) // Icon chưa lưu
            binding.bookmarkButton.tag = "not_saved"
        }
    }


    // Tự động chuyển chế độ toàn màn hình khi xoay ngang
    @UnstableApi
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            enterFullscreen()
        }else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){

            exitFullscreen()
        }
    }



    private fun setDeailMovie(movie : Movie){

        binding.Title.setText(movie.title)

        binding.overview.setText(movie.overview)
        binding.overview.maxLines = initialMaxLines
        //Kiem tra xem overview có nhiều dòng không
        binding.overview.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (binding.overview.lineCount > initialMaxLines && !isExpanded) {
                    binding.seeMoretextView.visibility = View.VISIBLE
                    binding.seeMoretextView.setOnClickListener { toggleOverview() }
                } else {
                    binding.seeMoretextView.visibility = View.GONE
                    binding.seeMoretextView.setOnClickListener(null)
                }
                binding.overview.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        val episodes : MutableList<Episode> = mutableListOf()
        episodes.addAll(
            listOf(
                Episode(1,"https://videos.pexels.com/video-files/31532147/13439835_2560_1440_60fps.mp4"),
                Episode(2,"https://videos.pexels.com/video-files/29660257/12759646_2560_1440_60fps.mp4"),
                Episode(3,"https://videos.pexels.com/video-files/31245234/13344953_2560_1440_30fps.mp4"),
                Episode(4,"https://drive.google.com/uc?export=download&id=1tbKsbX0WJdwoMk-8tFTvz77f7RFthV-e")
            )
        )
        val adapter = EpisodeAdapter(episodes) { url ->
            setupVideoPlayer(url)
        }
        binding.episodes.adapter = adapter
        binding.episodes.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)

//        getRecomentMovie(movie.id.toString(),movie.type,callback = { movies ->
//            val adapter = MovieAdapter(movies) { movie ->
//                val intent = Intent(this, MovieDetailActivity::class.java)
//                intent.putExtra("movieId", movie.id.toString())
//                intent.putExtra("type", movie.type)
//                startActivity(intent)
//            }
//            binding.recommentmovie.adapter = adapter
//            binding.recommentmovie.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
//
//        })
    }

    //Ham chuyen doi mo rong va thu nho
    private fun toggleOverview(){
        isExpanded = !isExpanded
        if(isExpanded){
            binding.overview.maxLines = Int.MAX_VALUE
            binding.seeMoretextView.setText("Thu gọn")
        }else {
            binding.overview.maxLines = initialMaxLines
            binding.seeMoretextView.setText("Xem thêm")
        }
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
        // 1. Ẩn nội dung dưới video
        binding.nestedScrollView.visibility = View.GONE
        playerView.visibility = View.VISIBLE
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
        // 1. Hiện lại layout
        binding.nestedScrollView.visibility = View.VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }else{
            @Suppress("DEPRECATION")
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        // 3. Gán lại 16:9
        val params = playerView.layoutParams
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        val height = (resources.displayMetrics.widthPixels * 9 / 16.0).toInt()
        params.height = height
        binding.playerView.layoutParams = params
    }

    fun dowloadVideo(context: Context,videoUrl: String,fileName: String,onDownloaded: (filePath: String) -> Unit){
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        if(!directory!!.exists()) directory.mkdirs() //Neu khong ton tai thi tao thu muc

        val file = File(directory,"$fileName.mp4")

        val request = Request.Builder().url(videoUrl).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("WatchMovieActivity", "Failed to download video", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body
                val contentLength = body?.contentLength() ?: 0
                val inputStream = body?.byteStream()

                var bytesRead = 0L
                val buffer = ByteArray(8192)
                val outputStream = FileOutputStream(file)

                inputStream?.use { input ->
                    outputStream.use { output ->
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            bytesRead += read

                            // Cập nhật tiến trình
                            val progress =
                                if (contentLength > 0) (bytesRead * 100 / contentLength).toInt() else -1
                            Handler(Looper.getMainLooper()).post {
                                if (progress >= 0) {
                                    binding.downloadProgressBar.progress = progress
                                }
                            }
                        }
                    }
                }
                Handler(Looper.getMainLooper()).post {
                    onDownloaded(file.absolutePath)
                }
            }
        })

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

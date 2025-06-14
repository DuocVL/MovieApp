package com.example.movieapp.Activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
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
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer
import com.example.movieapp.Dataclass.Movie
import com.example.movieapp.R
import androidx.media3.common.MediaItem
import com.example.movieapp.databinding.ActivityWatchMovieBinding
import androidx.media3.ui.PlayerView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieapp.Adapters.EpisodeAdapter
import com.example.movieapp.Adapters.MovieAdapter
import com.example.movieapp.AppDatabase
import com.example.movieapp.AppSessionViewModel
import com.example.movieapp.BuildConfig
import com.example.movieapp.Dataclass.DownloadedVideo
import com.example.movieapp.Dataclass.Episode
import com.example.movieapp.Dataclass.ItemMovie
import com.example.movieapp.Dataclass.MovieWatching
import com.example.movieapp.Dataclass.WatchLaterMovie
import com.example.movieapp.Fragment.CommentFragment
import com.example.movieapp.Fragment.RatingFragment
import com.example.movieapp.SessionManager
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

    private lateinit var scrollView: NestedScrollView
    private lateinit var playerView: PlayerView
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var controlsLayout: ConstraintLayout
    private lateinit var fullscreenButton: ImageButton
    private lateinit var commentLayout: FrameLayout
    private lateinit var commentInputLayout : LinearLayout
    private lateinit var sendButton : ImageButton
    private lateinit var commentFragment: CommentFragment

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = auth.currentUser

    private var movie: Movie? = null
    private var isFullscreen = false
    private var isExpanded = false //Mo rong hay khong
    private var initialMaxLines = 3 // Số dòng tối đa ban đầu
    private val userId = currentUser?.uid


    private var saveStatus : Boolean = false
    private var downloadStatus : Boolean = false
    private var videoUrl :String? = null
    private var listEpisode : MutableList<Episode>? = null
    private var episode : Int = 1
    private var progress : Long = 0
    private lateinit var appSessionViewModel: AppSessionViewModel
    private lateinit var sessionManager: SessionManager
    private var packageVipStatus : Boolean = false

    private lateinit var binding: ActivityWatchMovieBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWatchMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            movie = intent.getParcelableExtra("movie",Movie::class.java)
        }else{
            movie = intent.getParcelableExtra("movie")
        }
        Log.d("WatchMovieActivity", "Movie: $movie")

        scrollView = binding.nestedScrollView
        playerView = binding.playerView
        controlsLayout = binding.playerView.findViewById(R.id.watchMovieControls)
        fullscreenButton = controlsLayout.findViewById(R.id.btnFullscreen)
        commentLayout = binding.commentLayout
        commentInputLayout = binding.commentInputLayout
        sendButton = binding.sendButton

        appSessionViewModel = AppSessionViewModel(application)
        sessionManager = SessionManager(this)

        appSessionViewModel.getPackageVip {
            packageVipStatus = it
            getVideoUrlFromFirestore(callback = { list ->
                listEpisode = list
                if(movie?.type.equals("movie")){
                    binding.episodes.setText("1/1 tập")
                }else{
                    binding.episodes.setText("${listEpisode?.size}/${movie?.runtime} tập")
                }
                if(listEpisode.isNullOrEmpty()){
                    Log.e("WatchMovieActivity", "Video URL list is null or empty")
                    Toast.makeText(this,"Video URL list is null or empty",Toast.LENGTH_SHORT).show()
                }else{
                    listEpisode?.let { list ->
                        Log.d("WatchMovieActivity", "Video URL list: ${listEpisode}")
                        if(movie?.vip == true){
                            if (packageVipStatus || movie?.buy == true){
                                getMovieProgress()
                            }else{
                                showDialogBuyPackage()
                            }
                        }else{
                            if(appSessionViewModel.isAnonymous()){
                                this.videoUrl = list[0].url
                                episode = 1
                                setTitle()
                                setupVideoPlayer(videoUrl)
                            }else{
                                getMovieProgress()
                            }
                        }
                        setEpisode()
                    }
                }
            })
        }


        // Ẩn commentInputLayout ban đầu
        commentInputLayout.visibility = View.GONE

        //Kiem tra xem commentLayout co hien thi hay khong
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val rect = Rect()
            commentLayout.getGlobalVisibleRect(rect)

            // Kiểm tra xem commentLayout có bất kỳ phần nào hiển thị trên màn hình không
            val isVisible = rect.height() > 0 &&
                    rect.width() > 0 &&
                    rect.bottom > scrollView.scrollY &&
                    rect.top < scrollView.scrollY + scrollView.height

            commentInputLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
        }

        // khởi tạo và Gán ExoPlayer cho PlayerView
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer

        // Lấy video URL từ Firestore
        if(movie != null && currentUser != null){
            checkIfMovieSaved()
            if(packageVipStatus){
                showDialogBuyPackage()
            }else{
                checkIfMovieDowload()
            }
            setDetailMovie(movie!!)
        }else{
            Log.e("WatchMovieActivity", "Movie is null, unable to load video.")
        }



        // Click để chuyển chế độ fullscreen
        fullscreenButton.setOnClickListener {
            binding.commentInputLayout.visibility = View.GONE
            toggleFullscreen()
        }

        //Click bookmark để lưu vào danh sách xem sau
        binding.bookmarkButton.setOnClickListener {
            // Thực hiện hành động lưu vào danh sách xem sau
            if(appSessionViewModel.isAnonymous()){
                showDialogLogin()
                return@setOnClickListener
            }else{
                saveWatchLater(movie!!)
            }
        }

        //Click favorit de danh gia phim
        binding.rating.setOnClickListener {
            val ratingFragment = RatingFragment()
            val bundle = Bundle()
            bundle.putString("movieId", movie?.id.toString())
            // Bạn có thể put các kiểu dữ liệu khác nhau vào Bundle
            ratingFragment.arguments = bundle
            ratingFragment.show(supportFragmentManager, "RatingFragment")
        }

        //Click senButton de gui binh luan
        sendButton.setOnClickListener {
            if(appSessionViewModel.isAnonymous()){
                showDialogLogin()
                return@setOnClickListener
            }else{
                val content = binding.commentEditText.text.toString().trim()
                if(content.isNullOrEmpty()){
                    Toast.makeText(this,"Vui lòng nhập bình luận",Toast.LENGTH_SHORT).show()
                }else{
                    commentFragment.addComment(content)
                    binding.commentEditText.setText("")
                }
            }
        }

        //Click tai xuong để tải xuống
        binding.dowloadButton.setOnClickListener {
            if(appSessionViewModel.isAnonymous()){
                showDialogLogin()
                return@setOnClickListener
            }
            if(movie?.vip == true && !packageVipStatus && movie?.buy == false){
                showDialogBuyPackage()
                return@setOnClickListener
            }
            if(movie == null){
                Log.e("WatchMovieActivity", "Movie is null, unable to load video.")
            }
            if(videoUrl == null){
                Log.e("WatchMovieActivity", "Video URL is null")
                Toast.makeText(this,"Video URL is null",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                if(downloadStatus){
                    deleteMovie(movie?.id.toString())
                }else{
                    val movieId = movie?.id.toString()
                    binding.downloadProgressBar.visibility = View.VISIBLE
                    binding.dowloadButton.visibility = View.GONE
                    dowloadVideo(this,videoUrl!!,"${movieId}_${episode}"){ filePath ->
                        lifecycleScope.launch {
                            val movieDatabase = AppDatabase.getDatabase(this@WatchMovieActivity).downloadedVideoDao().getById(movieId)
                            if(movieDatabase != null){
                                val posterPath = movieDatabase.localPosterPath
                                insertDownloadedMovie(filePath,posterPath)
                            }else{
                                dowloadImage(this@WatchMovieActivity,movie?.posterPath!!,"${movieId}_poster"){  posterPath ->
                                    insertDownloadedMovie(filePath,posterPath)
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    private fun setTitle(){
        if (movie?.type.equals("movie")){
            if(movie?.vip == true){
                binding.Title.setText("${movie?.title} - VIP")
            }else{
                binding.Title.setText("${movie?.title}")
            }
        }else{
            if(movie?.vip == true){
                binding.Title.setText("${movie?.title} - Tập ${episode} - VIP")
            }else{
                binding.Title.setText("${movie?.title} - Tập ${episode}")
            }
        }
    }

    private fun showDialogBuyPackage(){
        val dialog = AlertDialog.Builder(this)
            .setTitle("Thông báo")
            .setMessage("Bạn cần mua gói VIP ?")
            .setIcon(R.drawable.ic_help)
            .setPositiveButton("Mua ngay") { _, _ ->
                val intent = Intent(this,PackagePaymentActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Hủy"){ _, _ ->
                // Không làm gì khi người dùng chọn hủy
            }
        dialog.show()
    }

    private fun showDialogLogin(){
        val dialog = AlertDialog.Builder(this)
            .setTitle("Thông báo")
            .setMessage("Bạn cần đăng nhập để mua gói ?")
            .setIcon(R.drawable.ic_help)
            .setPositiveButton("Đăng nhập") { _, _ ->
                sessionManager.clearSession()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Hủy"){ _, _ ->
                // Không làm gì khi người dùng chọn hủy
            }
        dialog.show()
    }

    private fun getMovieProgress(){
        firestore.collection("users")
            .document(userId!!)
            .collection("movieProgress")
            .document(movie?.id.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    progress = document.getLong("progress") ?: 0
                    episode = document.getLong("episode")?.toInt() ?: 1
                    videoUrl = listEpisode!![episode - 1].url
                    setTitle()
                    Log.d("WatchMovieActivity", "videoUrl: $videoUrl, episode: $episode, progress: $progress")
                    val mediaItem = MediaItem.fromUri(videoUrl!!)
                    exoPlayer.setMediaItem(mediaItem)
                    // Phát video
                    exoPlayer.prepare()
                    exoPlayer.seekTo(progress)
                    exoPlayer.play()
                } else {
                    Log.d("WatchMovieActivity", "No such document")
                }
            }
            .addOnFailureListener {
                Log.d("WatchMovieActivity", "get failed with ", it)
                progress = 0
                episode = 1
                videoUrl = listEpisode!![episode - 1].url
            }

        return
    }


    private fun insertDownloadedMovie(videoPath:String,posterPath:String){
        val downloadedVideo = DownloadedVideo(movie?.id.toString(),movie?.title!!,movie?.type!!,episode,posterPath,videoPath)
        lifecycleScope.launch {
            AppDatabase.getDatabase(this@WatchMovieActivity).downloadedVideoDao().insert(downloadedVideo)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@WatchMovieActivity, "Tải thành công!", Toast.LENGTH_SHORT).show()
                downloadStatus = true
                binding.downloadProgressBar.visibility = View.GONE
                binding.dowloadButton.visibility = View.VISIBLE
                binding.dowloadButton.setImageResource(R.drawable.download_done)
                binding.downloadTitle.setText("Đã tải")
            }

        }
    }

    private fun saveWatchLater(movie: Movie){
        if(saveStatus){
            val dialog = AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setIcon(R.drawable.ic_help)
                .setMessage("Bạn có muốn xóa phim khỏi danh sách xem sau ?")
                .setPositiveButton("Xóa") { _, _ ->
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
                                .addOnFailureListener {
                                    Log.e("WatchMovieActivity", "Error deleting movie from watch later", it)
                                }
                        }
                    }
                }
                .setNegativeButton("Hủy") { _, _ ->
                    // Không làm gì khi người dùng chọn hủy
                }
            dialog.show()

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

    //Cập nhật trạng thái của nút bookmark
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

    private fun setDetailComment(){
        commentFragment = CommentFragment()
        val bundle = Bundle()
        bundle.putString("movieId", movie?.id.toString())
        // Bạn có thể put các kiểu dữ liệu khác nhau vào Bundle

        commentFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.commentLayout,commentFragment)
            .commit()
    }

    //Ham hien thi thong tin phim
    private fun setDetailMovie(movie : Movie){
        binding.ratingButton.rating = movie.voteAverage.toFloat()/10
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

        getRecomentMovie(movie.id.toString(),movie.type,callback = { movies ->
            val adapter = MovieAdapter(movies.toMutableList()) { movie ->
                val intent = Intent(this, MovieDetailActivity::class.java)
                intent.putExtra("movieId", movie.id.toString())
                intent.putExtra("type", movie.type)
                intent.putExtra("vip", movie.vip)
                startActivity(intent)
            }
            binding.recommentmovie.adapter = adapter
            binding.recommentmovie.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)

        })
        setDetailComment()
    }

    //Ham hien thi danh sach cac tap
    private fun setEpisode(){
        val adapter = EpisodeAdapter(listEpisode!!.toList()) { url,episode ->
            if(!packageVipStatus && movie?.buy == false && movie?.vip == true){
                showDialogBuyPackage()
                return@EpisodeAdapter
            }
            setupVideoPlayer(url)
            videoUrl = url
            this.episode = episode
            setTitle()
            checkIfMovieDowload()
        }
        binding.episodesList.adapter = adapter
        binding.episodesList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
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

    //Ham lay danh sach phim lien quan
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
                    val title = if(type.equals("movie")) movieObject.getString("title") else movieObject.getString("name")
                    val posterPath = movieObject.getString("poster_path")
                    val fullPosterUrl = "https://image.tmdb.org/t/p/w500$posterPath"
                    val releaseDate = if(type.equals("movie")) movieObject.getString("release_date") else movieObject.getString("first_air_date")
                    val voteAverage = movieObject.getDouble("vote_average")
                    checkVip(id.toString(),type) { status ->
                        val movie = ItemMovie(id,title,type,releaseDate,voteAverage,fullPosterUrl,status)
                        movies.add(movie)
                        runOnUiThread {
                            callback(movies)
                        }
                    }
                }
            }
        })
    }

    private fun checkVip(movieId: String,type: String,callback: (Boolean) -> Unit){
        firestore.collection("vip")
            .document("$movieId")
            .get()
            .addOnSuccessListener { document ->
                val vip = document.getBoolean("vip") ?: false
                val type = document.getString("type") ?: "movie"
                val id = document.getString("movieId") ?: ""
                if(type.equals(type) && id.equals(movieId)){
                    callback(vip)
                }else{
                    callback(false)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    //Ham lay video url tu firestore
    private fun getVideoUrlFromFirestore(callback: (MutableList<Episode>?) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val movieId = movie?.id.toString()
        if(movieId == null){
            Log.e("WatchMovieActivity", "Movie ID is null")
            Toast.makeText(this,"Movie ID is null",Toast.LENGTH_SHORT).show()
            callback(null)
        }
        firestore.collection("movie")
            .document("$movieId")
            .collection("movieURL")
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    val list = mutableListOf<Episode>()
                    for(document in documents){
                        val episode = document.id
                        val url = document.getString("URL")
                        list.add(Episode(episode.toInt(),url!!))
                        Log.d("WatchMovieActivity", "Episode: $episode, URL: $url")
                        if(list.size == documents.size()){
                            callback(list)
                        }
                    }
                }else{
                    Log.e("WatchMovieActivity", "No such document")
                    Toast.makeText(this,"No such document",Toast.LENGTH_SHORT).show()
                    callback(null)
                }
            }
            .addOnFailureListener {
                Log.e("WatchMovieActivity", "Error fetching video URL", it)
            }

    }

    //Ham khoi tao ExoPlayer
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

    //Ham chuyen doi che do fullscreen
    private fun toggleFullscreen(){
        if(isFullscreen){
            exitFullscreen()
            fullscreenButton.setImageResource(R.drawable.full_screen)
        }else{
            enterFullscreen()
            fullscreenButton.setImageResource(R.drawable.small_screen)
        }
    }

    //Ham bat dau che do fullscreen
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

    //Ham thoat khoi che do fullscreen
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

    //Ham kiem tra xem phim da tai xuong chua
    private fun checkIfMovieDowload(){
        lifecycleScope.launch {
            val movieId = movie?.id.toString()
            val video = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(this@WatchMovieActivity).downloadedVideoDao()
                    .getById(movieId,episode)
            }
            if (video != null) {
                binding.dowloadButton.setImageResource(R.drawable.download_done)
                binding.downloadProgressBar.visibility = View.GONE
                binding.downloadTitle.setText("Đã tải")
                downloadStatus = true
            }else{
                binding.dowloadButton.setImageResource(R.drawable.download)
                binding.downloadProgressBar.visibility = View.GONE
                binding.downloadTitle.setText("Tải xuống")
                downloadStatus = false
            }
        }
    }


    //Ham xoa phim
    private fun deleteMovie(movieId: String){
        lifecycleScope.launch {
            val downloadedVideo = AppDatabase.getDatabase(this@WatchMovieActivity).downloadedVideoDao().getById(movieId,episode)
            if (downloadedVideo != null) {
                AppDatabase.getDatabase(this@WatchMovieActivity).downloadedVideoDao().deleteById(movieId,episode)
                val file = File(downloadedVideo.localVideoPath)
                file.delete()
                downloadStatus = false
                checkIfMovieDowload()
                Toast.makeText(this@WatchMovieActivity,"Xóa thành công",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@WatchMovieActivity,"Không tìm thấy phim",Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Haàm tải xuống video
    private fun dowloadVideo(context: Context,videoUrl: String,fileName: String,onDownloaded: (filePath: String) -> Unit){
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

    //Ham download anh
    private fun dowloadImage(context: Context,imageUrl: String,fileName: String,onDownloaded: (filePath: String) -> Unit){
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        if(!directory!!.exists()) directory.mkdirs() // Tao thu muc neu chua co

        val file = File(directory,"$fileName.jpg")
        val request = Request.Builder().url(imageUrl).build()

        OkHttpClient().newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("WatchMovieActivity", "Failed to download image", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val inputStream = response.body?.byteStream()
                if(inputStream == null){
                    Log.e("WatchMovieActivity", "Response body is null")
                    return
                }
                val outputStream = FileOutputStream(file)
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                Handler(Looper.getMainLooper()).post{
                    onDownloaded(file.absolutePath)
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.pause() // Dừng ExoPlayer khi activity bị pause
        Log.d("WatchMovieActivity", "onPause() called")
        val position = exoPlayer.currentPosition
        val length = exoPlayer.duration
        Log.d("WatchMovieActivity", "Current position: $position")
        if(userId.isNullOrEmpty()){
            Log.e("WatchMovieActivity","User ID is null or empty")
        }else{
            if(!packageVipStatus && movie?.buy == false && movie?.vip == true){
                return
            }
            firestore.collection("users")
                .document(userId)
                .collection("movieProgress")
                .document(movie?.id.toString())
                .set(MovieWatching(movie?.id.toString(),movie?.type!!,position,length,episode,movie?.backdropPath,movie?.vip!!))
                .addOnSuccessListener {
                    Log.d("WatchMovieActivity","Movie progress updated successfully")
                }
                .addOnFailureListener {
                    Log.e("WatchMovieActivity","Failed to update movie progress",it)
                }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release() // Giải phóng tài nguyên của ExoPlayer khi activity bị destroy
        Log.d("WatchMovieActivity", "onDestroy() called")
        val position = exoPlayer.currentPosition
        val length = exoPlayer.duration
        Log.d("WatchMovieActivity", "Current position: $position, length: $length")
        if(userId.isNullOrEmpty()){
            Log.e("WatchMovieActivity","User ID is null or empty")
        }else{
            if(!appSessionViewModel.isAnonymous()){
                if(!packageVipStatus && movie?.buy == false && movie?.vip == true){
                    return
                }
                firestore.collection("users")
                    .document(userId)
                    .collection("movieProgress")
                    .document(movie?.id.toString())
                    .set(MovieWatching(movie?.id.toString(),movie?.type!!,position,length,episode,movie?.backdropPath,movie?.vip!!))
                    .addOnSuccessListener {
                        Log.d("WatchMovieActivity","Movie progress updated successfully")
                    }
                    .addOnFailureListener {
                        Log.e("WatchMovieActivity","Failed to update movie progress",it)
                    }
            }
        }
    }
}

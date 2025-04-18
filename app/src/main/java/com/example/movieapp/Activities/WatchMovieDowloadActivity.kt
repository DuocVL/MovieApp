package com.example.movieapp.Activities

import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.movieapp.AppDatabase
import com.example.movieapp.R
import com.example.movieapp.databinding.ActivityWatchMovieDowloadBinding
import kotlinx.coroutines.launch
import java.io.File

class WatchMovieDowloadActivity : AppCompatActivity(){


    private lateinit var binding: ActivityWatchMovieDowloadBinding

    private lateinit var playerView: PlayerView
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var controlsLayout: ConstraintLayout
    private lateinit var fullscreenButton: ImageButton
    private lateinit var downloadButton : ImageButton

    private lateinit var id : String
    private lateinit var videoPath: String
    private lateinit var title: String
    private lateinit var type: String
    private var episode: Int = 0

    private var isFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWatchMovieDowloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy thông tin video từ Intent
        id = intent.getStringExtra("id") ?: ""
        videoPath = intent.getStringExtra("videoPath") ?: ""
        title = intent.getStringExtra("title") ?: ""
        episode = intent.getIntExtra("episode", 0)
        type = intent.getStringExtra("type") ?: ""

        //Hien thi thong tin video
        binding.movieTitle.text = title
        binding.episode.text = if(type == "movie") "Phim" else "Tập $episode"

        playerView = binding.playerView
        controlsLayout = binding.playerView.findViewById(R.id.watchMovieControls)
        fullscreenButton = controlsLayout.findViewById(R.id.btnFullscreen)
        downloadButton = binding.dowloadButton

        //khởi tạo và Gán ExoPlayer cho PlayerView
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer
        setUpVideoPlayer(videoPath)

        fullscreenButton.setOnClickListener {
            toggleFullscreen()
        }

        downloadButton.setOnClickListener {
            deleteMovie()
            finish()
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

    private fun setUpVideoPlayer(videoPath: String?) {
        videoPath?.let {
            //Tao mediaItem tu path video
            val mediaItem = MediaItem.fromUri(Uri.fromFile(File(videoPath)))

            //Cai nguon phat video
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        } ?: run {
            // Xử lý trường hợp videoPath là null hoặc không tồn tại
            Log.e("WatchMovieActivity", "Video path is null or does not exist")
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

    private fun deleteMovie(){
        lifecycleScope.launch {
            val downloadedVideo = AppDatabase.getDatabase(this@WatchMovieDowloadActivity).downloadedVideoDao().getById(id,episode)
            if(downloadedVideo != null){
                AppDatabase.getDatabase(this@WatchMovieDowloadActivity).downloadedVideoDao().deleteById(id,episode)
                val file = File(videoPath)
                if(file.exists()){
                    file.delete()
                    Toast.makeText(this@WatchMovieDowloadActivity, "Xóa thành công!", Toast.LENGTH_SHORT).show()
                }else{
                    Log.e("WatchMovieActivity", "File not found: $videoPath")
                }
            }

        }
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
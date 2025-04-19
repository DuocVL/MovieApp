package com.example.movieapp.Fragment

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieapp.Activities.WatchMovieDowloadActivity
import com.example.movieapp.Adapters.DownloadedVideoAdapter
import com.example.movieapp.AppDatabase
import com.example.movieapp.databinding.FragmentDowloadBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DowloadFragment : Fragment() {
    private var _binding : FragmentDowloadBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DownloadedVideoAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDowloadBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadDataAgainIfNeeded()
    }

    fun loadDataAgainIfNeeded(){
        lifecycleScope.launch(Dispatchers.IO) {
            val videos = AppDatabase.getDatabase(requireContext()).downloadedVideoDao().getAll()
            withContext(Dispatchers.Main) {
                adapter = DownloadedVideoAdapter(videos){ downloadedVideo ->
                    val intent = Intent(requireContext(), WatchMovieDowloadActivity::class.java)
                    intent.putExtra("id",downloadedVideo.id)
                    intent.putExtra("videoPath",downloadedVideo.localVideoPath)
                    intent.putExtra("title",downloadedVideo.title)
                    intent.putExtra("episode",downloadedVideo.episode)
                    intent.putExtra("type",downloadedVideo.type)
                    startActivity(intent)
                }
                binding.listMovieDowload.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
                binding.listMovieDowload.adapter = adapter
            }

        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        loadDataAgainIfNeeded()
    }
}
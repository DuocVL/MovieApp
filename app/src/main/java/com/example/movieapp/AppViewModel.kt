package com.example.movieapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.movieapp.Dataclass.WatchLaterMovie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ViewModel giúp lưu danh sách ID phim đã lưu để dùng lại trong toàn bộ app
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val database = FirebaseFirestore.getInstance()
    private val _savedMovie = MutableLiveData<List<WatchLaterMovie>>()

    val savedMovie: MutableLiveData<List<WatchLaterMovie>> = _savedMovie

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> = _errorMessage

    fun fetchSavedMovies() {
        _isLoading.value = true
        _errorMessage.value = null

        database.collection("users")
            .document(userId!!)
            .collection("watchLater")
            .get()
            .addOnSuccessListener { result ->
                val movies = mutableListOf<WatchLaterMovie>()
                for (document in result) {
                    try {
                        val data = document.toObject(WatchLaterMovie::class.java)
                        movies.add(data)
                    }catch (e : Exception){
                        Log.e("AppViewModel", "Error converting document: ${document.id}", e)
                        _errorMessage.value = "Lỗi khi đọc dữ liệu từ Firestore"
                    }
                }
                _savedMovie.value = movies
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.e("AppViewModel", "Error fetching saved movies", e)
                _errorMessage.value = "Lỗi khi đọc dữ liệu từ Firestore"
                _isLoading.value = false
            }
    }

//    // Hàm để cập nhật dữ liệu chia sẻ
//    fun updateSharedData(newData: WatchLaterMovie) {
//        savedMovie.value = savedMovie.value?.apply {
//            add(newData)
//        }
//    }
//
//    // Hàm để lấy dữ liệu chia sẻ hiện tại (có thể không cần thiết nếu bạn chỉ quan sát)
//    fun getSavedMovie(): MutableList<WatchLaterMovie>? {
//        return savedMovie.value
//    }

}
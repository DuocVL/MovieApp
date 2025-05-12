package com.example.movieapp.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.movieapp.Activities.LoginActivity
import com.example.movieapp.AppSessionViewModel
import com.example.movieapp.Dataclass.Rating
import com.example.movieapp.R
import com.example.movieapp.SessionManager
import com.example.movieapp.databinding.FragmentRatingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RatingFragment : DialogFragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseAuth
    private lateinit var userId: String

    private lateinit var _binding : FragmentRatingBinding
    private val binding get() = _binding

    private var list: List<Rating>? = null
    private var totalVotes: Int = 0
    private var totalScore : Float = 0f

    private var movieId : String? = null
    private var rating : Int = 0
    private var ratingStatus = false

    private lateinit var appSessionViewModel: AppSessionViewModel
    private lateinit var sessionManager: SessionManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRatingBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        movieId = arguments?.getString("movieId")
        Log.e("RatingFragment", "movieId: $movieId")

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = FirebaseAuth.getInstance()
        userId = currentUser.currentUser?.uid ?: ""
        appSessionViewModel = AppSessionViewModel(requireActivity().application)
        sessionManager = SessionManager(requireContext())

        getRatingFirestore(callback = { list: List<Rating>?,totalVotes: Int,totalScore : Float ->
            this.list = list
            this.totalVotes = totalVotes
            this.totalScore = totalScore
            getRatingUser(callback = { rating: Int ->
                this.rating = rating
                binding.ratingBarUser.rating = rating.toFloat()
            })
            loadDetail()
        })
        // Xử lý các sự kiện và logic cho fragment ở đây

        // Đặt sự kiện click cho nút đóng
        binding.closeButton.setOnClickListener {
            setFragmentResult("ratingRequest", Bundle().apply {
                putBoolean("closed",true)
            })
            dismiss()
        }

        binding.btnSubmit.setOnClickListener {
            if(appSessionViewModel.isAnonymous()){
                showDiaLog()
            }else{
                val ratingNew = binding.ratingBarUser.rating.toInt()
                addRatingFirestore(ratingNew)
            }
        }
    }

    private fun showDiaLog(){
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Thông báo")
            .setMessage("Bạn cần đăng nhập để mua gói ?")
            .setIcon(R.drawable.ic_help)
            .setPositiveButton("Đăng nhập") { _, _ ->
                sessionManager.clearSession()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
            .setNegativeButton("Hủy"){ _, _ ->
                // Không làm gì khi người dùng chọn hủy
            }
        dialog.show()
    }

    private fun loadDetail(){
        if (list == null){
            list = listOf(Rating(1,0),Rating(2,0),Rating(3,0),Rating(4,0),Rating(5,0))
        }
        val average = if(totalScore > 0) (totalScore/totalVotes) else 0f
        binding.ratingScore.text = if(totalScore > 0) String.format("%.1f", average*2) else "Chưa có đánh giá"
        binding.totalReviews.text = "$totalVotes người đã đánh giá"
        binding.ratingBarStatic.rating = average
        for(star in 5 downTo 1){
            val percent = if(totalVotes == 0) 0 else (list!![star-1].total*100)/totalVotes // Tính phần trăm đánh giá
            when(star){
                5 -> binding.progressBar5.progress = percent
                4 -> binding.progressBar4.progress = percent
                3 -> binding.progressBar3.progress = percent
                2 -> binding.progressBar2.progress = percent
                1 -> binding.progressBar1.progress = percent
                else -> Log.e("RatingFragment","Star $star not found")
            }
        }
    }

    //Hàm thêm đánh giá vào Firestore
    private fun addRatingFirestore(ratingNew: Int){
        if (movieId != null){
            val ratingRef = firestore.collection("movie")
                .document("${movieId}")
                .collection("rating")
                .document("total")

            firestore.runTransaction { transition ->
                val snapshot = transition.get(ratingRef)
                val currentCount = snapshot.getLong(ratingNew.toString())?.toInt() ?: 0
                if(ratingStatus){
                    val currentCountOld = snapshot.getLong("${rating}")?.toInt() ?: 0
                    transition.update(ratingRef,rating.toString(),currentCountOld-1)
                    list!![rating-1].total -= 1
                    totalVotes -= 1
                    totalScore -= rating
                    Log.e("RatingFragment","totalVotes: $totalVotes, totalScore: $totalScore")
                }
                transition.set(ratingRef,hashMapOf("$ratingNew" to currentCount+1))
                list!![ratingNew-1].total += 1
                totalVotes += 1
                totalScore += ratingNew
                Log.e("RatingFragment","totalVotes: $totalVotes, totalScore: $totalScore")
            }.addOnSuccessListener {
                ratingStatus = true
                rating = ratingNew
                updateRatingUser(callback = {
                    binding.ratingBarUser.rating = rating.toFloat()
                })
                Toast.makeText(this@RatingFragment.context,"Đánh giá thành công",Toast.LENGTH_SHORT).show()
                loadDetail()

            }.addOnFailureListener {
                Log.e("RatingFragment","Error adding rating",it)
                Toast.makeText(this@RatingFragment.context,"Error adding rating",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getRatingUser(callback: (Int) -> Unit){
        if(movieId != null){
            firestore.collection("users")
                .document("${userId}")
                .collection("ratings")
                .document("${movieId}")
                .get()
                .addOnSuccessListener { document ->
                    if(document.exists() || document.data != null){
                        ratingStatus = true
                        rating = document.getLong("rating")?.toInt() ?: 0
                    }else{
                        ratingStatus = false
                        rating = 0
                    }
                    callback(rating)
                }
                .addOnFailureListener {
                    Log.e("RatingFragment","Error getting rating",it)
                    Toast.makeText(this@RatingFragment.context,"Error getting rating",Toast.LENGTH_SHORT).show()
                }
        }else{
            Log.e("RatingFragment","movieId is null")
            Toast.makeText(this@RatingFragment.context,"movieId is null",Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateRatingUser(callback: (Int) -> Unit){
        if(movieId != null){
            firestore.collection("users")
                .document("${userId}")
                .collection("ratings")
                .document("${movieId}")
                .set(hashMapOf("rating" to rating))
                .addOnSuccessListener {
                    Toast.makeText(this@RatingFragment.context,"Đánh giá thành công",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.e("RatingFragment","Error updating rating",it)
                }
        }else{
            Log.e("RatingFragment","movieId is null")
            Toast.makeText(this@RatingFragment.context,"movieId is null",Toast.LENGTH_SHORT).show()
        }
    }


    private fun getRatingFirestore(callback: (List<Rating>?,Int,Float) -> Unit){
        if(movieId != null){
            firestore.collection("movie")
                .document("${movieId}")
                .collection("rating")
                .document("total")
                .get()
                .addOnSuccessListener { document ->
                    val data: Map<String, Any>? = document.data
                    Log.e("RatingFragment", "DocumentSnapshot data: ${data}")
                    val starCounts = ArrayList<Rating>(6)
                    starCounts.add(Rating(0, 0)) // Thêm một phần tử dummy ở index 0
                    var totalVotes = 0
                    var totalScore = 0f
                    for(star in 1..5){
                        val count = document.getLong("$star")?.toInt() ?: 0
                        Log.e("RatingFragment", "Star $star: $count")
                        starCounts.add(Rating(star,count))
                        totalVotes += count
                        totalScore += star * count
                        if(star == 5){
                            callback(starCounts.subList(1,6),totalVotes,totalScore)
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("RatingFragment", "Error getting documents: ", it)
                    Toast.makeText(this@RatingFragment.context,"Error getting documents: ",Toast.LENGTH_SHORT).show()
                    callback(null,0,0f)
                }
        }else{
            Log.e("RatingFragment", "movieId is null")
            Toast.makeText(this@RatingFragment.context,"movieId is null",Toast.LENGTH_SHORT).show()
        }
    }

    override fun getTheme(): Int {
        return R.style.CustomDialogTheme
    }


}
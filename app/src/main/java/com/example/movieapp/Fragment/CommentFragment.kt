package com.example.movieapp.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieapp.Activities.LoginActivity
import com.example.movieapp.Adapters.CommentAdapter
import com.example.movieapp.AppSessionViewModel
import com.example.movieapp.Dataclass.Comment
import com.example.movieapp.Dataclass.Reply
import com.example.movieapp.R
import com.example.movieapp.SessionManager
import com.example.movieapp.databinding.FragmentCommentBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot

class CommentFragment : Fragment() {

    private var _binding :FragmentCommentBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = auth.currentUser

    private var movieId : String? = null
    private var listComment : MutableList<Comment> = mutableListOf()
    private var adapter : CommentAdapter? = null


    private var statusReply : Boolean = false
    private var replyCommentId : String? = null
    private var replyUserName : String? = null
    private lateinit var appSessionViewModel: AppSessionViewModel
    private lateinit var sessionManager: SessionManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        movieId = arguments?.getString("movieId")
        Log.e("CommentFragment", "movieId: $movieId")

        appSessionViewModel = AppSessionViewModel(requireActivity().application)
        sessionManager = SessionManager(requireContext())

        getDetailFirebase(movieId!!){ list,userLike ->
            listComment = list
            Log.e("CommentFragment", "userLike: $userLike")
            Log.e("CommentFragment", "listComment: $listComment")
            binding.commentTitle.setText("${listComment.size} bình luận")
            adapter = CommentAdapter(movieId!!,listComment.toMutableList(),userLike.toMutableList()){ commentId,username ->
                if(appSessionViewModel.isAnonymous()){
                    showDiaLog()
                    return@CommentAdapter
                }
                statusReply = true
                replyCommentId = commentId
                replyUserName = username
                Log.e("CommentFragment", "replyCommentId: $replyCommentId")
            }
            binding.listComment.adapter = adapter
            binding.listComment.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
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


    private fun getDetailFirebase(movieId : String, callback: (MutableList<Comment>,MutableList<String>) -> Unit){
        firestore.collection("movie")
            .document("$movieId")
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val listComment = mutableListOf<Comment>()
                val userLike = mutableListOf<String>()
                if(documents.isEmpty){
                    callback(listComment,userLike)
                    return@addOnSuccessListener
                }
                for (document in documents){
                    val commentId = document.id
                    val userId = document.getString("userId") ?: ""
                    val userName = document.getString("userName") ?: ""
                    val content = document.getString("content") ?: ""
                    val timestamp = document.getTimestamp("timestamp")
                    val like = document.getLong("like")?.toInt() ?: 0
                    val comment = Comment(commentId,userId!!,userName!!,movieId!!,content!!,timestamp!!,like)
                    listComment.add(comment)
                    checkLikeComment(document){
                        if(it){
                            userLike.add(commentId)
                        }
                        if(listComment.size == documents.size()){
                            callback(listComment,userLike)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CommentFragment", "Error getting documents: ", exception)
                callback(mutableListOf(), mutableListOf())
            }
    }

    private fun checkLikeComment(document : QueryDocumentSnapshot,callback: (Boolean) -> Unit){
        document.reference
            .collection("userLike")
            .document("${currentUser?.uid}")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if(documentSnapshot != null && documentSnapshot.exists()){
                    val statusLike = documentSnapshot.getBoolean("like")
                    if(statusLike == true){
                        callback(true)
                    }else{
                        callback(false)
                    }
                }else{
                    callback(false)
                }
            }
            .addOnFailureListener {
                Log.e("CommentFragment", "Error getting documents: ", it)
                callback(false)
            }
        return
    }


    fun addComment(content: String){
        if(movieId == null){
            Log.e("CommentFragment", "movieId is null")
        }else{
            addCommentMovie(movieId!!,currentUser?.uid.toString(),currentUser?.email.toString(),content){ commentId, timestamp ->
                if(commentId == null || timestamp == null){
                    Log.e("CommentFragment", "commentId or timestamp is null")
                    return@addCommentMovie
                }
                addCommentUser(commentId!!,movieId!!,currentUser?.uid.toString(),currentUser?.email.toString(),content,timestamp!!)
            }
        }
    }

    //Cap nhat danh sach binh luan
    private fun addCommentList(comment: Comment){
        Log.e("CommentFragment", "addCommentList called before add: ${listComment.size}")
        listComment.add(comment)
        binding.commentTitle.setText("${listComment.size} bình luận")
        Log.e("CommentFragment", "addCommentList called after add: ${listComment.size}")
        adapter?.addComment(comment)
    }

    private fun addCommentMovie(movieId: String, userId: String, userName: String, content: String, callback: (String?, Timestamp?) -> Unit)  {
        Log.d("CommentFragment", "addCommentMovie called")

        val movieRef = firestore.collection("movie").document(movieId)
        val commentsCollectionRef = movieRef.collection("comments")
        val timestamp = FieldValue.serverTimestamp()
        val comment = hashMapOf(
            "userId" to userId,
            "userName" to userName,
            "content" to content,
            "timestamp" to timestamp
        )
        if(statusReply && replyCommentId != null){
            commentsCollectionRef.document("$replyCommentId")
                .collection("replies")
                .add(comment)
                .addOnSuccessListener { replyRef ->
                    val replyId = replyRef.id
                    replyRef.get()
                        .addOnSuccessListener { snapshot ->
                            val actualTimestamp = snapshot.getTimestamp("timestamp")
                            callback(replyId, actualTimestamp)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error getting comment after adding", e)
                            callback(null, null) // Hoặc xử lý lỗi khác
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error adding comment to movie $movieId", e)
                    // Xử lý lỗi thêm comment
                }
            return
        }

        Log.d("CommentFragment", "Comment data: $comment")
        movieRef.get()
            .addOnSuccessListener { movieSnapshot ->
                if (!movieSnapshot.exists()) {
                    // Document movieId tồn tại, tiến hành thêm comment
                    movieRef.set(hashMapOf("exists" to true)) // Thêm một trường đơn giản để đánh dấu sự tồn tại
                        .addOnSuccessListener {
                            Log.d("Firestore", "Movie $movieId created. Now adding comment.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error creating movie $movieId", e)
                            // Xử lý lỗi tạo movie
                        }
                }
                commentsCollectionRef.add(comment)
                    .addOnSuccessListener { commentRef ->
                        val commentId = commentRef.id
                        commentRef.get()
                            .addOnSuccessListener { snapshot ->
                                val actualTimestamp = snapshot.getTimestamp("timestamp")
                                callback(commentId, actualTimestamp)
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error getting comment after adding", e)
                                callback(null, null) // Hoặc xử lý lỗi khác
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error adding comment to movie $movieId", e)
                        // Xử lý lỗi thêm comment
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error checking movie existence", e)
                // Xử lý lỗi khi kiểm tra sự tồn tại của movie
            }
    }

    private fun addCommentUser(commentId: String,movieId: String,userId: String,userName: String,content: String,timestamp: Timestamp){
        Log.d("CommentFragment", "addCommentUser called")
        val comment : HashMap<String,Any>
        if(statusReply && replyCommentId != null){
            comment = hashMapOf(
                "commentId" to commentId,
                "userId" to userId,
                "commentIdReply" to replyCommentId!!,
                "userName" to userName,
                "movieId" to movieId,
                "content" to content,
                "timestamp" to timestamp
            )
        }else{
            comment = hashMapOf(
                "commentId" to commentId,
                "userId" to userId,
                "userName" to userName,
                "movieId" to movieId,
                "content" to content,
                "timestamp" to timestamp
            )
        }
        val userRef = firestore.collection("users").document("${userId}")
        val commentRef = userRef.collection("comments").document("${commentId}")
        userRef.get()
            .addOnSuccessListener { userSnapshot ->
                if (!userSnapshot.exists()) {
                    // Document movieId tồn tại, tiến hành thêm comment
                    userRef.set(hashMapOf("exists" to true)) // Thêm một trường đơn giản để đánh dấu sự tồn tại
                }
                commentRef.set(comment)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Comment added to user ${commentId}")
                        if(statusReply){
                            addReplyComment(Reply(commentId,userId,replyUserName!!,currentUser?.email.toString(),movieId,content,timestamp))
                        }else{
                            addCommentList(Comment(commentId!!,currentUser?.uid.toString(),currentUser?.email.toString(),movieId!!,content,timestamp!!,0))
                        }
                        statusReply = false
                        replyCommentId = null
                        replyUserName = null
                    }
                    .addOnFailureListener { e->
                        Log.e("Firestore", "Error adding comment to user ${userId}", e)
                        // Xử lý lỗi thêm comment
                    }
            }
    }

    private fun addReplyComment(reply: Reply){
        Log.e("CommentFragment", "addReplyComment called")
        adapter?.addReply(replyCommentId!!,reply)
    }
}
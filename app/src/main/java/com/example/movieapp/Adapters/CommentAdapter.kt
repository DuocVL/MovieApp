package com.example.movieapp.Adapters

import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.Dataclass.Comment
import com.example.movieapp.Dataclass.Reply
import com.example.movieapp.R
import com.example.movieapp.databinding.ItemCommentBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentAdapter(
    private val movieId: String,
    private var list: MutableList<Comment>,
    private var likeList : MutableList<String>,
    private val onReplyClick: (String, String) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val repliesMap = mutableMapOf<String, MutableList<Reply>>()
    private val firebaseFirestore : FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    inner class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        val replyAdapter = ReplyAdapter(mutableListOf()) // Mỗi ViewHolder có adapter riêng cho reply
        init {
            binding.listReply.adapter = replyAdapter
            binding.listReply.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    //Hàm chạy khi có sự thay đổi dữ liệu hoặc khi khởi tạo adapter
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = list[position]
        holder.binding.username.text = comment.username
        holder.binding.commentText.text = comment.content
        holder.binding.time.text = formatTimeDifferenceAndroid(comment.timestamp, holder.itemView.context)
        holder.binding.likeCount.setText("${comment.like}")
        holder.binding.totalReply.setText("${repliesMap[comment.commentId]?.size ?: 0} bình luận") // Cập nhật số lượng reply
        if(likeList.contains(comment.commentId)){
            holder.binding.btnLike.setImageResource(R.drawable.favorite_red)
        }else{
            holder.binding.btnLike.setImageResource(R.drawable.favorite)
        }
        // Lấy danh sách reply cho comment hiện tại hoặc tạo mới nếu chưa có
        getRepliesForComment(comment.commentId){ listReply ->
            val currentReplies = repliesMap.getOrPut(comment.commentId) { listReply.toMutableList() }
            if (currentReplies.isEmpty()) {
                holder.binding.showReply.visibility = ViewGroup.GONE
            }else{
                holder.binding.showReply.visibility = ViewGroup.VISIBLE
                holder.binding.totalReply.setText("${currentReplies.size} bình luận")
            }
            holder.replyAdapter.updateList(currentReplies) // Cập nhật danh sách trong ReplyAdapter

            Log.e("CommentAdapter", "replyList size for ${comment.commentId}: ${currentReplies.size}")
        }


        holder.binding.showReply.setOnClickListener {
            Log.e("CommentAdapter", "showReply clicked for ${comment.commentId}")
            holder.binding.listReply.visibility = if (holder.binding.listReply.visibility == ViewGroup.VISIBLE) {
                ViewGroup.GONE
            } else {
                ViewGroup.VISIBLE
            }
        }

        holder.binding.replyButton.setOnClickListener {
            Log.e("CommentAdapter", "replyButton clicked for ${comment.commentId}")
            onReplyClick(comment.commentId ?: "", comment.username ?: "")
        }
        holder.binding.btnLike.setOnClickListener {
            addLike(position){ status ->
                if(status){
                    addLikeUser(position){ it ->
                        if(it){
                            if(likeList.contains(list[position].commentId)){
                                val like = list[position].like - 1
                                list[position] = list[position].copy(like = like)
                                likeList.remove(list[position].commentId)
                                holder.binding.likeCount.setText("${like}")
                                holder.binding.btnLike.setImageResource(R.drawable.favorite)
                                Log.e("CommentAdapter", "likeList size: ${likeList.size}")
                            }else{
                                val like = list[position].like + 1
                                list[position] = list[position].copy(like = like)
                                likeList.add(list[position].commentId)
                                holder.binding.likeCount.setText("${like}")
                                holder.binding.btnLike.setImageResource(R.drawable.favorite_red)
                                Log.e("CommentAdapter", "likeList size: ${likeList.size}")
                            }
                        }else{
                            Log.e("CommentAdapter", "Error adding list like")
                        }
                    }
                }else{
                    Log.e("CommentAdapter", "Error adding like")
                }
            }
        }
    }

    fun addLike(position: Int,callback: (Boolean) -> Unit){
        Log.e("CommentAdapter", "addLike called for position: $position")
        //Thêm lươợt thích vào comment
        val movieId = list[position].movieId
        val commentId = list[position].commentId
        val value = if(likeList.contains(commentId)) list[position].like - 1 else list[position].like + 1
        firebaseFirestore.collection("movie")
            .document(movieId)
            .collection("comments")
            .document("$commentId")
            .update("like",value)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
        return
    }

    //Them vao  danh sach like cua comment
    fun addLikeUser(position: Int,callback: (Boolean) -> Unit){
        Log.e("CommentAdapter", "addLikeUser called for position: $position")
        val movieId = list[position].movieId
        val commentId = list[position].commentId
        val value = if(likeList.contains(commentId)) false else true
        firebaseFirestore.collection("movie")
            .document(movieId)
            .collection("comments")
            .document("$commentId")
            .collection("userLike")
            .document(currentUser?.uid ?: "")
            .set(mapOf("like" to value))
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                Log.e("CommentAdapter", "Error adding like: ${it.message}")
                callback(false)
            }
        return
    }


    // Sử dụng thư viện DateUtils của Android (nếu bạn đang trong môi trường Android)
    fun formatTimeDifferenceAndroid(timestamp: Timestamp, context: android.content.Context): CharSequence {
        return DateUtils.getRelativeTimeSpanString(
            timestamp.toDate().time,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        )
    }

    fun addComment(comment: Comment) {
        Log.e("CommentAdapter", "addComment called")
        list.add(comment)
        notifyItemInserted(list.size - 1)
    }

    fun addReply(commentId: String, reply: Reply) {
        Log.e("CommentAdapter", "addReply called for commentId: $commentId")
        val repliesForComment = repliesMap.getOrPut(commentId) { mutableListOf() }
        Log.e("CommentAdapter", "repliesForComment size: ${repliesForComment.size}")
        repliesForComment.add(reply)
        // Tìm vị trí của comment cha và chỉ cập nhật RecyclerView của comment đó
        val commentIndex = list.indexOfFirst { it.commentId == commentId }
        if (commentIndex != -1) {
            notifyItemChanged(commentIndex) // Cập nhật lại item comment để hiển thị reply mới
        }
    }

    fun getRepliesForComment(commentId: String, callback: (List<Reply>) -> Unit) {
        firebaseFirestore.collection("movie")
            .document(movieId)
            .collection("comments")
            .document(commentId)
            .collection("replies")
            .get()
            .addOnSuccessListener { documents ->
                val replies = mutableListOf<Reply>()
                for (document in documents) {
                    val replyId = document.id
                    val userId = document.getString("userId") ?: ""
                    val usernameReply = document.getString("usernameReply") ?: ""
                    val username = document.getString("username") ?: ""
                    val content = document.getString("content") ?: ""
                    val timestamp = document.getTimestamp("timestamp") ?: Timestamp.now()
                    val reply = Reply(replyId, userId, usernameReply, username, movieId, content, timestamp)
                    replies.add(reply)
                    if (replies.size == documents.size()) {
                        callback(replies)
                    }
                }
            }
            .addOnFailureListener {
                callback(emptyList())
                Log.e("CommentAdapter", "Error getting replies for comment $commentId", it)
            }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
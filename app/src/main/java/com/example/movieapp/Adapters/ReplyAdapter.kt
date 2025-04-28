package com.example.movieapp.Adapters

import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.movieapp.Dataclass.Reply
import com.example.movieapp.databinding.ItemReplyBinding
import com.google.firebase.Timestamp

class ReplyAdapter(private val replyList: MutableList<Reply>) : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {

    inner class ReplyViewHolder(val binding: ItemReplyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val binding = ItemReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReplyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        Log.d("ReplyAdapter", "Binding reply at position $position: ${replyList[position]}")
        holder.binding.username.text = replyList[position].usernameReply // Sử dụng usernameReply
        holder.binding.commentText.setText(replyList[position].content)
        holder.binding.time.setText(formatTimeDifferenceAndroid(replyList[position].timestamp, holder.itemView.context))
        holder.binding.likeCount.setText("0")
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

    fun updateList(newReplyList: MutableList<Reply>) {
        replyList.clear()
        replyList.addAll(newReplyList)
        notifyDataSetChanged()
        Log.d("ReplyAdapter", "Updated reply list size: ${replyList.size}")
    }

    fun addReply(reply: Reply) {
        replyList.add(reply)
        notifyItemInserted(replyList.size - 1)
        Log.d("ReplyAdapter", "Added reply at position ${replyList.size - 1}")
    }

    override fun getItemCount(): Int {
        return replyList.size
    }
}
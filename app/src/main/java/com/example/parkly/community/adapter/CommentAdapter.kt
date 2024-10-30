package com.example.parkly.community.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkly.R
import com.example.parkly.data.PostComments
import com.example.parkly.databinding.ItemPostCommentBinding
import com.example.parkly.util.isBlobEmpty
import com.example.parkly.util.toBitmap
import io.getstream.avatarview.coil.loadImage

class CommentAdapter(
    val fn: (ViewHolder, PostComments) -> Unit = { _, _ -> }
) : ListAdapter<PostComments, CommentAdapter.ViewHolder>(Diff) {
    inner class CommentViewHolder(val binding: ItemPostCommentBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object Diff : DiffUtil.ItemCallback<PostComments>() {
        override fun areItemsTheSame(a: PostComments, b: PostComments) =
            a.postCommentID == b.postCommentID

        override fun areContentsTheSame(a: PostComments, b: PostComments) = a == b
    }

    class ViewHolder(val binding: ItemPostCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemPostCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postComment = getItem(position)
        val time = displayPostTime(postComment.createdAt)
        if (isBlobEmpty(postComment.user.avatar)) {
            val avatarDrawableRes = R.drawable.round_account_circle_24

            val avatarDrawable: Drawable? =
                ContextCompat.getDrawable(holder.itemView.context, avatarDrawableRes)
            holder.binding.avatarView.setImageDrawable(avatarDrawable)
        } else {
            if (postComment.user.isEnterprise && postComment.user.company_id != "") {
                holder.binding.avatarView.indicatorEnabled = true
            }
            holder.binding.avatarView.loadImage(postComment.user.avatar.toBitmap())
        }

        holder.binding.txtComment.text = postComment.comment
        holder.binding.txtUsername.text = postComment.user.name
        holder.binding.txtTime.text = time

        fn(holder, postComment)
    }

    private fun displayPostTime(postTime: Long): String {
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - postTime

        val seconds = timeDifference / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            years > 0 -> "$years year${if (years > 1) "s" else ""} ago"
            months > 0 -> "$months month${if (months > 1) "s" else ""} ago"
            weeks > 0 -> "$weeks week${if (weeks > 1) "s" else ""} ago"
            days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
            else -> "Just now"
        }
    }
}
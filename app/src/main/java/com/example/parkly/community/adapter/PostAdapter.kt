package com.example.parkly.community.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.parkly.data.Post
import com.example.parkly.databinding.ItemPostBinding
import com.example.parkly.util.setImageBlob
import com.example.parkly.util.toBitmap
import io.getstream.avatarview.coil.loadImage

class PostAdapter (
    val fn: (ViewHolder, Post) -> Unit = { _, _ -> }
    ) : ListAdapter<Post, PostAdapter.ViewHolder>(Diff) {
        inner class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)
        companion object Diff : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(a: Post, b: Post) = a.postID == b.postID
            override fun areContentsTheSame(a: Post, b: Post) = a == b
        }

        class ViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val post = getItem(position)
            val time = displayPostTime(post.createdAt)

            holder.binding.avatarView.loadImage(post.user.avatar.toBitmap())
            if (post.user.isEnterprise && post.user.company_id != "") {
                holder.binding.avatarView.indicatorEnabled = true
            }
            holder.binding.postImageView.setImageBlob(post.image)
            holder.binding.txtDescription.text = post.description
            holder.binding.txtUsername.text = post.user.name
            holder.binding.txtTime.text = time
            holder.binding.txtLikes.text = post.likes.toString()
            holder.binding.txtComments.text = post.comments.toString() + " Comments"

            fn(holder, post)
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
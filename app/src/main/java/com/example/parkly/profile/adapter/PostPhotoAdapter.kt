package com.example.parkly.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkly.data.Post
import com.example.parkly.databinding.ItemPostPhotoBinding
import com.example.parkly.util.setImageBlob

class PostPhotoAdapter (
    val fn: (ViewHolder, Post) -> Unit = { _, _ -> }
) : ListAdapter<Post, PostPhotoAdapter.ViewHolder>(Diff) {

    companion object Diff : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(a: Post, b: Post) = a.postID == b.postID
        override fun areContentsTheSame(a: Post, b: Post) = a == b
    }

    class ViewHolder(val binding: ItemPostPhotoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemPostPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = getItem(position)

        holder.binding.imageView.setImageBlob(post.image)
        fn(holder, post)
    }


}

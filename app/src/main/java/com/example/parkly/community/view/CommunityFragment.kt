package com.example.parkly.community.view

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.community.adapter.PostAdapter
import com.example.parkly.community.viewmodel.PostLikesViewModel
import com.example.parkly.community.viewmodel.PostViewModel
import com.example.parkly.data.Post
import com.example.parkly.data.PostLikes
import com.example.parkly.data.User
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentCommunityBinding

class CommunityFragment : Fragment() {

    companion object {
        fun newInstance() = CommunityFragment()
    }

    private lateinit var adapter: PostAdapter
    private val postVM: PostViewModel by activityViewModels()
    private val postLikesVM: PostLikesViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentCommunityBinding
    private var isSearching = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)


        binding.btnAddPost.setOnClickListener {
            findNavController().navigate(
                R.id.action_communityFragment_to_addPostFragment, bundleOf(
                    "postID" to ""
                )
            )
        }

        binding.btnSearch.setOnClickListener {
            if (binding.edtSearch.text.toString().trim() != "") {
                postVM.search(binding.edtSearch.text.toString().trim())
                postVM.getResultLD().observe(viewLifecycleOwner) { postList ->
                    userVM.getUserLLD().observe(viewLifecycleOwner) { user ->
                        val filteredPostList =
                            postList.filter { it.deletedAt == 0L } // Filter out posts with deletedAt != 0
                        filteredPostList.forEach { post ->
                            post.user = userVM.get(post.userID) ?: User()
                        }
                        val sortedPostList = filteredPostList.sortedByDescending { it.createdAt }

                        adapter.submitList(sortedPostList)

                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed({
                            adapter.notifyDataSetChanged()
                        }, 100)
                    }
                }
            } else {
                postVM.getPostLD().observe(viewLifecycleOwner) { postList ->
                    userVM.getUserLLD().observe(viewLifecycleOwner) { user ->
                        val filteredPostList =
                            postList.filter { it.deletedAt == 0L } // Filter out posts with deletedAt != 0
                        filteredPostList.forEach { post ->
                            post.user = userVM.get(post.userID) ?: User()
                        }
                        val sortedPostList = filteredPostList.sortedByDescending { it.createdAt }

                        adapter.submitList(sortedPostList)

                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed({
                            adapter.notifyDataSetChanged()
                        }, 100)
                    }
                }


            }
        }

        adapter = PostAdapter { holder, post ->
            holder.binding.avatarView.setOnClickListener {
                if (post.userID == userVM.getAuth().uid) {
                    return@setOnClickListener
                }
                findNavController().navigate(
                    R.id.action_communityFragment_to_userProfileFragment, bundleOf(
                        "userID" to post.userID
                    )
                )
            }
            holder.binding.btnLike.setOnClickListener {
                toggleLike(post)
            }
            holder.binding.txtComments.setOnClickListener {
                findNavController().navigate(
                    R.id.action_communityFragment_to_postCommentFragment, bundleOf(
                        "postID" to post.postID
                    )
                )
            }
            holder.binding.btnComment.setOnClickListener {
                findNavController().navigate(
                    R.id.action_communityFragment_to_postCommentFragment, bundleOf(
                        "postID" to post.postID
                    )
                )
            }
            holder.binding.btnMoreOptions.visibility = View.INVISIBLE
            updateThumbUpDrawable(holder, post)
        }

        binding.postResult.adapter = adapter


        postVM.getPostLD().observe(viewLifecycleOwner) { postList ->
            userVM.getUserLLD().observe(viewLifecycleOwner) { user ->
                val filteredPostList =
                    postList.filter { it.deletedAt == 0L } // Filter out posts with deletedAt != 0
                filteredPostList.forEach { post ->
                    post.user = userVM.get(post.userID) ?: User()
                }
                val sortedPostList = filteredPostList.sortedByDescending { it.createdAt }

                adapter.submitList(sortedPostList)

                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    adapter.notifyDataSetChanged()
                }, 100)
            }
        }


        // Refresh
        binding.refresh.setOnRefreshListener {
            postVM.getPostLD().observe(viewLifecycleOwner) { postList ->
                userVM.getUserLLD().observe(viewLifecycleOwner) { user ->
                    val filteredPostList =
                        postList.filter { it.deletedAt == 0L } // Filter out posts with deletedAt != 0
                    filteredPostList.forEach { post ->
                        post.user = userVM.get(post.userID) ?: User()
                    }
                    val sortedPostList = filteredPostList.sortedByDescending { it.createdAt }

                    adapter.submitList(sortedPostList)

                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        adapter.notifyDataSetChanged()
                    }, 100)

                }

                adapter.notifyDataSetChanged()
                binding.refresh.isRefreshing = false
            }
        }




        return binding.root
    }

    private fun toggleLike(post: Post) {
        val userId = userVM.getUserLD().value!!.uid
        if (postLikesVM.get(post.postID, userId) != null) {
            postLikesVM.delete(post.postID, userId)
            removeLikesTotaltoPost(post.postID)
            adapter.notifyDataSetChanged()
        } else {
            postLikesVM.set(addPostlikes(post.postID))
            addLikesTotaltoPost(post.postID)
            adapter.notifyDataSetChanged()
        }
    }

    private fun addPostlikes(postID: String): PostLikes {
        return PostLikes(
            postLikesID = "",
            postID = postID,
            userID = userVM.getUserLD().value!!.uid
        )
    }

    private fun addLikesTotaltoPost(postID: String) {
        val tempPost = postVM.get(postID)
        val f = tempPost?.let {
            Post(
                postID = it.postID,
                description = it.description,
                image = it.image,
                createdAt = it.createdAt,
                userID = it.userID,
                comments = it.comments,
                likes = it.likes + 1
            )
        }

        if (f != null) {
            postVM.update(f)
        }
    }

    private fun removeLikesTotaltoPost(postID: String) {
        val tempPost = postVM.get(postID)
        val f = tempPost?.let {
            Post(
                postID = it.postID,
                description = it.description,
                image = it.image,
                createdAt = it.createdAt,
                userID = it.userID,
                comments = it.comments,
                likes = it.likes - 1
            )
        }

        if (f != null) {
            postVM.update(f)
        }
    }

    private fun updateThumbUpDrawable(holder: PostAdapter.ViewHolder, post: Post) {
        val userId = userVM.getUserLD().value!!.uid
        val isLiked = postLikesVM.get(post.postID, userId) != null
        val thumbUpDrawableRes = if (isLiked) {
            R.drawable.baseline_thumb_up_24_blue
        } else {
            // Use the default thumb up drawable here
            R.drawable.baseline_thumb_up_off_alt_24_blue
        }
        val thumbUpDrawable: Drawable? =
            ContextCompat.getDrawable(holder.itemView.context, thumbUpDrawableRes)
        holder.binding.imageThumbUp.setImageDrawable(thumbUpDrawable)
    }


}
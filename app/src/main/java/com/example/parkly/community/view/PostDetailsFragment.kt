package com.example.parkly.community.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.community.adapter.CommentAdapter
import com.example.parkly.community.viewmodel.PostCommentViewModel
import com.example.parkly.community.viewmodel.PostLikesViewModel
import com.example.parkly.community.viewmodel.PostViewModel
import com.example.parkly.data.Post
import com.example.parkly.data.PostComments
import com.example.parkly.data.PostLikes
import com.example.parkly.data.User
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentPostDetailsBinding
import com.example.parkly.util.dialog
import com.example.parkly.util.displayPostTime
import com.example.parkly.util.setImageBlob
import com.example.parkly.util.snackbar
import com.example.parkly.util.toBitmap
import io.getstream.avatarview.coil.loadImage
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class PostDetailsFragment : Fragment() {

    private lateinit var adapterComment: CommentAdapter
    private val postVM: PostViewModel by activityViewModels()
    private val postLikesVM: PostLikesViewModel by activityViewModels()
    private val postCommentsVM: PostCommentViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentPostDetailsBinding
    private val postID by lazy { requireArguments().getString("postID", "") }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostDetailsBinding.inflate(inflater, container, false)

        binding.topAppBar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSend.setOnClickListener {
            togglePost(postID)
        }

        adapterComment = CommentAdapter { holder, post ->
            holder.binding.avatarView.setOnClickListener {
                findNavController().navigate(
                    R.id.action_postDetailsFragment_to_userProfileFragment, bundleOf(
                        "userID" to post.userID
                    )
                )

            }

        }

        postVM.getPostLD().observe(viewLifecycleOwner) {
            val myPost = postVM.get(postID) ?: return@observe
            var targetUser = userVM.get(myPost.userID)
            if (targetUser != null) {
                binding.avatarView.loadImage(targetUser.avatar.toBitmap())
                binding.txtUsername.text = targetUser.name
            }
            binding.postImageView.setImageBlob(myPost.image)
            binding.txtDescription.text = myPost.description

            binding.txtTime.text = displayPostTime(myPost.createdAt)
            binding.txtLikes.text = myPost.likes.toString()
            binding.txtComments.text = myPost.comments.toString() + " Comments"

            binding.btnLike.setOnClickListener {
                toggleLike(myPost)
            }

            if (myPost.userID != userVM.getUserLD().value!!.uid) {
                binding.btnMoreOptions.visibility = View.INVISIBLE
            }

            binding.btnMoreOptions.setOnClickListener {
                showPopupMenu(binding.btnMoreOptions, myPost.postID)
            }

            updateThumbUpDrawable(myPost)


        }


        binding.commentResult.adapter = adapterComment


        postCommentsVM.getCommentsByPostID(postID).observe(viewLifecycleOwner) { commentList ->
            userVM.getUserLLD().observe(viewLifecycleOwner) { user ->
                commentList.forEach { comment ->
                    comment.user = userVM.get(comment.userID) ?: User()
                }
            }
            val sortedCommentList = commentList.sortedByDescending { it.createdAt }

            adapterComment.submitList(sortedCommentList)
        }

        // Refresh
        binding.refresh.setOnRefreshListener {

            adapterComment.notifyDataSetChanged()
            binding.refresh.isRefreshing = false
        }


        return binding.root
    }

    private fun toggleLike(post: Post) {
        val userId = userVM.getUserLD().value!!.uid
        if (postLikesVM.get(post.postID, userId) != null) {
            postLikesVM.delete(post.postID, userId)
            removeLikesTotaltoPost(post.postID)

        } else {
            postLikesVM.set(addPostlikes(post.postID))
            addLikesTotaltoPost(post.postID)
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

    private fun updateThumbUpDrawable(post: Post) {
        val userId = userVM.getUserLD().value!!.uid
        val isLiked = postLikesVM.get(post.postID, userId) != null
        val thumbUpDrawableRes = if (isLiked) {
            R.drawable.baseline_thumb_up_24_blue
        } else {
            R.drawable.baseline_thumb_up_off_alt_24_blue
        }
        val drawable = ContextCompat.getDrawable(binding.root.context, thumbUpDrawableRes)
        binding.imageThumbUp.setImageDrawable(drawable)
    }

    fun showPopupMenu(view: View, postID: String) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.post_menu)

        // Set up a listener for menu item clicks
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    // Handle edit action
                    findNavController().navigate(
                        R.id.action_postDetailsFragment_to_addPostFragment, bundleOf(
                            "postID" to postID
                        )
                    )
                    true
                }

                R.id.menu_delete -> {
                    // Handle delete action
                    val oriPost = postVM.get(postID)
                    if (oriPost != null) {
                        val updatedPost = oriPost.copy(deletedAt = DateTime.now().millis)
                        dialog("Delete Post", "Are you sure want to delete this post ?",
                            onPositiveClick = { _, _ ->
                                lifecycleScope.launch {
                                    postVM.update(updatedPost)
                                }
                                findNavController().navigateUp()
                                snackbar("Post Deleted Successfully.")
                            })
                    }
                    true
                }

                else -> false
            }
        }
        // Show the popup menu
        popupMenu.show()
    }

    private fun togglePost(postID: String) {
        postComment()
        addCommentsTotaltoPost(postID)
    }

    private fun postComment() {
        val comment = createCommentObject()

        if (!isPostValid(comment)) {
            snackbar("Please fulfill the requirement.")
            return
        }



        dialog("Comment", "Are you sure want to post this comment ?",
            onPositiveClick = { _, _ ->
                lifecycleScope.launch {
                    postCommentsVM.set(comment)

                }

                snackbar("Commented Successfully.")
                binding.edtComment.setText("");

            }
        )
    }

    private fun createCommentObject(): PostComments {
        return PostComments(
            postID = postID,
            createdAt = DateTime.now().millis,
            userID = userVM.getUserLD().value!!.uid,
            comment = binding.edtComment.text.toString().trim()
        )

    }

    private fun addCommentsTotaltoPost(postID: String) {
        val tempPost = postVM.get(postID)
        val f = tempPost?.let {
            Post(
                postID = it.postID,
                description = it.description,
                image = it.image,
                createdAt = it.createdAt,
                userID = it.userID,
                comments = it.comments + 1,
                likes = it.likes
            )
        }

        if (f != null) {
            postVM.update(f)
        }
    }

    private fun isPostValid(comments: PostComments): Boolean {

        val validation = postCommentsVM.validateInput(binding.txtCommenting, comments.comment)

        return validation
    }

}
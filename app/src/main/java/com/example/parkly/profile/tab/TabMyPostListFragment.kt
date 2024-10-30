package com.example.parkly.profile.tab

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.parkly.R
import com.example.parkly.community.viewmodel.PostViewModel
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentTabMyPostListBinding
import com.example.parkly.profile.adapter.PostPhotoAdapter

class TabMyPostListFragment(val userID: String = "") : Fragment() {

    private lateinit var adapter: PostPhotoAdapter
    private val postVM: PostViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentTabMyPostListBinding
    private val nav by lazy { findNavController() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTabMyPostListBinding.inflate(inflater, container, false)

        adapter = PostPhotoAdapter { holder, post ->
            holder.binding.imageView.setOnClickListener {
                findNavController().navigate(
                    R.id.postDetailsFragment, bundleOf(
                        "postID" to post.postID
                    )
                )
            }
        }
        binding.rv.adapter = adapter

        postVM.getPostLD().observe(viewLifecycleOwner) { postList ->
            if (postList.isEmpty()) {
                binding.noPost.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                return@observe
            }

            // if no id pass in (My Post), use own id
            val uid = if (userID == "") userVM.getAuth().uid else userID

            val sortedPostList = postList.filter { it.deletedAt == 0L && it.userID == uid }

            if (sortedPostList.isEmpty()) {
                binding.noPost.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
                return@observe
            }

            binding.noPost.visibility = View.GONE
            binding.rv.visibility = View.VISIBLE

            adapter.submitList(sortedPostList.sortedByDescending { it.createdAt })
        }

        return binding.root
    }

}
package com.example.parkly.community.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.parkly.community.viewmodel.PostViewModel
import com.example.parkly.data.Post
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.databinding.FragmentAddPostBinding
import com.example.parkly.util.cropToBlob
import com.example.parkly.util.dialog
import com.example.parkly.util.setImageBlob
import com.example.parkly.util.snackbar
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class AddPostFragment : Fragment() {

    companion object {
        fun newInstance() = AddPostFragment()
    }

    private val postVM: PostViewModel by activityViewModels()
    private val userVM: UserViewModel by activityViewModels()
    private val nav by lazy { findNavController() }
    private lateinit var binding: FragmentAddPostBinding
    private val postID by lazy { arguments?.getString("postID") ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddPostBinding.inflate(inflater, container, false)

        validateOnTextChanged()

        binding.btnUploadImage.setOnClickListener { select()}

        if(postID != ""){
            var post = postVM.get(postID)
            binding.topAppBar.title ="Edit Post"
            if (post != null) {
                binding.edtImage.setImageBlob(post.image)
                binding.edtDescription.setText(post.description)
                binding.createdAt.text = post.createdAt.toString()
                binding.btnPost.setOnClickListener { post(true,post) }
            }

        }else{
            binding.btnPost.setOnClickListener { post(false,null) }
        }

        binding.topAppBar.setOnClickListener{
            findNavController().navigateUp()
        }
        return binding.root
    }

    private fun validateOnTextChanged() {
        val textFields = listOf(
            binding.edtDescription

        )

        textFields.forEach { textField ->
            textField.doOnTextChanged { text, _, _, _ ->
                val label = when (textField) {
                    binding.edtDescription -> binding.lblPostDescription

                    else -> null
                }
                postVM.validateInput(label!!, text.toString().trim())
            }
        }


    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        binding.edtImage.setImageURI(it)
    }

    private fun select() {
        // Select file
        getContent.launch("image/*")
    }

    private fun post(isEditing: Boolean,EditPost:Post?) {
        if(!isImageValid()){
            snackbar("Please Add a picture.")
            return
        }

        val post = createPostObject(isEditing,EditPost)

        if (!isPostValid(post)) {
            snackbar("Please fulfill the requirement.")
            return
        }

        if(isEditing == true){
            dialog("Edit Post", "Are you sure want to submit this edit ?",
                onPositiveClick = { _, _ ->
                    lifecycleScope.launch {
                        postVM.update(post)
                    }

                    snackbar("Post Edited Successfully.")
                    nav.navigateUp()

                })
        }else{
            dialog("Add Post", "Are you sure want to submit this post ?",
                onPositiveClick = { _, _ ->
                    lifecycleScope.launch {
                        postVM.set(post)
                    }

                    snackbar("Post Submitted Successfully.")
                    nav.navigateUp()
                })


        }}

    private fun createPostObject(isEditing: Boolean,post: Post?): Post {
        return Post(
            postID = if (isEditing) postID else "",
            description = binding.edtDescription.text.toString().trim(),
            createdAt = if (isEditing) binding.createdAt.text.toString()
                .toLong() else DateTime.now().millis,
            image = binding.edtImage.cropToBlob(binding.edtImage.getDrawable().getIntrinsicWidth(),binding.edtImage.getDrawable().getIntrinsicHeight()),
            userID = userVM.getUserLD().value!!.uid,
            comments = if (isEditing && post?.comments !=null) post.comments else 0,
            likes =  if (isEditing && post?.likes !=null) post.likes else 0,
            updatedAt = if (isEditing) DateTime.now().millis else 0,
            deletedAt = 0
        )

    }

    private fun isPostValid(post: Post): Boolean {

        val validation = postVM.validateInput(binding.lblPostDescription, post.description)


        return validation
    }


    private fun isImageValid(): Boolean {
        if(binding.edtImage.getDrawable() == null){
            return false
        }else
            return true

    }

}
package com.example.parkly.community.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.parkly.data.PostComments
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects

class PostCommentViewModel(val app: Application) : AndroidViewModel(app){
    private val POSTCOMMENTS = Firebase.firestore.collection("post_comments")
    private val _postCommentsLD = MutableLiveData<List<PostComments>>()
    private var listener: ListenerRegistration? = null
    private val required = "* Required"

    init {
        listener = POSTCOMMENTS.addSnapshotListener { snap, _ ->
            _postCommentsLD.value = snap?.toObjects()
        }
    }

    fun init() = Unit

    fun getPostCommentsLD() = _postCommentsLD

    fun getAll() = _postCommentsLD.value ?: emptyList()

    fun get(postID: String, userID: String) = getAll().find { it.postID == postID && it.userID == userID }

    fun get(postID: String) = getAll().find { it.postID == postID }

    fun getCommentsByPostID(postID: String): MutableLiveData<List<PostComments>> {
        val filteredCommentsLiveData = MutableLiveData<List<PostComments>>()
        _postCommentsLD.observeForever { comments ->
            filteredCommentsLiveData.value = comments.filter { it.postID == postID }
        }
        return filteredCommentsLiveData
    }
    fun set(comment: PostComments) {
        POSTCOMMENTS.document().set(comment)
    }

    fun validateInput(field: TextInputLayout, fieldValue: String): Boolean {
        val isValid = !fieldValue.isNullOrEmpty()
        field.helperText = if (isValid) "" else required
        return isValid
    }
}
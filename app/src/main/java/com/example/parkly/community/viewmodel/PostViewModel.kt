package com.example.parkly.community.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.parkly.data.Post
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects

class PostViewModel(val app: Application) : AndroidViewModel(app){
    private val POSTS = Firebase.firestore.collection("post")
    private val _postLD = MutableLiveData<List<Post>>()
    private var listener: ListenerRegistration? = null
    private val required = "* Required"

    init {
        listener = POSTS.addSnapshotListener { snap, _ ->
            _postLD.value = snap?.toObjects()
            updateResult()
        }
    }

    fun init() = Unit

    fun getPostLD() = _postLD

    fun getAll() = _postLD.value ?: emptyList()

    fun get(postID: String) = getAll().find { it.postID == postID }
    fun getByUser(userID: String) = getAll().find { it.userID == userID }

    fun getPostLive(postList: List<Post>, postID: String) = postList.find { it.postID == postID }!!

    fun set(post: Post) {
        POSTS.document().set(post)
    }

    fun update(post: Post) {
        POSTS.document(post.postID).set(post)
    }

    fun getPostByUser(userID: String): List<Post> {
        var list = getAll()

        list = list.filter {
            it.userID == userID
        }

        _postLD.value = list

        return list
    }

    fun validateInput(field: TextInputLayout, fieldValue: String): Boolean {
        val isValid = !fieldValue.isNullOrEmpty()
        field.helperText = if (isValid) "" else required
        return isValid
    }

    private val resultLD = MutableLiveData<List<Post>>()
    private var search = ""

    fun getResultLD() = resultLD

    fun clearSearch() {
        search = ""
        updateResult()
    }

    fun search(search: String) {
        this.search = search
        updateResult()
    }
    fun updateResult() {
        var list = getAll()

        list = list.filter {
            it.description.contains(search, true) || it.user.name.contains(search, true)
        }

        resultLD.value = list
    }




}
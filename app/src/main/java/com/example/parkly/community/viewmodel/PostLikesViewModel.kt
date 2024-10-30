package com.example.parkly.community.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.parkly.data.PostLikes
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects

class PostLikesViewModel(val app: Application) : AndroidViewModel(app){
    private val POSTLIKES = Firebase.firestore.collection("post_likes")
    private val _postlikesLD = MutableLiveData<List<PostLikes>>()
    private var listener: ListenerRegistration? = null

    init {
        listener = POSTLIKES.addSnapshotListener { snap, _ ->
            _postlikesLD.value = snap?.toObjects()
        }
    }

    fun init() = Unit

    fun getPostLD() = _postlikesLD

    fun getAll() = _postlikesLD.value ?: emptyList()

    fun get(postID: String, userID: String) = getAll().find { it.postID == postID && it.userID == userID }

    fun set(postLikes: PostLikes) {
        POSTLIKES.document().set(postLikes)
    }

    fun delete(postID: String, userID: String) {
        POSTLIKES
            .whereEqualTo("postID", postID)
            .whereEqualTo("userID", userID)
            .get()
            .addOnSuccessListener { documents ->

                for (document in documents) {
                    document.reference.delete()
                }
            }

    }
}
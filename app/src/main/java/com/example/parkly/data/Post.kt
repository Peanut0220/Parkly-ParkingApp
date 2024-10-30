package com.example.parkly.data

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Post(
    @DocumentId
    val postID: String = "",
    val description: String = "",
    val image: Blob = Blob.fromBytes(ByteArray(0)),
    val createdAt: Long = 0,
    val userID: String = "",
    val comments: Long = 0,
    val likes: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long = 0,
) {
    @get:Exclude
    var user: User = User()
}

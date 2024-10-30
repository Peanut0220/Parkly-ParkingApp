package com.example.parkly.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class PostLikes(
    @DocumentId
    val postLikesID: String = "",
    val postID: String = "",
    val userID: String = "",
) {
    @get:Exclude
    var user: User = User()

    @get:Exclude
    var post: Post = Post()
}

package com.example.parkly.data


import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class PostComments(
    @DocumentId
    val postCommentID: String ="",
    val postID: String = "",
    val createdAt: Long = 0,
    val userID: String = "",
    val comment: String = "",
) {
    @get:Exclude
    var user: User = User()

    @get:Exclude
    var post: Post = Post()
}

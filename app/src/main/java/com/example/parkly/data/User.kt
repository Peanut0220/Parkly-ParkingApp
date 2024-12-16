package com.example.parkly.data

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val avatar: Blob = Blob.fromBytes(ByteArray(0)),
    val dob: Long = 0,
    val type: String = "",
    val token: String = "",
)

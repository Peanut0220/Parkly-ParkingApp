package com.example.parkly.data

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId

data class Company(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val avatar: Blob = Blob.fromBytes(ByteArray(0)),
    val description: String = "",
    val location: String = "",
    val year: Int = -1,
)

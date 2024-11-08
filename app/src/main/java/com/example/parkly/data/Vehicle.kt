package com.example.parkly.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Vehicle(
    @DocumentId
    val vehicleID: String = "",
    val vehicleNumber: String = "",
    var userID :String="",
    val vehicleModel: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val deletedAt: Long = 0,
) {
    @get:Exclude
    var user: User = User()
}
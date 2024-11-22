package com.example.parkly.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import org.joda.time.DateTime

data class Reservation(
    @DocumentId
    val id: String = "",
    val userID: String = "",
    val spaceID: String = "",
    val file: Pdf = Pdf(),
    val reason: String = "",
    val date: Long = 0,
    val startTime: Int = 0,
    val duration: Int =0,
    val status: String = "",
    val createdAt: Long = DateTime.now().millis
) {
    @get:Exclude
    var user: User = User()

    @get:Exclude
    var space: ParkingSpace = ParkingSpace()
}

data class Pdf(
    val name: String? = "",
    val path: String? = "",
)


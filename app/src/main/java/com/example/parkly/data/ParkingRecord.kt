package com.example.parkly.data


import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class ParkingRecord(
    @DocumentId
    val recordID: String ="",
    val spaceID: String = "",
    val userID :String="",
    val startTime: Long = 0,
    val endTime: Long = 0,
    val carImage: Blob = Blob.fromBytes(ByteArray(0)),
    val vehicleNumber: String = ""
) {
    @get:Exclude
    var user: User = User()

    @get:Exclude
    var space: ParkingSpace = ParkingSpace()
}

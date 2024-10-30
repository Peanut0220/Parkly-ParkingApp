package com.example.parkly.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class SaveJob(
    @DocumentId
    val id: String = "",
    val userID: String = "",
    val jobID: String = "",
) {
    @get:Exclude
    var job: Job = Job()
}

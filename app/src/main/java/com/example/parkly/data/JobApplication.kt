package com.example.parkly.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import org.joda.time.DateTime

data class JobApplication(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val jobId: String = "",
    val file: Pdf = Pdf(),
    val info: String = "",
    val status: String = "",
    val createdAt: Long = DateTime.now().millis
) {
    @get:Exclude
    var user: User = User()

    @get:Exclude
    var job: Job = Job()

    @get:Exclude
    val count = 0
}

data class Pdf(
    val name: String? = "",
    val path: String? = "",
)
package com.example.parkly.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Interview(
    @DocumentId
    val id: String = "",
    val jobAppID: String = "",
    val location: String = "",
    val video: String = "",
    val remark: String = "",
    val date: Long = 0,
    val startTime: Time = Time(),
    val endTime: Time = Time(),
) {
    @get:Exclude
    var jobApp: JobApplication = JobApplication()
}

data class Time(val hour: Int = 0, val minutes: Int = 0) {
    val combinedTime = hour * 100 + minutes
}
package com.example.parkly.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Job(
    @DocumentId
    val jobID: String = "",
    val jobName: String = "",
    val position: String = "",
    val jobType: String = "",
    val workplace: String = "",
    val minSalary: Int = -1,
    val maxSalary: Int = -1,
    val qualification: String = "",
    val experience: Int = -1,
    val description: String = "",
    val requirement: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    var deletedAt: Long = 0,
    val companyID: String = "",
) {
    @get:Exclude
    var company: Company = Company()

}

package com.example.parkly.data.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.parkly.data.JobApplication
import com.example.parkly.data.Pdf
import com.example.parkly.util.JobApplicationState
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import org.joda.time.DateTime

class JobApplicationViewModel : ViewModel() {
    private val JOBAPP = Firebase.firestore.collection("job_application")
    private val _jobAppLD = MutableLiveData<List<JobApplication>>()
    val isSuccess = MutableLiveData<Boolean>()
    val response = MutableLiveData<String>()
    val progress = MutableLiveData<Int>()
    val resume = MutableLiveData<Pdf>()


    private var listener: ListenerRegistration? = null

    init {
        listener = JOBAPP.addSnapshotListener { snap, _ ->
            _jobAppLD.value = snap?.toObjects()
        }
    }

    override fun onCleared() {
        listener?.remove()
    }

    fun init() = Unit

    fun getJobAppLD() = _jobAppLD

    fun getAll() = _jobAppLD.value ?: emptyList()

    fun isApplied(uid: String, jobID: String) =
        getAll().any { it.jobId == jobID && it.userId == uid }

    fun get(jobAppID: String) = getAll().find { it.id == jobAppID }

    suspend fun uploadResume(uri: Uri, fileName: String) {
        val storageRef =
            Firebase.storage.reference.child("resume/").child("${DateTime.now()}_$fileName ")

        storageRef.putFile(uri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener {
                resume.value = Pdf(fileName, it.toString())
            }
        }.addOnProgressListener {
            progress.value = (it.bytesTransferred * 100 / it.totalByteCount).toInt()
        }.addOnFailureListener() {
            response.value = it.message.toString()
        }.await()
    }

    suspend fun set(jobApp: JobApplication) {
        JOBAPP.document().set(jobApp).addOnCompleteListener {
            isSuccess.value = it.isSuccessful
        }.await()
    }

     fun updateStatus(status:JobApplicationState,jobID: String){
        JOBAPP.document(jobID).update("status",status.toString()).addOnCompleteListener {
            isSuccess.value = it.isSuccessful
        }.addOnFailureListener {
            response.value = it.toString()
        }
    }


}
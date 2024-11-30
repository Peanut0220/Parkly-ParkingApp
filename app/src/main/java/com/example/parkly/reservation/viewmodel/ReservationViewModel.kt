package com.example.parkly.reservation.viewmodel

import android.app.Application
import android.net.Uri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.parkly.data.ParkingRecord
import com.example.parkly.data.ParkingSpace
import com.example.parkly.data.Pdf
import com.example.parkly.data.Reservation
import com.example.parkly.data.Vehicle
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel
import com.example.parkly.util.JobApplicationState
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import org.joda.time.DateTime

class ReservationViewModel(val app: Application) : AndroidViewModel(app){
    private val RESERVATIONS = Firebase.firestore.collection("reservation")
    private val _reservationLD = MutableLiveData<List<Reservation>>()
    private var listener: ListenerRegistration? = null
    private val required = "* Required"
    val isSuccess = MutableLiveData<Boolean>()
    val response = MutableLiveData<String>()
    val progress = MutableLiveData<Int>()
    val supportedFile = MutableLiveData<Pdf>()

    init {
        listener = RESERVATIONS.addSnapshotListener { snap, _ ->
            _reservationLD.value = snap?.toObjects()
        }
    }

    fun init() = Unit

    fun getreservationLD() = _reservationLD

    fun getAll() = _reservationLD.value ?: emptyList()

    fun get(reservationID: String) = getAll().find { it.id == reservationID }



    fun set(reservation: Reservation) {
        RESERVATIONS.document().set(reservation)
    }

    fun update(reservation: Reservation) {
        RESERVATIONS.document(reservation.id).set(reservation)
    }

    suspend fun uploadFile(uri: Uri, fileName: String) {
        val storageRef =
            Firebase.storage.reference.child("reservationReason/").child("${DateTime.now()}_$fileName ")

        storageRef.putFile(uri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener {
                supportedFile.value = Pdf(fileName, it.toString())
            }
        }.addOnProgressListener {
            progress.value = (it.bytesTransferred * 100 / it.totalByteCount).toInt()
        }.addOnFailureListener() {
            response.value = it.message.toString()
        }.await()
    }

    fun updateStatus(reservationID: String,status: String){
        RESERVATIONS.document(reservationID).update("status",status.toString())
    }

    fun getBySpaceID(spaceID: String): Reservation? {
        return getAll().find { it.spaceID == spaceID }
    }










}
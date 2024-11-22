package com.example.parkly.reservation.viewmodel

import android.app.Application
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.parkly.data.ParkingRecord
import com.example.parkly.data.ParkingSpace
import com.example.parkly.data.Reservation
import com.example.parkly.data.Vehicle
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects

class ReservationViewModel(val app: Application) : AndroidViewModel(app){
    private val RESERVATIONS = Firebase.firestore.collection("reservation")
    private val _reservationLD = MutableLiveData<List<Reservation>>()
    private var listener: ListenerRegistration? = null
    private val required = "* Required"

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





}
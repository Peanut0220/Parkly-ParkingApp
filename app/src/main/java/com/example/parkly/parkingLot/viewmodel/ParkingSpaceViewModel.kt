package com.example.parkly.parkingLot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.parkly.data.ParkingRecord
import com.example.parkly.data.ParkingSpace
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects

class ParkingSpaceViewModel(val app: Application) : AndroidViewModel(app){
    private val PARKINGSPACES = Firebase.firestore.collection("parkingSpace")
    private val _parkingSpaceLD = MutableLiveData<List<ParkingSpace>>()
    private var listener: ListenerRegistration? = null
    private val required = "* Required"

    init {
        listener = PARKINGSPACES.addSnapshotListener { snap, _ ->
            _parkingSpaceLD.value = snap?.toObjects()
        }
    }

    fun init() = Unit

    fun getParkingSpaceLD() = _parkingSpaceLD

    fun getAll() = _parkingSpaceLD.value ?: emptyList()

    fun get(spaceID: String) = getAll().find { it.spaceID == spaceID }


    fun set(space: ParkingSpace) {
        PARKINGSPACES.document().set(space)
    }

    fun update(space: ParkingSpace) {
        PARKINGSPACES.document(space.spaceID).set(space)
    }



    // Function to create multiple parking spaces
    fun createParkingSpaces() {
        val parkingSpaces = mutableListOf<ParkingSpace>()

        // Create space IDs in specified ranges
        val ranges = listOf(
            1..10,   // A1 to A10
            12..21,  // A12 to A21
            22..31,  // A22 to A31
            33..52,  // A33 to A52
            54..73,  // A54 to A73
            75..94,  // A75 to A94
            95..104, // A95 to A104
            106..125,// A106 to A125
            127..146,// A127 to A146
            148..167,// A148 to A167
            169..188 // A169 to A188
        )

        // Generate ParkingSpace objects for each space ID
        for (range in ranges) {
            for (i in range) {
                // Format spaceID as A followed by the number
                val spaceID = "A$i"
                // Initialize ParkingSpace with spaceID and available status
                val parkingSpace = ParkingSpace(
                    spaceID = spaceID,
                    currentUserID = "",
                    currentCarImage =  Blob.fromBytes(ByteArray(0)),
                    spaceStatus = "Available",
                    currentRecordID = ""
                )
                PARKINGSPACES.document(spaceID).set(parkingSpace)
            }
        }

        // Batch set all ParkingSpace objects to Firestore

    }





}
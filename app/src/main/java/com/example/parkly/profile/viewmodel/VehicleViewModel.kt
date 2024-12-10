package com.example.parkly.community.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.parkly.data.Vehicle
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects

class VehicleViewModel(val app: Application) : AndroidViewModel(app){
    private val VEHICLES = Firebase.firestore.collection("vehicle")
    private val _vehicleLD = MutableLiveData<List<Vehicle>>()
    private var listener: ListenerRegistration? = null
    private val required = "* Required (eg: Myvi Red)"

    init {
        listener = VEHICLES.addSnapshotListener { snap, _ ->
            _vehicleLD.value = snap?.toObjects()
        }
    }

    fun init() = Unit

    fun getVehicleLD() = _vehicleLD

    fun getAll() = _vehicleLD.value ?: emptyList()

    fun get(vehicleID: String) = getAll().find { it.vehicleID == vehicleID }
    fun getByUser(userID: String) = getAll().find { it.userID == userID }

    fun set(vehicle: Vehicle) {
        VEHICLES.document().set(vehicle)
    }

    fun update(vehicle: Vehicle) {
        VEHICLES.document(vehicle.vehicleID).set(vehicle)
    }


    fun validateInput(field: TextInputLayout, fieldValue: String): Boolean {
        val isValid = !fieldValue.isNullOrEmpty()
        field.helperText = if (isValid) "" else required
        return isValid
    }

    private val resultLD = MutableLiveData<List<Vehicle>>()

    fun getResultLD() = resultLD





}
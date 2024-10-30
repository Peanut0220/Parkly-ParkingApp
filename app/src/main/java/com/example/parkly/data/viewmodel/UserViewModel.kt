package com.example.parkly.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.parkly.data.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await

class UserViewModel(val app: Application) : AndroidViewModel(app) {
    private val USERS = Firebase.firestore.collection("user")
    private val _userLD = MutableLiveData<User>()
    private val _userLLD = MutableLiveData<List<User>>()

    private val auth = Firebase.auth
    private var listener: ListenerRegistration? = null
    private var listener2: ListenerRegistration? = null

    val response = MutableLiveData<Boolean>()

    init {
        listener = auth.currentUser?.let {
            USERS.document(it.uid).addSnapshotListener { snap, _ ->
                _userLD.value = snap?.toObject()
            }
        }

        listener2 = USERS.addSnapshotListener{ snap, _ ->
            _userLLD.value = snap?.toObjects()
        }
    }

    fun getUserLD() = _userLD

    fun getUserLLD() = _userLLD

    suspend fun set(user: User) {
        USERS.document(user.uid).set(user).addOnCompleteListener {
        }.await()
    }

    suspend fun setToken(token: String) {
        USERS.document(auth.currentUser!!.uid).update("token", token).await()
    }

    suspend fun update(user: User) {
        USERS.document(auth.currentUser!!.uid)
            .update(
                "name", user.name,
                "avatar", user.avatar
            )
            .addOnCompleteListener {
                response.value = it.isSuccessful
            }.await()
    }

    fun getAuth() = auth.currentUser!!.let {
        User(
            uid = it.uid,
            email = it.email ?: "",
            name = it.displayName ?: "User#${it.uid.take(8)}",
        )
    }

    fun getAll() = _userLLD.value ?: emptyList()
    fun get(userID: String) = getAll().find { it.uid == userID }
    fun getByCompanyID(companyID: String) = getAll().find { it.company_id == companyID }

    fun isEnterprise() = _userLD.value!!.isEnterprise
    fun isCompanyRegistered() = _userLD.value?.company_id != ""

    fun isVerified() = auth.currentUser!!.isEmailVerified
    fun attachCompany(id: String) {
        USERS.document(getAuth().uid).update("company_id", id)
    }

    fun init() = Unit
}
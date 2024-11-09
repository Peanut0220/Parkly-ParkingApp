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
                "avatar", user.avatar,
                "ic",user.ic,
                "phone",user.phone,
                "dob",user.dob,
                "type",user.type

            )
            .addOnCompleteListener {
                response.value = it.isSuccessful
            }.await()
    }

    fun isUserDataComplete(user: User): Boolean {
        return user.uid.isNotBlank() &&
                user.name.isNotBlank() &&
                user.email.isNotBlank() &&
                user.phone.isNotBlank() &&
                user.ic.isNotBlank() &&
                user.dob != 0L && // Ensures dob has a valid timestamp
                user.type.isNotBlank()
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

    fun isVerified() = auth.currentUser!!.isEmailVerified

    fun init() = Unit
}
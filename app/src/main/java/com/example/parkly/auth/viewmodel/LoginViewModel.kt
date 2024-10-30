package com.example.parkly.auth.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class LoginViewModel(val app: Application) :
    AndroidViewModel(app) {
        val auth = Firebase.auth

    private val _signInResult = MutableLiveData<Boolean>()
    val signInResult = _signInResult

    private val _response = MutableLiveData<String>()
    val response = _response

    fun isLoggedIn() = auth.currentUser != null

    fun isVerified() = auth.currentUser!!.isEmailVerified

    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                _signInResult.value = task.isSuccessful
            }
    }

    fun firebaseAuthWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            _signInResult.value = isVerified()
            if (!isVerified())
                auth.signOut()
        }.addOnFailureListener {
            _response.value = it.message
        }
    }
     fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()
    }


    suspend fun getCurrentUser() {
        auth.currentUser?.reload()?.await()
        Log.d("USER", " ${auth.currentUser?.uid}")
        Log.d("USER", " ${auth.currentUser?.isEmailVerified}")
    }


}
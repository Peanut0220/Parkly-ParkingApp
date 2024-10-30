package com.example.parkly.auth.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class SignUpViewModel(val app: Application) : AndroidViewModel(app) {
    val auth = Firebase.auth

    private val _isSignUpSuccess = MutableLiveData<Boolean>()
    val isSignUpSuccess = _isSignUpSuccess

    private val _errorResponseMsg = MutableLiveData<String>()
    val errorResponseMsg = _errorResponseMsg

    fun signUpWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isSignUpSuccess.value = task.isSuccessful
                    sendEmailVerification()
                }
            }
            .addOnFailureListener {
                _errorResponseMsg.value = it.message
                Log.d("Error", "signUpWithEmail: " + it.message)
            }
    }
    private fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()?.addOnFailureListener {
            errorResponseMsg.value = it.message
        }
    }


}
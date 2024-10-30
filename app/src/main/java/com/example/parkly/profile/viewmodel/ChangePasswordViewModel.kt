package com.example.parkly.profile.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ChangePasswordViewModel : ViewModel() {
    val user = Firebase.auth.currentUser
    val isCorrectPassword = MutableLiveData<Boolean>()
    val isSuccess = MutableLiveData<Boolean>()
    val response = MutableLiveData<String>()

    fun isCurrentPasswordValid(password: String = "") {
        if (user == null) {
            response.value = "Something went wrong."
            return
        }
        val result = EmailAuthProvider.getCredential(user.email!!, password)
        Log.d("SIGN IN METHID", "isCurrentPasswordValid: ${result.signInMethod}")
        user.reauthenticate(result).addOnCompleteListener {
            isCorrectPassword.value = it.isSuccessful
        }.addOnFailureListener {
            response.value = "Current Password Incorrect."
        }
    }

    fun resetPassword(newPassword: String = "") {
        if (user == null) {
            response.value = "Something went wrong."
            return
        }

        user.updatePassword(newPassword).addOnCompleteListener {
            isSuccess.value = it.isSuccessful
        }.addOnFailureListener {
            response.value = it.toString()
        }
    }

    fun isPasswordLogin(): Boolean =
        user?.providerData!!.any { it.providerId == EmailAuthProvider.PROVIDER_ID }


}
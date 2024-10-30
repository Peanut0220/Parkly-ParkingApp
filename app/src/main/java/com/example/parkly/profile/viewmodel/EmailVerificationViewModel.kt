package com.example.parkly.profile.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.util.Timer
import java.util.TimerTask

class EmailVerificationViewModel : ViewModel() {
    val auth = Firebase.auth

    private val _isVerified = MutableLiveData<Boolean>()
    val isVerified = _isVerified

    private val _errorResponseMsg = MutableLiveData<String>()
    val errorResponseMsg = _errorResponseMsg
    fun checkEmailVerificationInterval() {
        val timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                Log.d("timer task", "run: $isVerified")
                auth.currentUser?.reload()
                _isVerified.postValue(auth.currentUser!!.isEmailVerified)
                if (auth.currentUser!!.isEmailVerified)
                    timer.cancel()
            }
        }

        if (auth.currentUser != null)
            timer.scheduleAtFixedRate(timerTask, 0, 3000)
    }

    fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()?.addOnFailureListener {
            errorResponseMsg.value = it.message
        }
    }
}
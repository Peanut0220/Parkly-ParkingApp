package com.example.parkly

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Handle incoming messages here
        Log.d(TAG, "From: ${remoteMessage.from}")
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            // Handle data payload
        }
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            // Handle notification
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Get updated FCM registration token
        Log.d(TAG, "Refreshed token: $token")
        // You can now save the token to your database or send it to your server

    }

    companion object {
        private const val TAG = "MyFirebaseMessagingServ"
    }
}


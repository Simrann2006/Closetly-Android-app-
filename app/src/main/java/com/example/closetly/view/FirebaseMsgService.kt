package com.example.closetly.view

import android.util.Log
import com.example.closetly.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMsgService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FirebaseMsgService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }
        
        remoteMessage.notification?.let {
            Log.d(TAG, "Message notification: ${it.title} - ${it.body}")
            handleNotificationPayload(it, remoteMessage.data)
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        val title = data["title"]
        val body = data["body"]
        val type = data["type"] // chat, post, general
        
        NotificationHelper.showNotification(
            context = this,
            title = title,
            message = body,
            type = type,
            data = data
        )
    }

    private fun handleNotificationPayload(
        notification: RemoteMessage.Notification,
        data: Map<String, String>
    ) {
        val type = data["type"] ?: NotificationHelper.TYPE_GENERAL
        
        NotificationHelper.showNotification(
            context = this,
            title = notification.title,
            message = notification.body,
            type = type,
            data = data
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token generated: $token")
        
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(userId)
            userRef.child("fcmToken").setValue(token)
                .addOnSuccessListener {
                    Log.d(TAG, "FCM token updated successfully in database")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update FCM token: ${e.message}")
                }
        } else {
            Log.w(TAG, "onNewToken called but user is not logged in. Token will be updated on next login.")
        }
    }
}
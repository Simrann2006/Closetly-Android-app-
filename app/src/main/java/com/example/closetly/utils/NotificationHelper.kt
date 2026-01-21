package com.example.closetly.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.closetly.ChatActivity
import com.example.closetly.DashboardActivity
import com.example.closetly.NotificationActivity
import com.example.closetly.R
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object NotificationHelper {

    private const val TAG = "NotificationHelper"

    private const val FCM_API_URL =
        "https://fcm.googleapis.com/v1/projects/closetly-f2f11/messages:send"

    const val CHANNEL_CHAT_ID = "chat_channel"
    const val CHANNEL_POST_ID = "post_channel"
    const val CHANNEL_GENERAL_ID = "general_channel"

    const val TYPE_CHAT = "chat"
    const val TYPE_POST = "post"
    const val TYPE_GENERAL = "general"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val chatChannel = NotificationChannel(
                CHANNEL_CHAT_ID,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new chat messages"
                enableVibration(true)
                enableLights(true)
            }

            val postChannel = NotificationChannel(
                CHANNEL_POST_ID,
                "Post Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new posts and interactions"
                enableVibration(true)
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL_ID,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            notificationManager.createNotificationChannel(chatChannel)
            notificationManager.createNotificationChannel(postChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }

    fun showNotification(
        context: Context,
        title: String?,
        message: String?,
        type: String?,
        data: Map<String, String>
    ) {
        val notificationType = type ?: TYPE_GENERAL

        when (notificationType) {
            TYPE_CHAT -> showChatNotification(context, title, message, data)
            TYPE_POST -> showPostNotification(context, title, message, data)
            else -> showGeneralNotification(context, title, message)
        }
    }

    private fun showChatNotification(
        context: Context,
        title: String?,
        message: String?,
        data: Map<String, String>
    ) {
        val chatId = data["chatId"]
        val senderId = data["senderId"]
        val senderName = data["senderName"]

        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra("chatId", chatId)
            putExtra("senderId", senderId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_CHAT_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title ?: senderName ?: "New Message")
            .setContentText(message ?: "You have a new message")
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)

        val replyIntent = Intent(context, ChatActivity::class.java).apply {
            putExtra("chatId", chatId)
            putExtra("senderId", senderId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val replyPendingIntent = PendingIntent.getActivity(
            context,
            (chatId.hashCode() + 1),
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notificationBuilder.addAction(
            R.drawable.chat,
            "Reply",
            replyPendingIntent
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(chatId.hashCode(), notificationBuilder.build())
    }

    private fun showPostNotification(
        context: Context,
        title: String?,
        message: String?,
        data: Map<String, String>
    ) {
        val postId = data["postId"]
        val notificationType = data["notificationType"]

        val intent = Intent(context, NotificationActivity::class.java).apply {
            putExtra("postId", postId)
            putExtra("type", notificationType)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            postId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_POST_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title ?: "New Activity")
            .setContentText(message ?: "Someone interacted with your post")
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(postId.hashCode(), notificationBuilder.build())
    }

    /**
     * Show general notification
     */
    private fun showGeneralNotification(
        context: Context,
        title: String?,
        message: String?
    ) {
        val intent = Intent(context, DashboardActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_GENERAL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title ?: "Closetly")
            .setContentText(message ?: "You have a new notification")
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    fun sendChatNotification(
        context: Context,
        receiverUserId: String,
        senderName: String,
        messageText: String,
        chatId: String,
        senderId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getUserFcmToken(receiverUserId) ?: return@launch

                val payload = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("token", token)

                        put("notification", JSONObject().apply {
                            put("title", senderName)
                            put("body", messageText)
                        })

                        put("data", JSONObject().apply {
                            put("type", TYPE_CHAT)
                            put("title", senderName)
                            put("body", messageText)
                            put("chatId", chatId)
                            put("senderId", senderId)
                            put("senderName", senderName)
                        })

                        put("android", JSONObject().apply {
                            put("priority", "HIGH")
                        })
                    })
                }

                sendFcmV1Request(context, payload)

            } catch (e: Exception) {
                Log.e(TAG, "Error sending chat notification: ${e.message}")
            }
        }
    }

    fun sendPostNotification(
        context: Context,
        receiverUserId: String,
        actorName: String,
        notificationType: String,
        postId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getUserFcmToken(receiverUserId) ?: return@launch

                val title = when (notificationType) {
                    "like" -> "New Like ❤️"
                    "comment" -> "New Comment 💬"
                    "share" -> "Post Shared 🔄"
                    else -> "New Activity"
                }

                val body = when (notificationType) {
                    "like" -> "$actorName liked your post"
                    "comment" -> "$actorName commented on your post"
                    "share" -> "$actorName shared your post"
                    else -> "$actorName interacted with your post"
                }

                val payload = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("token", token)
                        put("notification", JSONObject().apply {
                            put("title", title)
                            put("body", body)
                        })
                        put("data", JSONObject().apply {
                            put("type", TYPE_POST)
                            put("title", title)
                            put("body", body)
                            put("postId", postId)
                            put("notificationType", notificationType)
                        })
                        put("android", JSONObject().apply {
                            put("priority", "HIGH")
                        })
                    })
                }

                sendFcmV1Request(context, payload)

            } catch (e: Exception) {
                Log.e(TAG, "Error sending post notification: ${e.message}")
            }
        }
    }

    fun sendGeneralNotification(
        context: Context,
        receiverUserId: String,
        title: String,
        message: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getUserFcmToken(receiverUserId) ?: return@launch

                val payload = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("token", token)
                        put("notification", JSONObject().apply {
                            put("title", title)
                            put("body", message)
                        })
                        put("data", JSONObject().apply {
                            put("type", TYPE_GENERAL)
                            put("title", title)
                            put("body", message)
                        })
                        put("android", JSONObject().apply {
                            put("priority", "HIGH")
                        })
                    })
                }

                sendFcmV1Request(context, payload)

            } catch (e: Exception) {
                Log.e(TAG, "Error sending general notification: ${e.message}")
            }
        }
    }

    private suspend fun getUserFcmToken(userId: String): String? {
        return try {
            val snapshot = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("fcmToken")
                .get()
                .await()

            snapshot.getValue(String::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting FCM token: ${e.message}")
            null
        }
    }

    private suspend fun sendFcmV1Request(context: Context, payload: JSONObject) {
        try {
            val accessToken = getOAuth2AccessToken(context)

            val connection = URL(FCM_API_URL).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.setRequestProperty("Content-Type", "application/json; UTF-8")
            connection.doOutput = true

            connection.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "FCM notification sent successfully")
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.readText()
                Log.e(TAG, "FCM error: $responseCode - $errorStream")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error sending FCM request: ${e.message}")
        }
    }

    private suspend fun getOAuth2AccessToken(context: Context): String {
        return withContext(Dispatchers.IO) {
            try {
                val serviceAccountStream = context.assets.open("service-account.json")
                
                val credentials = GoogleCredentials
                    .fromStream(serviceAccountStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
                
                credentials.refresh()
                
                val accessToken = credentials.accessToken.tokenValue
                
                Log.d(TAG, "OAuth 2.0 access token generated successfully")
                accessToken
                
            } catch (e: Exception) {
                Log.e(TAG, "Error generating OAuth token: ${e.message}")
                throw e
            }
        }
    }
}
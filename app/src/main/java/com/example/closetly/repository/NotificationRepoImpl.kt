package com.example.closetly.repository

import android.content.Context
import com.example.closetly.model.NotificationType
import com.example.closetly.utils.NotificationHelper
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class NotificationRepoImpl : NotificationRepo {
    private val database = FirebaseDatabase.getInstance()
    private val notificationsRef = database.getReference("Notifications")
    private val usersRef = database.getReference("Users")

    override fun sendFollowNotification(
        context: Context,
        senderId: String,
        senderName: String,
        senderImage: String,
        receiverId: String
    ) {
        if (senderId == receiverId) return
        
        val notificationId = notificationsRef.child(receiverId).push().key ?: return
        val timestamp = System.currentTimeMillis()
        
        val notification = mapOf(
            "notificationId" to notificationId,
            "senderId" to senderId,
            "userName" to senderName,
            "userProfileImage" to senderImage,
            "type" to NotificationType.FOLLOW.name,
            "message" to "started following you",
            "time" to getTimeAgo(timestamp),
            "timestamp" to timestamp,
            "isRead" to false,
            "postId" to "",
            "postImage" to "",
            "commentText" to ""
        )
        
        notificationsRef.child(receiverId).child(notificationId).setValue(notification)
        
        // Send push notification
        NotificationHelper.sendFollowNotification(
            context = context,
            receiverUserId = receiverId,
            followerName = senderName,
            followerId = senderId
        )
    }
    
    override fun sendLikeNotification(
        context: Context,
        senderId: String,
        senderName: String,
        senderImage: String,
        postOwnerId: String,
        postId: String,
        postImage: String
    ) {
        if (senderId == postOwnerId) return
        
        val notificationId = notificationsRef.child(postOwnerId).push().key ?: return
        val timestamp = System.currentTimeMillis()
        
        val notification = mapOf(
            "notificationId" to notificationId,
            "senderId" to senderId,
            "userName" to senderName,
            "userProfileImage" to senderImage,
            "type" to NotificationType.LIKE.name,
            "message" to "liked your post",
            "time" to getTimeAgo(timestamp),
            "timestamp" to timestamp,
            "isRead" to false,
            "postId" to postId,
            "postImage" to postImage,
            "commentText" to ""
        )
        
        notificationsRef.child(postOwnerId).child(notificationId).setValue(notification)
        
        // Send push notification
        NotificationHelper.sendPostNotification(
            context = context,
            receiverUserId = postOwnerId,
            actorName = senderName,
            notificationType = "like",
            postId = postId
        )
    }
    
    override fun sendCommentNotification(
        context: Context,
        senderId: String,
        senderName: String,
        senderImage: String,
        postOwnerId: String,
        postId: String,
        postImage: String,
        commentText: String
    ) {
        if (senderId == postOwnerId) return
        
        val notificationId = notificationsRef.child(postOwnerId).push().key ?: return
        val timestamp = System.currentTimeMillis()
        
        val notification = mapOf(
            "notificationId" to notificationId,
            "senderId" to senderId,
            "userName" to senderName,
            "userProfileImage" to senderImage,
            "type" to NotificationType.COMMENT.name,
            "message" to "commented: $commentText",
            "time" to getTimeAgo(timestamp),
            "timestamp" to timestamp,
            "isRead" to false,
            "postId" to postId,
            "postImage" to postImage,
            "commentText" to commentText
        )
        
        notificationsRef.child(postOwnerId).child(notificationId).setValue(notification)
        
        // Send push notification
        NotificationHelper.sendPostNotification(
            context = context,
            receiverUserId = postOwnerId,
            actorName = senderName,
            notificationType = "comment",
            postId = postId
        )
    }
    
    override fun sendPostNotification(
        postOwnerId: String,
        postOwnerName: String,
        postOwnerImage: String,
        postId: String,
        postImage: String,
        postCaption: String
    ) {
        if (!postCaption.contains("sale", ignoreCase = true)) return
        
        usersRef.child(postOwnerId).child("followers").get().addOnSuccessListener { snapshot ->
            val timestamp = System.currentTimeMillis()
            
            snapshot.children.forEach { followerSnapshot ->
                val followerId = followerSnapshot.key ?: return@forEach
                if (followerId == postOwnerId) return@forEach
                
                val notificationId = notificationsRef.child(followerId).push().key ?: return@forEach
                
                val notification = mapOf(
                    "notificationId" to notificationId,
                    "senderId" to postOwnerId,
                    "userName" to postOwnerName,
                    "userProfileImage" to postOwnerImage,
                    "type" to NotificationType.POST.name,
                    "message" to "posted a new sale",
                    "time" to getTimeAgo(timestamp),
                    "timestamp" to timestamp,
                    "isRead" to false,
                    "postId" to postId,
                    "postImage" to postImage,
                    "commentText" to ""
                )
                
                notificationsRef.child(followerId).child(notificationId).setValue(notification)
            }
        }
    }
    
    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            diff < 2592000000 -> "${diff / 604800000}w ago"
            else -> {
                val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }
}

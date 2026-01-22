package com.example.closetly.repository

import android.content.Context

interface NotificationRepo {
    fun sendFollowNotification(
        context: Context,
        senderId: String,
        senderName: String,
        senderImage: String,
        receiverId: String
    )
    
    fun sendLikeNotification(
        context: Context,
        senderId: String,
        senderName: String,
        senderImage: String,
        postOwnerId: String,
        postId: String,
        postImage: String
    )
    
    fun sendCommentNotification(
        context: Context,
        senderId: String,
        senderName: String,
        senderImage: String,
        postOwnerId: String,
        postId: String,
        postImage: String,
        commentText: String
    )
    
    fun sendPostNotification(
        postOwnerId: String,
        postOwnerName: String,
        postOwnerImage: String,
        postId: String,
        postImage: String,
        postCaption: String
    )
}

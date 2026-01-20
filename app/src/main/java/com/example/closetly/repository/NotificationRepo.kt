package com.example.closetly.repository

interface NotificationRepo {
    fun sendFollowNotification(
        senderId: String,
        senderName: String,
        senderImage: String,
        receiverId: String
    )
    
    fun sendLikeNotification(
        senderId: String,
        senderName: String,
        senderImage: String,
        postOwnerId: String,
        postId: String,
        postImage: String
    )
    
    fun sendCommentNotification(
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

package com.example.closetly.data.model

data class Comment(
    val id: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val userProfileImage: String,
    val commentText: String,
    val timestamp: Long,
    val likesCount: Int = 0,
    val isLiked: Boolean = false
)

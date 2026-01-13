package com.example.closetly.model

data class PostModel(
    val postId: String = "",
    val caption: String = "",
    val imageUrl: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfilePic: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0
)

package com.example.closetly.model

data class CommentModel(
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
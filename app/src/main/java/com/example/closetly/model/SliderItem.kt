package com.example.closetly.model

data class SliderItem(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Long = 0L,
    val likesCount: Int = 0,
    val commentsCount: Int = 0
)

package com.example.closetly.model

data class MessageModel(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)

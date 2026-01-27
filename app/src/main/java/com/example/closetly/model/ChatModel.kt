package com.example.closetly.model

data class ChatModel(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val lastMessageSenderId: String = "",
    val unreadCount: Map<String, Int> = emptyMap(),
    val lastSeenAt: Map<String, Long> = emptyMap(),  // userId -> Last time they saw messages
    val typingStatus: Map<String, Long> = emptyMap()  // userId -> timestamp when they started typing
)

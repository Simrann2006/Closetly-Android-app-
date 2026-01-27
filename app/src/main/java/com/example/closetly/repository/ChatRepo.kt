package com.example.closetly.repository

import com.example.closetly.model.ChatModel
import com.example.closetly.model.MessageModel
import com.example.closetly.model.UserModel

interface ChatRepo {

    fun getOrCreateChat(
        currentUserId: String,
        otherUserId: String,
        callback: (Boolean, String, String?) -> Unit
    )

    fun sendMessage(
        chatId: String,
        message: MessageModel,
        callback: (Boolean, String) -> Unit
    )

    fun getMessages(
        chatId: String,
        callback: (Boolean, String, List<MessageModel>) -> Unit
    )

    fun getUserChats(
        userId: String,
        callback: (Boolean, String, List<Pair<ChatModel, UserModel>>) -> Unit
    )

    fun markMessagesAsRead(
        chatId: String,
        userId: String,
        callback: (Boolean, String) -> Unit
    )

    fun deleteChat(
        chatId: String,
        callback: (Boolean, String) -> Unit
    )
    
    fun deleteMessage(
        chatId: String,
        messageId: String,
        callback: (Boolean, String) -> Unit
    )
    
    fun unsendMessage(
        chatId: String,
        messageId: String,
        callback: (Boolean, String) -> Unit
    )
    
    fun setTypingStatus(
        chatId: String,
        userId: String,
        isTyping: Boolean
    )
    
    fun listenForTypingStatus(
        chatId: String,
        otherUserId: String,
        callback: (Boolean) -> Unit
    )
}

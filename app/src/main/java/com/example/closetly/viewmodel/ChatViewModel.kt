package com.example.closetly.viewmodel

import androidx.lifecycle.ViewModel
import com.example.closetly.model.ChatModel
import com.example.closetly.model.MessageModel
import com.example.closetly.model.UserModel
import com.example.closetly.repository.ChatRepo

class ChatViewModel(val repo: ChatRepo) : ViewModel() {

    fun getOrCreateChat(
        currentUserId: String,
        otherUserId: String,
        callback: (Boolean, String, String?) -> Unit
    ) {
        repo.getOrCreateChat(currentUserId, otherUserId, callback)
    }

    fun sendMessage(
        chatId: String,
        message: MessageModel,
        callback: (Boolean, String) -> Unit
    ) {
        repo.sendMessage(chatId, message, callback)
    }

    fun getMessages(
        chatId: String,
        callback: (Boolean, String, List<MessageModel>) -> Unit
    ) {
        repo.getMessages(chatId, callback)
    }

    fun getUserChats(
        userId: String,
        callback: (Boolean, String, List<Pair<ChatModel, UserModel>>) -> Unit
    ) {
        repo.getUserChats(userId, callback)
    }

    fun markMessagesAsRead(
        chatId: String,
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.markMessagesAsRead(chatId, userId, callback)
    }

    fun deleteChat(
        chatId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteChat(chatId, callback)
    }
    
    fun deleteMessage(
        chatId: String,
        messageId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteMessage(chatId, messageId, callback)
    }
    
    fun unsendMessage(
        chatId: String,
        messageId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.unsendMessage(chatId, messageId, callback)
    }
    
    fun setTypingStatus(
        chatId: String,
        userId: String,
        isTyping: Boolean
    ) {
        repo.setTypingStatus(chatId, userId, isTyping)
    }
    
    fun listenForTypingStatus(
        chatId: String,
        otherUserId: String,
        callback: (Boolean) -> Unit
    ) {
        repo.listenForTypingStatus(chatId, otherUserId, callback)
    }
}

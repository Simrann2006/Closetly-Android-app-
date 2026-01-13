package com.example.closetly.repository

import android.util.Log
import com.example.closetly.model.ChatModel
import com.example.closetly.model.MessageModel
import com.example.closetly.model.UserModel
import com.google.firebase.database.*

class ChatRepoImpl : ChatRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val chatsRef: DatabaseReference = database.getReference("Chats")
    private val messagesRef: DatabaseReference = database.getReference("Messages")
    private val usersRef: DatabaseReference = database.getReference("Users")

    override fun getOrCreateChat(
        currentUserId: String,
        otherUserId: String,
        callback: (Boolean, String, String?) -> Unit
    ) {
        val participants = listOf(currentUserId, otherUserId).sorted()
        val chatId = participants.joinToString("_")

        chatsRef.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    callback(true, "Chat exists", chatId)
                } else {
                    val chat = ChatModel(
                        chatId = chatId,
                        participants = participants,
                        lastMessage = "",
                        lastMessageTime = System.currentTimeMillis(),
                        lastMessageSenderId = "",
                        unreadCount = mapOf(currentUserId to 0, otherUserId to 0)
                    )
                    chatsRef.child(chatId).setValue(chat).addOnCompleteListener {
                        if (it.isSuccessful) {
                            callback(true, "Chat created", chatId)
                        } else {
                            callback(false, "${it.exception?.message}", null)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun sendMessage(
        chatId: String,
        message: MessageModel,
        callback: (Boolean, String) -> Unit
    ) {
        val messageId = messagesRef.child(chatId).push().key ?: return
        val messageWithId = message.copy(messageId = messageId)

        messagesRef.child(chatId).child(messageId).setValue(messageWithId)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    chatsRef.child(chatId).child("lastMessage").setValue(message.text)
                    chatsRef.child(chatId).child("lastMessageTime").setValue(message.timestamp)
                    chatsRef.child(chatId).child("lastMessageSenderId").setValue(message.senderId)
                    
                    chatsRef.child(chatId).child("participants")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val participants = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                                participants.forEach { userId ->
                                    if (userId != message.senderId) {
                                        chatsRef.child(chatId).child("unreadCount").child(userId)
                                            .get().addOnSuccessListener {
                                                val count = it.getValue(Int::class.java) ?: 0
                                                chatsRef.child(chatId).child("unreadCount")
                                                    .child(userId).setValue(count + 1)
                                            }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    
                    callback(true, "Message sent")
                } else {
                    callback(false, "${task.exception?.message}")
                }
            }
    }

    override fun getMessages(
        chatId: String,
        callback: (Boolean, String, List<MessageModel>) -> Unit
    ) {
        messagesRef.child(chatId).orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<MessageModel>()
                    snapshot.children.forEach {
                        val message = it.getValue(MessageModel::class.java)
                        message?.let { msg -> messages.add(msg) }
                    }
                    callback(true, "Messages retrieved", messages)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, emptyList())
                }
            })
    }

    override fun getUserChats(
        userId: String,
        callback: (Boolean, String, List<Pair<ChatModel, UserModel>>) -> Unit
    ) {
        chatsRef.orderByChild("lastMessageTime")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatList = mutableListOf<Pair<ChatModel, UserModel>>()
                    var processedCount = 0
                    val totalChats = snapshot.children.mapNotNull { chatSnapshot ->
                        try {
                            if (chatSnapshot.value !is Map<*, *>) {
                                Log.w("ChatRepoImpl", "Skipping non-object entry at key '${chatSnapshot.key}'")
                                return@mapNotNull null
                            }
                            
                            val chat = chatSnapshot.getValue(ChatModel::class.java)
                            if (chat?.participants?.contains(userId) == true) chat else null
                        } catch (e: Exception) {
                            null
                        }
                    }.count()

                    if (totalChats == 0) {
                        callback(true, "No chats", emptyList())
                        return
                    }

                    snapshot.children.forEach { chatSnapshot ->
                        try {
                            if (chatSnapshot.value !is Map<*, *>) {
                                return@forEach
                            }
                            
                            val chat = chatSnapshot.getValue(ChatModel::class.java)
                            if (chat != null && chat.participants.contains(userId)) {
                            val otherUserId = chat.participants.firstOrNull { it != userId } ?: return@forEach
                            
                            usersRef.child(otherUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val user = userSnapshot.getValue(UserModel::class.java)
                                    if (user != null) {
                                        chatList.add(Pair(chat, user))
                                    }
                                    processedCount++
                                    if (processedCount == totalChats) {
                                        val sortedList = chatList.sortedByDescending { it.first.lastMessageTime }
                                        callback(true, "Chats retrieved", sortedList)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    processedCount++
                                    if (processedCount == totalChats) {
                                        val sortedList = chatList.sortedByDescending { it.first.lastMessageTime }
                                        callback(true, "Chats retrieved", sortedList)
                                    }
                                }
                            })
                        }
                        } catch (e: Exception) {
                            Log.e("ChatRepoImpl", "Error processing chat at key '${chatSnapshot.key}': ${e.message}")
                            processedCount++
                            if (processedCount == totalChats) {
                                val sortedList = chatList.sortedByDescending { it.first.lastMessageTime }
                                callback(true, "Chats retrieved", sortedList)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, emptyList())
                }
            })
    }

    override fun markMessagesAsRead(
        chatId: String,
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        chatsRef.child(chatId).child("unreadCount").child(userId).setValue(0)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Marked as read")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun deleteChat(
        chatId: String,
        callback: (Boolean, String) -> Unit
    ) {
        chatsRef.child(chatId).removeValue().addOnCompleteListener { chatTask ->
            if (chatTask.isSuccessful) {
                messagesRef.child(chatId).removeValue().addOnCompleteListener { messagesTask ->
                    if (messagesTask.isSuccessful) {
                        callback(true, "Chat deleted")
                    } else {
                        callback(false, "${messagesTask.exception?.message}")
                    }
                }
            } else {
                callback(false, "${chatTask.exception?.message}")
            }
        }
    }
}

package com.example.closetly.repository

import com.example.closetly.model.SliderItem
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SliderRepoImpl : SliderRepo {
    
    private val database = FirebaseDatabase.getInstance()
    private val postsRef = database.getReference("posts")
    
    override fun getSliderItems(): Flow<List<SliderItem>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<SliderItem>()
                for (child in snapshot.children) {
                    try {
                        val postId = child.key ?: ""
                        val userId = child.child("userId").getValue(String::class.java) ?: ""
                        val username = child.child("username").getValue(String::class.java) ?: ""
                        val imageUrl = child.child("imageUrl").getValue(String::class.java) ?: ""
                        val caption = child.child("caption").getValue(String::class.java) ?: ""
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                        val likesCount = child.child("likesCount").getValue(Int::class.java) ?: 0
                        val commentsCount = child.child("commentsCount").getValue(Int::class.java) ?: 0

                        if (postId.isNotEmpty() && imageUrl.isNotEmpty()) {
                            items.add(
                                SliderItem(
                                    id = postId,
                                    postId = postId,
                                    userId = userId,
                                    username = username,
                                    imageUrl = imageUrl,
                                    caption = caption,
                                    timestamp = timestamp,
                                    likesCount = likesCount,
                                    commentsCount = commentsCount
                                )
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                // Sort by timestamp descending (newest first) and take top 10
                items.sortByDescending { it.timestamp }
                trySend(items.take(10))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        postsRef.addValueEventListener(listener)
        
        awaitClose {
            postsRef.removeEventListener(listener)
        }
    }
}

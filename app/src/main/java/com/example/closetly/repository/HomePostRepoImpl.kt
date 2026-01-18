package com.example.closetly.repository

import android.util.Log
import com.example.closetly.model.PostModel
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class HomePostRepoImpl : HomePostRepo {
    
    private val database = FirebaseDatabase.getInstance()
    private val postsRef = database.getReference("Posts")
    private val productsRef = database.getReference("Products")
    private val usersRef = database.getReference("Users")
    
    companion object {
        private const val TAG = "HomePostRepoImpl"
    }

    override fun getAllPostsRealTime(): Flow<List<PostModel>> = callbackFlow {
        val allPosts = mutableListOf<PostModel>()
        var postsLoaded = false
        var productsLoaded = false
        
        fun emitCombinedData() {
            if (postsLoaded && productsLoaded) {
                // Sort by timestamp (newest first)
                val sorted = allPosts.sortedByDescending { it.resolveTimestamp() }
                Log.d(TAG, "Emitting ${sorted.size} total items (posts + products)")
                trySend(sorted)
            }
        }
        
        val postsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Posts data changed: ${snapshot.childrenCount} posts")
                
                // Clear and reload posts
                allPosts.removeAll { it.postType == "post" }
                
                snapshot.children.forEach { postSnapshot ->
                    try {
                        postSnapshot.getValue(PostModel::class.java)?.let { post ->
                            // Ensure it's marked as a post
                            val postWithType = post.copy(postType = "post")
                            allPosts.add(postWithType)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing post: ${e.message}", e)
                    }
                }
                
                postsLoaded = true
                emitCombinedData()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Posts listener cancelled: ${error.message}")
                close(error.toException())
            }
        }
        
        val productsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Products data changed: ${snapshot.childrenCount} products")
                
                // Clear and reload products
                allPosts.removeAll { it.postType == "product" }
                
                snapshot.children.forEach { productSnapshot ->
                    try {
                        val productId = productSnapshot.key ?: ""
                        val title = productSnapshot.child("title").getValue(String::class.java) ?: ""
                        val description = productSnapshot.child("description").getValue(String::class.java) ?: ""
                        val imageUrl = productSnapshot.child("imageUrl").getValue(String::class.java) ?: ""
                        val price = productSnapshot.child("price").getValue(Double::class.java) ?: 0.0
                        val sellerId = productSnapshot.child("sellerId").getValue(String::class.java) ?: ""
                        val sellerName = productSnapshot.child("sellerName").getValue(String::class.java) ?: ""
                        val sellerProfilePic = productSnapshot.child("sellerProfilePic").getValue(String::class.java) ?: ""
                        val timestamp = productSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                        val status = productSnapshot.child("status").getValue(String::class.java) ?: "Available"
                        
                        if (status == "Available" && productId.isNotEmpty() && sellerId.isNotEmpty()) {
                            val productAsPost = PostModel(
                                postId = productId,
                                caption = description,
                                text = description,
                                title = title,
                                imageUrl = imageUrl,
                                userId = sellerId,
                                username = sellerName,
                                userProfilePic = sellerProfilePic,
                                profilePicture = sellerProfilePic,
                                price = price,
                                priceText = "₹${price.toInt()}",
                                timestamp = timestamp,
                                postTimestamp = timestamp,
                                postType = "product",
                                likesCount = 0,
                                commentsCount = 0
                            )
                            allPosts.add(productAsPost)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing product: ${e.message}", e)
                    }
                }
                
                productsLoaded = true
                emitCombinedData()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Products listener cancelled: ${error.message}")
                close(error.toException())
            }
        }
        
        // Attach both listeners
        postsRef.addValueEventListener(postsListener)
        productsRef.addValueEventListener(productsListener)
        
        Log.d(TAG, "Firebase real-time listeners attached for Posts and Products")
        
        awaitClose {
            Log.d(TAG, "Removing Firebase listeners")
            postsRef.removeEventListener(postsListener)
            productsRef.removeEventListener(productsListener)
        }
    }
    
    override suspend fun toggleLike(postId: String, userId: String): Result<Boolean> {
        return try {
            val likesRef = postsRef.child(postId).child("likes").child(userId)
            val snapshot = likesRef.get().await()
            
            if (snapshot.exists()) {
                // Unlike
                likesRef.removeValue().await()
            } else {
                // Like
                likesRef.setValue(true).await()
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun toggleSave(postId: String, userId: String): Result<Boolean> {
        return try {
            val savedRef = usersRef.child(userId).child("saved").child(postId)
            val snapshot = savedRef.get().await()
            
            if (snapshot.exists()) {
                // Unsave
                savedRef.removeValue().await()
            } else {
                // Save
                savedRef.setValue(true).await()
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun toggleFollow(targetUserId: String, currentUserId: String): Result<Boolean> {
        return try {
            val followingRef = usersRef.child(currentUserId).child("following").child(targetUserId)
            val followerRef = usersRef.child(targetUserId).child("followers").child(currentUserId)
            val snapshot = followingRef.get().await()
            
            if (snapshot.exists()) {
                // Unfollow
                followingRef.removeValue().await()
                followerRef.removeValue().await()
            } else {
                // Follow
                followingRef.setValue(true).await()
                followerRef.setValue(true).await()
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getPostLikesCount(postId: String): Flow<Int> = callbackFlow {
        val likesRef = postsRef.child(postId).child("likes")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.childrenCount.toInt())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        likesRef.addValueEventListener(listener)
        
        awaitClose {
            likesRef.removeEventListener(listener)
        }
    }
    
    override fun getPostCommentsCount(postId: String): Flow<Int> = callbackFlow {
        val commentsRef = database.getReference("Comments")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                snapshot.children.forEach { commentSnapshot ->
                    val commentPostId = commentSnapshot.child("postId").getValue(String::class.java)
                    if (commentPostId == postId) {
                        count++
                    }
                }
                trySend(count)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        commentsRef.addValueEventListener(listener)
        
        awaitClose {
            commentsRef.removeEventListener(listener)
        }
    }
    
    override fun isPostLiked(postId: String, userId: String): Flow<Boolean> = callbackFlow {
        val likesRef = postsRef.child(postId).child("likes").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        likesRef.addValueEventListener(listener)
        
        awaitClose {
            likesRef.removeEventListener(listener)
        }
    }
    
    override fun isPostSaved(postId: String, userId: String): Flow<Boolean> = callbackFlow {
        val savedRef = usersRef.child(userId).child("saved").child(postId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        savedRef.addValueEventListener(listener)
        
        awaitClose {
            savedRef.removeEventListener(listener)
        }
    }
    
    override fun isUserFollowing(targetUserId: String, currentUserId: String): Flow<Boolean> = callbackFlow {
        val followingRef = usersRef.child(currentUserId).child("following").child(targetUserId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        followingRef.addValueEventListener(listener)
        
        awaitClose {
            followingRef.removeEventListener(listener)
        }
    }
}

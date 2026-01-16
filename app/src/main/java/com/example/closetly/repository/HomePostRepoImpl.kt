package com.example.closetly.repository

import com.example.closetly.model.PostModel
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class HomePostRepoImpl : HomePostRepo {
    
    private val database = FirebaseDatabase.getInstance()
    private val postsRef = database.getReference("Posts")
    private val usersRef = database.getReference("Users")
    
    override fun getAllPostsRealTime(): Flow<List<PostModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<PostModel>()
                snapshot.children.forEach { postSnapshot ->
                    postSnapshot.getValue(PostModel::class.java)?.let { post ->
                        posts.add(post)
                    }
                }
                // Sort by timestamp descending (newest first)
                trySend(posts.sortedByDescending { it.timestamp })
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

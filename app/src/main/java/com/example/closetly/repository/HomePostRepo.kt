package com.example.closetly.repository

import com.example.closetly.model.PostModel
import kotlinx.coroutines.flow.Flow

interface HomePostRepo {
    fun getAllPostsRealTime(): Flow<List<PostModel>>
    
    // New methods for Instagram-like refresh with pagination
    fun getPostsFromFollowedUsers(
        userId: String, 
        afterTimestamp: Long? = null,
        limit: Int = 10
    ): Flow<List<PostModel>>
    
    fun getNewPostsOnly(
        userId: String,
        lastSeenTimestamp: Long
    ): Flow<List<PostModel>>
    
    suspend fun saveLastSeenTimestamp(userId: String, timestamp: Long)
    suspend fun getLastSeenTimestamp(userId: String): Long
    
    suspend fun toggleLike(postId: String, userId: String): Result<Boolean>
    suspend fun toggleSave(postId: String, userId: String): Result<Boolean>
    suspend fun toggleFollow(targetUserId: String, currentUserId: String): Result<Boolean>
    fun getPostLikesCount(postId: String): Flow<Int>
    fun getPostCommentsCount(postId: String): Flow<Int>
    fun isPostLiked(postId: String, userId: String): Flow<Boolean>
    fun isPostSaved(postId: String, userId: String): Flow<Boolean>
    fun isUserFollowing(targetUserId: String, currentUserId: String): Flow<Boolean>
    fun getSavedPosts(userId: String): Flow<List<PostModel>>
    fun getLikedPosts(userId: String): Flow<List<PostModel>>
}

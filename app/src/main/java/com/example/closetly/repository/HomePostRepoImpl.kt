package com.example.closetly.repository

import android.content.Context
import com.example.closetly.model.PostModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class HomePostRepoImpl(private val context: Context) : HomePostRepo {

    private val database = FirebaseDatabase.getInstance()
    private val postsRef = database.getReference("Posts")
    private val usersRef = database.getReference("Users")
    private val notificationRepo = NotificationRepoImpl()
    private val auth = FirebaseAuth.getInstance()

    override fun getAllPostsRealTime(): Flow<List<PostModel>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        var blockedUserIds = setOf<String>()
        
        // Listen for blocked users changes
        val blockedListener = if (currentUserId != null) {
            object : ValueEventListener {
                override fun onDataChange(blockedSnapshot: DataSnapshot) {
                    blockedUserIds = blockedSnapshot.children.mapNotNull { it.key }.toSet()
                    // After updating blocked list, fetch posts again to trigger filtering
                }
                override fun onCancelled(error: DatabaseError) {}
            }
        } else null
        
        // Attach blocked users listener
        if (currentUserId != null && blockedListener != null) {
            usersRef.child(currentUserId).child("blocked").addValueEventListener(blockedListener)
        }
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<PostModel>()
                snapshot.children.forEach { postSnapshot ->
                    postSnapshot.getValue(PostModel::class.java)?.let { post ->
                        // Filter out posts from blocked users
                        if (!blockedUserIds.contains(post.userId)) {
                            posts.add(post)
                        }
                    }
                }
                trySend(posts.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        postsRef.addValueEventListener(listener)

        awaitClose {
            postsRef.removeEventListener(listener)
            if (currentUserId != null && blockedListener != null) {
                usersRef.child(currentUserId).child("blocked").removeEventListener(blockedListener)
            }
        }
    }

    override suspend fun toggleLike(postId: String, userId: String): Result<Boolean> {
        return try {
            val likesRef = postsRef.child(postId).child("likes").child(userId)
            val snapshot = likesRef.get().await()

            if (snapshot.exists()) {
                likesRef.removeValue().await()
            } else {
                likesRef.setValue(true).await()

                val postSnapshot = postsRef.child(postId).get().await()
                val post = postSnapshot.getValue(PostModel::class.java)
                val userSnapshot = usersRef.child(userId).get().await()
                val user = userSnapshot.getValue(com.example.closetly.model.UserModel::class.java)

                if (post != null && user != null) {
                    notificationRepo.sendLikeNotification(
                        context = context,
                        senderId = userId,
                        senderName = user.username,
                        senderImage = user.profilePicture,
                        postOwnerId = post.userId,
                        postId = postId,
                        postImage = post.imageUrl
                    )
                }
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
                savedRef.removeValue().await()
            } else {
                savedRef.setValue(true).await()
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFollow(targetUserId: String, currentUserId: String): Result<Boolean> {
        return try {
            // Check if either user has blocked the other
            val currentUserBlockedSnapshot = usersRef.child(currentUserId).child("blocked").child(targetUserId).get().await()
            val targetUserBlockedSnapshot = usersRef.child(targetUserId).child("blocked").child(currentUserId).get().await()
            
            if (currentUserBlockedSnapshot.exists() || targetUserBlockedSnapshot.exists()) {
                // Either user has blocked the other - silently prevent follow action
                return Result.failure(Exception("Cannot follow this user"))
            }
            
            val followingRef = usersRef.child(currentUserId).child("following").child(targetUserId)
            val followerRef = usersRef.child(targetUserId).child("followers").child(currentUserId)
            val snapshot = followingRef.get().await()

            if (snapshot.exists()) {
                followingRef.removeValue().await()
                followerRef.removeValue().await()
            } else {
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

    override fun getSavedPosts(userId: String): Flow<List<PostModel>> = callbackFlow {
        val savedRef = usersRef.child(userId).child("saved")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val savedPostIds = mutableListOf<String>()
                
                // Get all saved post IDs
                snapshot.children.forEach { savedSnapshot ->
                    savedSnapshot.key?.let { postId ->
                        savedPostIds.add(postId)
                    }
                }
                
                // Fetch actual post data for each saved post ID
                if (savedPostIds.isEmpty()) {
                    trySend(emptyList())
                } else {
                    val savedPosts = mutableListOf<PostModel>()
                    var processedCount = 0
                    
                    savedPostIds.forEach { postId ->
                        postsRef.child(postId).get().addOnSuccessListener { postSnapshot ->
                            postSnapshot.getValue(PostModel::class.java)?.let { post ->
                                savedPosts.add(post)
                            }
                            processedCount++
                            
                            // When all posts are processed, send the result
                            if (processedCount == savedPostIds.size) {
                                // Sort by timestamp (newest first)
                                trySend(savedPosts.sortedByDescending { it.timestamp })
                            }
                        }.addOnFailureListener {
                            processedCount++
                            
                            // Still send result even if one post fails
                            if (processedCount == savedPostIds.size) {
                                trySend(savedPosts.sortedByDescending { it.timestamp })
                            }
                        }
                    }
                }
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

    override fun getLikedPosts(userId: String): Flow<List<PostModel>> = callbackFlow {
        val postsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likedPosts = mutableListOf<PostModel>()
                
                // Iterate through all posts and check if user liked them
                snapshot.children.forEach { postSnapshot ->
                    postSnapshot.getValue(PostModel::class.java)?.let { post ->
                        // Check if user liked this post
                        val isLiked = postSnapshot.child("likes").child(userId).exists()
                        if (isLiked) {
                            likedPosts.add(post)
                        }
                    }
                }
                
                // Sort by timestamp (newest first)
                trySend(likedPosts.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        postsRef.addValueEventListener(postsListener)

        awaitClose {
            postsRef.removeEventListener(postsListener)
        }
    }
    
    /**
     * Get posts from followed users with pagination support
     * Shows posts in descending order by timestamp (latest first)
     */
    override fun getPostsFromFollowedUsers(
        userId: String,
        afterTimestamp: Long?,
        limit: Int
    ): Flow<List<PostModel>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        var blockedUserIds = setOf<String>()
        
        // Listen for blocked users changes
        val blockedListener = if (currentUserId != null) {
            object : ValueEventListener {
                override fun onDataChange(blockedSnapshot: DataSnapshot) {
                    blockedUserIds = blockedSnapshot.children.mapNotNull { it.key }.toSet()
                }
                override fun onCancelled(error: DatabaseError) {}
            }
        } else null
        
        // Attach listeners
        if (currentUserId != null && blockedListener != null) {
            usersRef.child(currentUserId).child("blocked").addValueEventListener(blockedListener)
        }
        
        // Create query for posts ordered by timestamp - FETCH ALL POSTS
        val query = if (afterTimestamp != null) {
            postsRef.orderByChild("timestamp")
                .startAt(afterTimestamp.toDouble() + 1) // Exclusive of afterTimestamp
                .limitToFirst(limit)
        } else {
            postsRef.orderByChild("timestamp")
                .limitToLast(limit) // Get latest posts
        }
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<PostModel>()
                snapshot.children.forEach { postSnapshot ->
                    postSnapshot.getValue(PostModel::class.java)?.let { post ->
                        // Filter: only exclude blocked users - SHOW ALL OTHER POSTS
                        if (!blockedUserIds.contains(post.userId) &&
                            post.imageUrl.isNotEmpty() &&
                            post.username.isNotEmpty()) {
                            posts.add(post)
                        }
                    }
                }
                // Sort by timestamp descending (latest first)
                trySend(posts.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)

        awaitClose {
            query.removeEventListener(listener)
            if (currentUserId != null && blockedListener != null) {
                usersRef.child(currentUserId).child("blocked").removeEventListener(blockedListener)
            }
        }
    }
    
    /**
     * Get only new posts created after the last seen timestamp
     * Used for pull-to-refresh to show only fresh content
     */
    override fun getNewPostsOnly(
        userId: String,
        lastSeenTimestamp: Long
    ): Flow<List<PostModel>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        var blockedUserIds = setOf<String>()
        
        // Listen for blocked users
        val blockedListener = if (currentUserId != null) {
            object : ValueEventListener {
                override fun onDataChange(blockedSnapshot: DataSnapshot) {
                    blockedUserIds = blockedSnapshot.children.mapNotNull { it.key }.toSet()
                }
                override fun onCancelled(error: DatabaseError) {}
            }
        } else null
        
        // Attach listeners
        if (currentUserId != null && blockedListener != null) {
            usersRef.child(currentUserId).child("blocked").addValueEventListener(blockedListener)
        }
        
        // Query for posts created after lastSeenTimestamp - FETCH ALL NEW POSTS
        val query = postsRef.orderByChild("timestamp")
            .startAt((lastSeenTimestamp + 1).toDouble()) // Exclusive of lastSeenTimestamp
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newPosts = mutableListOf<PostModel>()
                snapshot.children.forEach { postSnapshot ->
                    postSnapshot.getValue(PostModel::class.java)?.let { post ->
                        // Only exclude blocked users - SHOW ALL OTHER NEW POSTS
                        if (!blockedUserIds.contains(post.userId) &&
                            post.timestamp > lastSeenTimestamp &&
                            post.imageUrl.isNotEmpty() &&
                            post.username.isNotEmpty()) {
                            newPosts.add(post)
                        }
                    }
                }
                // Sort by timestamp descending (latest first)
                trySend(newPosts.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)

        awaitClose {
            query.removeEventListener(listener)
            if (currentUserId != null && blockedListener != null) {
                usersRef.child(currentUserId).child("blocked").removeEventListener(blockedListener)
            }
        }
    }
    
    /**
     * Save the last seen timestamp for a user
     * This tracks when the user last refreshed their feed
     */
    override suspend fun saveLastSeenTimestamp(userId: String, timestamp: Long) {
        try {
            usersRef.child(userId).child("lastSeenTimestamp").setValue(timestamp).await()
        } catch (e: Exception) {
            // Silent fail - not critical
        }
    }
    
    /**
     * Get the last seen timestamp for a user
     * Returns 0 if not found (first time user)
     */
    override suspend fun getLastSeenTimestamp(userId: String): Long {
        return try {
            val snapshot = usersRef.child(userId).child("lastSeenTimestamp").get().await()
            snapshot.getValue(Long::class.java) ?: 0L
        } catch (e: Exception) {
            0L // Default to epoch start if error
        }
    }
}

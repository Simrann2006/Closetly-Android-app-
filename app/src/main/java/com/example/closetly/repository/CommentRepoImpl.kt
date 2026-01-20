package com.example.closetly.repository

import com.example.closetly.model.CommentModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CommentRepoImpl : CommentRepo {

    private val database = FirebaseDatabase.getInstance()
    private val commentsRef = database.getReference("Comments")

    private val postsRef = database.getReference("Posts")
    private val usersRef = database.getReference("Users")
    private val notificationRepo = NotificationRepoImpl()

    override fun getComments(postId: String): Flow<List<CommentModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentsList = mutableListOf<CommentModel>()
                snapshot.children.forEach { commentSnapshot ->
                    try {
                        val id = commentSnapshot.child("id").getValue(String::class.java) ?: ""
                        val commentPostId = commentSnapshot.child("postId").getValue(String::class.java) ?: ""
                        val userId = commentSnapshot.child("userId").getValue(String::class.java) ?: ""
                        val userName = commentSnapshot.child("userName").getValue(String::class.java) ?: ""
                        val userProfileImage = commentSnapshot.child("userProfileImage").getValue(String::class.java) ?: ""
                        val commentText = commentSnapshot.child("commentText").getValue(String::class.java) ?: ""
                        val timestamp = commentSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                        
                        val likes = mutableMapOf<String, Boolean>()
                        commentSnapshot.child("likes").children.forEach { likeSnapshot ->
                            likeSnapshot.key?.let { likeUserId ->
                                if (likeSnapshot.getValue(Boolean::class.java) == true) {
                                    likes[likeUserId] = true
                                }
                            }
                        }
                        
                        if (commentPostId == postId) {
                            val comment = CommentModel(
                                id = id,
                                postId = commentPostId,
                                userId = userId,
                                userName = userName,
                                userProfileImage = userProfileImage,
                                commentText = commentText,
                                timestamp = timestamp,
                                likes = likes
                            )
                            commentsList.add(comment)
                        }
                    } catch (e: Exception) {
                    }
                }
                trySend(commentsList.sortedByDescending { it.timestamp })
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

    override suspend fun addComment(comment: CommentModel): Result<CommentModel> {
        return try {
            commentsRef.child(comment.id).setValue(comment).await()

            val postSnapshot = postsRef.child(comment.postId).get().await()
            val post = postSnapshot.getValue(com.example.closetly.model.PostModel::class.java)
            val userSnapshot = usersRef.child(comment.userId).get().await()
            val user = userSnapshot.getValue(com.example.closetly.model.UserModel::class.java)

            if (post != null && user != null) {
                notificationRepo.sendCommentNotification(
                    senderId = comment.userId,
                    senderName = user.username,
                    senderImage = user.profilePicture,
                    postOwnerId = post.userId,
                    postId = comment.postId,
                    postImage = post.imageUrl,
                    commentText = comment.commentText
                )
            }

            Result.success(comment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun likeComment(commentId: String, userId: String): Result<Boolean> {
        return try {
            val likesRef = commentsRef.child(commentId).child("likes").child(userId)
            val snapshot = likesRef.get().await()
            
            if (snapshot.exists()) {
                likesRef.removeValue().await()
            } else {
                likesRef.setValue(true).await()
            }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteComment(commentId: String): Result<Boolean> {
        return try {
            commentsRef.child(commentId).removeValue().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

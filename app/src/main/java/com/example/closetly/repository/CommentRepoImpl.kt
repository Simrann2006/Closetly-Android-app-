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

    override fun getComments(postId: String): Flow<List<CommentModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentsList = mutableListOf<CommentModel>()
                snapshot.children.forEach { commentSnapshot ->
                    commentSnapshot.getValue(CommentModel::class.java)?.let { comment ->
                        if (comment.postId == postId) {
                            commentsList.add(comment)
                        }
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
            Result.success(comment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun likeComment(commentId: String): Result<Boolean> {
        return try {
            val commentRef = commentsRef.child(commentId)
            val snapshot = commentRef.get().await()
            val comment = snapshot.getValue(CommentModel::class.java)
            
            comment?.let {
                val updatedComment = it.copy(
                    isLiked = !it.isLiked,
                    likesCount = if (it.isLiked) it.likesCount - 1 else it.likesCount + 1
                )
                commentRef.setValue(updatedComment).await()
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

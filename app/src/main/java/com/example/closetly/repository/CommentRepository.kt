package com.example.closetly.repository

import com.example.closetly.data.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun getComments(postId: String): Flow<List<Comment>>
    suspend fun addComment(comment: Comment): Result<Comment>
    suspend fun likeComment(commentId: String): Result<Boolean>
}
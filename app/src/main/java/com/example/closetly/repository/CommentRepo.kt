package com.example.closetly.repository

import com.example.closetly.model.CommentModel
import kotlinx.coroutines.flow.Flow

interface CommentRepo {
    fun getComments(postId: String): Flow<List<CommentModel>>
    suspend fun addComment(comment: CommentModel): Result<CommentModel>
    suspend fun likeComment(commentId: String): Result<Boolean>
    suspend fun deleteComment(commentId: String): Result<Boolean>
}
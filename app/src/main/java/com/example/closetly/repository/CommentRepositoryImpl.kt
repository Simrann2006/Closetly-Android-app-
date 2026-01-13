package com.example.closetly.repository

import com.example.closetly.data.model.Comment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CommentRepositoryImpl : CommentRepository {

    private val comments = mutableListOf<Comment>()

    override fun getComments(postId: String): Flow<List<Comment>> = flow {
        delay(500)
        emit(comments.filter { it.postId == postId }.sortedByDescending { it.timestamp })
    }

    override suspend fun addComment(comment: Comment): Result<Comment> {
        return try {
            delay(300)
            comments.add(comment)
            Result.success(comment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun likeComment(commentId: String): Result<Boolean> {
        return try {
            delay(200)
            val comment = comments.find { it.id == commentId }
            comment?.let {
                val index = comments.indexOf(it)
                comments[index] = it.copy(
                    isLiked = !it.isLiked,
                    likesCount = if (it.isLiked) it.likesCount - 1 else it.likesCount + 1
                )
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteComment(commentId: String): Result<Boolean> {
        return try {
            delay(200)
            val removed = comments.removeIf { it.id == commentId }
            Result.success(removed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.example.closetly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.closetly.data.model.Comment
import com.example.closetly.repository.CommentRepository
import com.example.closetly.repository.CommentRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CommentViewModel(
    private val repository: CommentRepository = CommentRepositoryImpl()
) : ViewModel() {
    
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()
    
    fun loadComments(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getComments(postId).collect { commentsList ->
                _comments.value = commentsList
                _isLoading.value = false
            }
        }
    }
    
    fun updateCommentText(text: String) {
        _commentText.value = text
    }
    
    fun postComment(postId: String, userName: String, userProfileImage: String) {
        if (_commentText.value.isBlank()) return
        
        viewModelScope.launch {
            val comment = Comment(
                id = UUID.randomUUID().toString(),
                postId = postId,
                userId = "current_user_id",
                userName = userName,
                userProfileImage = userProfileImage,
                commentText = _commentText.value,
                timestamp = System.currentTimeMillis()
            )
            
            repository.addComment(comment).onSuccess {
                _commentText.value = ""
                loadComments(postId)
            }
        }
    }
    
    fun likeComment(commentId: String, postId: String) {
        viewModelScope.launch {
            repository.likeComment(commentId).onSuccess {
                loadComments(postId)
            }
        }
    }
}

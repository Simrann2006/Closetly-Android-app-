package com.example.closetly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.closetly.model.CommentModel
import com.example.closetly.repository.CommentRepo
import com.example.closetly.repository.CommentRepoImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CommentViewModel(
    private val repository: CommentRepo = CommentRepoImpl()
) : ViewModel() {
    
    private val _comments = MutableStateFlow<List<CommentModel>>(emptyList())
    val comments: StateFlow<List<CommentModel>> = _comments.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()
    
    private val _showDeleteDialog = MutableStateFlow<String?>(null)
    val showDeleteDialog: StateFlow<String?> = _showDeleteDialog.asStateFlow()
    
    private val database = FirebaseDatabase.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
    
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
    
    fun postComment(postId: String, userName: String = "", userProfileImage: String = "") {
        if (_commentText.value.isBlank()) return
        
        viewModelScope.launch {
            // Get current user data from Firebase
            var finalUserName = userName
            var finalUserProfileImage = userProfileImage
            
            if (finalUserName.isEmpty()) {
                try {
                    val userSnapshot = database.getReference("Users/$currentUserId").get().await()
                    finalUserName = userSnapshot.child("fullName").value?.toString() ?: "Anonymous"
                    finalUserProfileImage = userSnapshot.child("profilePicture").value?.toString() ?: ""
                } catch (e: Exception) {
                    finalUserName = "Anonymous"
                }
            }
            
            val comment = CommentModel(
                id = UUID.randomUUID().toString(),
                postId = postId,
                userId = currentUserId,
                userName = finalUserName,
                userProfileImage = finalUserProfileImage,
                commentText = _commentText.value,
                timestamp = System.currentTimeMillis()
            )
            
            repository.addComment(comment).onSuccess {
                _commentText.value = ""
            }
        }
    }
    
    fun likeComment(commentId: String, postId: String) {
        viewModelScope.launch {
            // Real-time Firebase listener will automatically update the UI
            repository.likeComment(commentId, currentUserId)
        }
    }
    
    fun showDeleteConfirmation(commentId: String) {
        _showDeleteDialog.value = commentId
    }
    
    fun dismissDeleteDialog() {
        _showDeleteDialog.value = null
    }
    
    fun deleteComment(commentId: String, postId: String) {
        viewModelScope.launch {
            // Optimistically remove from UI immediately
            _comments.value = _comments.value.filter { it.id != commentId }
            
            // Update repository in background
            repository.deleteComment(commentId).onSuccess {
                _showDeleteDialog.value = null
            }
        }
    }
    
    fun isCurrentUserComment(userId: String): Boolean {
        return userId == currentUserId
    }
    
    fun getCurrentUserId(): String {
        return currentUserId
    }
}


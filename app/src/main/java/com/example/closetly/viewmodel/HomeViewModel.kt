package com.example.closetly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.closetly.model.PostModel
import com.example.closetly.repository.HomePostRepo
import com.example.closetly.repository.HomePostRepoImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * PostUI wraps a PostModel with its associated UI states (like, save, follow, counts)
 * This allows each post to have independent state management
 */
data class PostUI(
    val post: PostModel,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val isFollowing: Boolean = false,
    val likesCount: Int = 0,
    val commentsCount: Int = 0
)

class HomeViewModel(
    private val repository: HomePostRepo = HomePostRepoImpl()
) : ViewModel() {
    
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
    
    // Main state for all posts with their UI states
    private val _postsUI = MutableStateFlow<List<PostUI>>(emptyList())
    val postsUI: StateFlow<List<PostUI>> = _postsUI.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadPosts()
    }
    
    /**
     * Load all posts with real-time listeners
     * For each post, we also subscribe to its likes count, comments count, and user states
     */
    fun loadPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.getAllPostsRealTime()
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collectLatest { posts ->
                    _isLoading.value = false
                    
                    // For each post, combine it with its real-time states
                    val postsWithStates = posts.map { post ->
                        createPostUIFlow(post)
                    }
                    
                    // Combine all post flows into one
                    combine(postsWithStates) { postsArray ->
                        postsArray.toList()
                    }.collect { combinedPosts ->
                        _postsUI.value = combinedPosts
                    }
                }
        }
    }
    
    /**
     * Create a Flow that combines a post with all its real-time states
     */
    private fun createPostUIFlow(post: PostModel): Flow<PostUI> {
        return combine(
            repository.isPostLiked(post.postId, currentUserId),
            repository.isPostSaved(post.postId, currentUserId),
            repository.isUserFollowing(post.userId, currentUserId),
            repository.getPostLikesCount(post.postId),
            repository.getPostCommentsCount(post.postId)
        ) { isLiked, isSaved, isFollowing, likesCount, commentsCount ->
            PostUI(
                post = post,
                isLiked = isLiked,
                isSaved = isSaved,
                isFollowing = isFollowing,
                likesCount = likesCount,
                commentsCount = commentsCount
            )
        }
    }
    
    /**
     * Toggle like for a specific post
     * UI will update automatically through the real-time listeners
     */
    fun toggleLike(postId: String) {
        viewModelScope.launch {
            repository.toggleLike(postId, currentUserId)
                .onFailure { e ->
                    _error.value = "Failed to like post: ${e.message}"
                }
        }
    }
    
    /**
     * Toggle save for a specific post
     * UI will update automatically through the real-time listeners
     */
    fun toggleSave(postId: String) {
        viewModelScope.launch {
            repository.toggleSave(postId, currentUserId)
                .onFailure { e ->
                    _error.value = "Failed to save post: ${e.message}"
                }
        }
    }
    
    /**
     * Toggle follow for a specific user
     * UI will update automatically through the real-time listeners
     */
    fun toggleFollow(targetUserId: String) {
        viewModelScope.launch {
            repository.toggleFollow(targetUserId, currentUserId)
                .onFailure { e ->
                    _error.value = "Failed to follow user: ${e.message}"
                }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}

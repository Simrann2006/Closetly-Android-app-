package com.example.closetly.viewmodel

import android.content.Context
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
    context: Context,
    private val repository: HomePostRepo = HomePostRepoImpl(context)
) : ViewModel() {
    
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
    
    // Main state for all posts with their UI states
    private val _postsUI = MutableStateFlow<List<PostUI>>(emptyList())
    val postsUI: StateFlow<List<PostUI>> = _postsUI.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _lastSeenTimestamp = MutableStateFlow(0L)
    val lastSeenTimestamp: StateFlow<Long> = _lastSeenTimestamp.asStateFlow()
    
    // Track oldest post timestamp for pagination
    private var oldestPostTimestamp: Long? = null
    private val PAGE_SIZE = 10
    
    init {
        loadInitialPosts()
    }
    
    /**
     * Load initial posts (from followed users + real-time updates)
     * This is called on first load
     */
    private fun loadInitialPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Get last seen timestamp
            _lastSeenTimestamp.value = repository.getLastSeenTimestamp(currentUserId)
            
            // Load posts from followed users
            repository.getPostsFromFollowedUsers(
                userId = currentUserId,
                afterTimestamp = null,
                limit = PAGE_SIZE
            )
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collectLatest { posts ->
                    val filteredPosts = posts.filter { post ->
                        post.userId.isNotEmpty() &&
                        post.imageUrl.isNotEmpty() &&
                        post.username.isNotEmpty()
                    }
                    _isLoading.value = false
                    
                    // Track oldest post for pagination
                    if (filteredPosts.isNotEmpty()) {
                        oldestPostTimestamp = filteredPosts.minOfOrNull { it.timestamp }
                    }
                    
                    // For each post, combine it with its real-time states
                    val postsWithStates = filteredPosts.map { post ->
                        createPostUIFlow(post)
                    }
                    
                    // Combine all post flows into one
                    if (postsWithStates.isNotEmpty()) {
                        combine(postsWithStates) { postsArray ->
                            postsArray.toList()
                        }.collect { combinedPosts ->
                            _postsUI.value = combinedPosts.sortedByDescending { it.post.timestamp }
                        }
                    } else {
                        _postsUI.value = emptyList()
                    }
                }
        }
    }
    
    /**
     * Pull-to-refresh: Load only NEW posts created after last seen timestamp
     * This prevents old posts from reappearing
     */
    fun refreshPosts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            
            try {
                val lastSeen = _lastSeenTimestamp.value
                val currentTime = System.currentTimeMillis()
                
                // Get only new posts
                repository.getNewPostsOnly(currentUserId, lastSeen)
                    .catch { e ->
                        _error.value = "Refresh failed: ${e.message}"
                        _isRefreshing.value = false
                    }
                    .collect { newPosts ->
                        if (newPosts.isNotEmpty()) {
                            // Add new posts to existing list
                            val currentPosts = _postsUI.value.map { it.post }
                            val allPosts = (newPosts + currentPosts).distinctBy { it.postId }
                            
                            // Create UI flows for new posts
                            val postsWithStates = allPosts.map { post ->
                                createPostUIFlow(post)
                            }
                            
                            if (postsWithStates.isNotEmpty()) {
                                combine(postsWithStates) { postsArray ->
                                    postsArray.toList()
                                }.collect { combinedPosts ->
                                    _postsUI.value = combinedPosts.sortedByDescending { it.post.timestamp }
                                }
                            }
                            
                            // Update last seen timestamp
                            _lastSeenTimestamp.value = currentTime
                            repository.saveLastSeenTimestamp(currentUserId, currentTime)
                        }
                        
                        _isRefreshing.value = false
                    }
            } catch (e: Exception) {
                _error.value = "Refresh failed: ${e.message}"
                _isRefreshing.value = false
            }
        }
    }
    
    /**
     * Load more posts (pagination)
     * Called when user scrolls to bottom
     */
    fun loadMorePosts() {
        if (_isLoadingMore.value || oldestPostTimestamp == null) return
        
        viewModelScope.launch {
            _isLoadingMore.value = true
            
            try {
                repository.getPostsFromFollowedUsers(
                    userId = currentUserId,
                    afterTimestamp = oldestPostTimestamp,
                    limit = PAGE_SIZE
                )
                    .catch { e ->
                        _error.value = "Load more failed: ${e.message}"
                        _isLoadingMore.value = false
                    }
                    .collect { morePosts ->
                        if (morePosts.isNotEmpty()) {
                            // Update oldest timestamp
                            oldestPostTimestamp = morePosts.minOfOrNull { it.timestamp }
                            
                            // Add to existing posts
                            val currentPosts = _postsUI.value.map { it.post }
                            val allPosts = (currentPosts + morePosts).distinctBy { it.postId }
                            
                            // Create UI flows
                            val postsWithStates = allPosts.map { post ->
                                createPostUIFlow(post)
                            }
                            
                            if (postsWithStates.isNotEmpty()) {
                                combine(postsWithStates) { postsArray ->
                                    postsArray.toList()
                                }.collect { combinedPosts ->
                                    _postsUI.value = combinedPosts.sortedByDescending { it.post.timestamp }
                                }
                            }
                        }
                        
                        _isLoadingMore.value = false
                    }
            } catch (e: Exception) {
                _error.value = "Load more failed: ${e.message}"
                _isLoadingMore.value = false
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

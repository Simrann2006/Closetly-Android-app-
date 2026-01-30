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
    val commentsCount: Int = 0,
    val isFromFollowedUser: Boolean = false // Track if post is from followed user
)

class HomeViewModel(
    context: Context,
    private val repository: HomePostRepo = HomePostRepoImpl(context)
) : ViewModel() {
    
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
    
    // Main state for all posts with their UI states
    private val _postsUI = MutableStateFlow<List<PostUI>>(emptyList())
    val postsUI: StateFlow<List<PostUI>> = _postsUI.asStateFlow()
    
    // Track followed user IDs for priority sorting
    private val _followedUserIds = MutableStateFlow<Set<String>>(emptySet())
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Track loaded post IDs to avoid duplicates
    private val loadedPostIds = mutableSetOf<String>()
    
    private val PAGE_SIZE = 20
    
    init {
        loadFollowedUsers()
        loadPriorityFeed()
    }
    
    /**
     * Load followed user IDs for priority sorting
     */
    private fun loadFollowedUsers() {
        viewModelScope.launch {
            repository.getFollowedUserIds(currentUserId)
                .catch { /* Silent fail */ }
                .collect { followedIds ->
                    _followedUserIds.value = followedIds
                }
        }
    }
    
    /**
     * Load posts with priority: followed users first, then others
     * Both sorted by newest to oldest within their groups
     */
    private fun loadPriorityFeed() {
        viewModelScope.launch {
            _isLoading.value = true
            loadedPostIds.clear()
            
            repository.getPriorityFeedPosts(
                userId = currentUserId,
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
                        post.username.isNotEmpty() &&
                        post.userId != currentUserId // Exclude own posts
                    }
                    
                    _isLoading.value = false
                    
                    // Track loaded post IDs
                    loadedPostIds.clear()
                    loadedPostIds.addAll(filteredPosts.map { it.postId })
                    
                    // Create UI flows for each post
                    val postsWithStates = filteredPosts.map { post ->
                        createPostUIFlow(post)
                    }
                    
                    if (postsWithStates.isNotEmpty()) {
                        combine(postsWithStates) { postsArray ->
                            postsArray.toList()
                        }.collect { combinedPosts ->
                            // Sort: followed users first (newest to oldest), then others (newest to oldest)
                            val followedIds = _followedUserIds.value
                            val sortedPosts = combinedPosts
                                .map { it.copy(isFromFollowedUser = followedIds.contains(it.post.userId)) }
                                .sortedWith(
                                    compareByDescending<PostUI> { it.isFromFollowedUser }
                                        .thenByDescending { it.post.timestamp }
                                )
                            _postsUI.value = sortedPosts
                        }
                    } else {
                        _postsUI.value = emptyList()
                    }
                }
        }
    }
    
    /**
     * Pull-to-refresh: Reload fresh data without duplicates
     */
    fun refreshPosts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadedPostIds.clear()
            
            try {
                repository.getPriorityFeedPosts(
                    userId = currentUserId,
                    limit = PAGE_SIZE
                )
                    .catch { e ->
                        _error.value = "Refresh failed: ${e.message}"
                        _isRefreshing.value = false
                    }
                    .take(1) // Take only one emission for refresh
                    .collect { posts ->
                        val filteredPosts = posts.filter { post ->
                            post.userId.isNotEmpty() &&
                            post.imageUrl.isNotEmpty() &&
                            post.username.isNotEmpty() &&
                            post.userId != currentUserId
                        }
                        
                        // Track new post IDs
                        loadedPostIds.clear()
                        loadedPostIds.addAll(filteredPosts.map { it.postId })
                        
                        // Create UI flows for new posts
                        val postsWithStates = filteredPosts.map { post ->
                            createPostUIFlow(post)
                        }
                        
                        if (postsWithStates.isNotEmpty()) {
                            combine(postsWithStates) { postsArray ->
                                postsArray.toList()
                            }.take(1).collect { combinedPosts ->
                                val followedIds = _followedUserIds.value
                                val sortedPosts = combinedPosts
                                    .map { it.copy(isFromFollowedUser = followedIds.contains(it.post.userId)) }
                                    .sortedWith(
                                        compareByDescending<PostUI> { it.isFromFollowedUser }
                                            .thenByDescending { it.post.timestamp }
                                    )
                                _postsUI.value = sortedPosts
                            }
                        } else {
                            _postsUI.value = emptyList()
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
        if (_isLoadingMore.value) return
        
        viewModelScope.launch {
            _isLoadingMore.value = true
            
            try {
                // Get more posts
                repository.getPriorityFeedPosts(
                    userId = currentUserId,
                    limit = PAGE_SIZE * 2 // Get more to filter out already loaded
                )
                    .catch { e ->
                        _error.value = "Load more failed: ${e.message}"
                        _isLoadingMore.value = false
                    }
                    .take(1)
                    .collect { allPosts ->
                        // Filter out already loaded posts
                        val newPosts = allPosts.filter { post ->
                            !loadedPostIds.contains(post.postId) &&
                            post.userId.isNotEmpty() &&
                            post.imageUrl.isNotEmpty() &&
                            post.username.isNotEmpty() &&
                            post.userId != currentUserId
                        }.take(PAGE_SIZE)
                        
                        if (newPosts.isNotEmpty()) {
                            // Add to loaded IDs
                            loadedPostIds.addAll(newPosts.map { it.postId })
                            
                            // Combine with existing posts
                            val currentPosts = _postsUI.value.map { it.post }
                            val allUniquePosts = (currentPosts + newPosts).distinctBy { it.postId }
                            
                            // Create UI flows
                            val postsWithStates = allUniquePosts.map { post ->
                                createPostUIFlow(post)
                            }
                            
                            if (postsWithStates.isNotEmpty()) {
                                combine(postsWithStates) { postsArray ->
                                    postsArray.toList()
                                }.take(1).collect { combinedPosts ->
                                    val followedIds = _followedUserIds.value
                                    val sortedPosts = combinedPosts
                                        .map { it.copy(isFromFollowedUser = followedIds.contains(it.post.userId)) }
                                        .sortedWith(
                                            compareByDescending<PostUI> { it.isFromFollowedUser }
                                                .thenByDescending { it.post.timestamp }
                                        )
                                    _postsUI.value = sortedPosts
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

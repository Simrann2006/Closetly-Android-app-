package com.example.closetly.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.closetly.model.SliderItemModel
import com.example.closetly.repository.SliderRepo
import com.example.closetly.repository.SliderRepoImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for managing slider items on the Home Screen.
 * Provides real-time data from Firebase using StateFlow.
 * 
 * This ViewModel:
 * - Shows ONLY posts from followed users (max 5 items)
 * - Automatically updates UI when followed users upload new posts
 * - Shows newest posts first based on timestamp
 * - Handles loading states
 * - Follows clean MVVM architecture
 */
class SliderViewModel(
    private val repository: SliderRepo = SliderRepoImpl()
) : ViewModel() {
    
    companion object {
        private const val TAG = "SliderViewModel"
        private const val MAX_SLIDER_ITEMS = 5
    }
    
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    
    // StateFlow for slider items - UI observes this for automatic updates
    private val _sliderItems = MutableStateFlow<List<SliderItemModel>>(emptyList())
    val sliderItems: StateFlow<List<SliderItemModel>> = _sliderItems.asStateFlow()
    
    // Loading state for UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        Log.d(TAG, "SliderViewModel initialized - loading followed users' posts")
        loadFollowedUsersSlider()
    }
    
    /**
     * Loads slider items ONLY from followed users.
     * Limited to 5 items, sorted by newest first.
     * Real-time updates when followed users post new content.
     */
    private fun loadFollowedUsersSlider() {
        if (currentUserId == null) {
            Log.e(TAG, "No current user - cannot load followed users slider")
            _sliderItems.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getFollowedUsersSliderItems(
                currentUserId = currentUserId,
                limit = MAX_SLIDER_ITEMS
            )
                .catch { e ->
                    Log.e(TAG, "Error loading followed users slider: ${e.message}", e)
                    _error.value = "Failed to load slider: ${e.message}"
                    _isLoading.value = false
                }
                .collectLatest { items ->
                    Log.d(TAG, "Received ${items.size} slider items from followed users")
                    _sliderItems.value = items.take(MAX_SLIDER_ITEMS)
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Manually refresh slider items
     * Reloads posts from followed users
     */
    fun refresh() {
        Log.d(TAG, "Manual refresh requested for followed users slider")
        loadFollowedUsersSlider()
    }
}

package com.example.closetly.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.closetly.model.SliderItemModel
import com.example.closetly.repository.SliderRepo
import com.example.closetly.repository.SliderRepoImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for managing slider items on the Home Screen.
 * Provides real-time data from Firebase using StateFlow.
 * 
 * This ViewModel:
 * - Observes Firebase data in real-time (latest 5 users only)
 * - Groups all posts (thrift + rent) by user in one slider
 * - Automatically updates UI when ANY user uploads a new post
 * - Shows newest users first based on their latest post
 * - Handles loading states
 * - Follows clean MVVM architecture
 */
class SliderViewModel(
    private val repository: SliderRepo = SliderRepoImpl()
) : ViewModel() {
    
    companion object {
        private const val TAG = "SliderViewModel"
    }
    
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
        Log.d(TAG, "SliderViewModel initialized - starting to load slider items")
        loadSliderItems()
    }
    
    /**
     * Loads slider items from Firebase in real-time (LATEST 5 USERS ONLY).
     * Uses Flow to automatically receive updates when data changes.
     * Groups all user posts (thrift + rent) together in one slider per user.
     * Shows up to 5 items per user. Newest users appear first.
     */
    private fun loadSliderItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getSliderItems()
                .catch { e ->
                    Log.e(TAG, "Error loading slider items: ${e.message}", e)
                    _error.value = "Failed to load slider: ${e.message}"
                    _isLoading.value = false
                }
                .collectLatest { items ->
                    Log.d(TAG, "Received ${items.size} slider items from repository")
                    
                    // Inject placeholder when empty so slider always has at least 1 item
                    _sliderItems.value = if (items.isEmpty()) {
                        Log.w(TAG, "No slider items available - injecting placeholder")
                        listOf(
                            SliderItemModel(
                                userId = "PLACEHOLDER_EMPTY",
                                username = "",
                                profilePictureUrl = "",
                                listings = emptyList(),
                                totalListings = 0,
                                lastUpdated = 0L
                            )
                        )
                    } else {
                        items
                    }
                    
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Manually refresh slider items (optional - usually not needed due to real-time updates)
     */
    fun refresh() {
        Log.d(TAG, "Manual refresh requested")
        loadSliderItems()
    }
}

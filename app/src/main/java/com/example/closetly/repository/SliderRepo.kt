package com.example.closetly.repository

import com.example.closetly.model.SliderItemModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for slider items.
 * Provides real-time updates from Firebase for the auto-carousel.
 */
interface SliderRepo {
    /**
     * Get slider items as a Flow for real-time updates.
     * @param excludeUserId Optional userId to exclude from slider (typically current user)
     * @return Flow emitting list of SliderItemModel whenever data changes in Firebase
     */
    fun getSliderItems(excludeUserId: String? = null): Flow<List<SliderItemModel>>
    
    /**
     * Get slider items ONLY from followed users.
     * Shows posts from users that the current user follows.
     * @param currentUserId The current user's ID (to get their following list)
     * @param limit Maximum number of slider items to return (default 5)
     * @return Flow emitting list of SliderItemModel from followed users only
     */
    fun getFollowedUsersSliderItems(
        currentUserId: String,
        limit: Int = 5
    ): Flow<List<SliderItemModel>>
}

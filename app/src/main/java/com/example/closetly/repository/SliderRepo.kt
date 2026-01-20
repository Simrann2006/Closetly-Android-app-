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
}

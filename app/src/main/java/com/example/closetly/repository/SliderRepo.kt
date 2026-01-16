package com.example.closetly.repository

import com.example.closetly.model.SliderItem
import kotlinx.coroutines.flow.Flow

interface SliderRepo {
    fun getSliderItems(): Flow<List<SliderItem>>
}

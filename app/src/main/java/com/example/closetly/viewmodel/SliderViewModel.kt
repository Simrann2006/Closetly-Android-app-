package com.example.closetly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.closetly.model.SliderItem
import com.example.closetly.repository.SliderRepo
import com.example.closetly.repository.SliderRepoImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SliderViewModel(
    private val repository: SliderRepo = SliderRepoImpl()
) : ViewModel() {
    
    private val _sliderItems = MutableStateFlow<List<SliderItem>>(emptyList())
    val sliderItems: StateFlow<List<SliderItem>> = _sliderItems.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadSliderItems()
    }
    
    private fun loadSliderItems() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getSliderItems()
                .catch { e ->
                    e.printStackTrace()
                    _isLoading.value = false
                }
                .collectLatest { items ->
                    _sliderItems.value = items
                    _isLoading.value = false
                }
        }
    }
}

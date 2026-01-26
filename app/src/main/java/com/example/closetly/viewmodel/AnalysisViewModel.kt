package com.example.closetly.viewmodel

import androidx.lifecycle.ViewModel
import com.example.closetly.model.ActivityStats
import com.example.closetly.model.ClosetCategory
import com.example.closetly.model.ClothesModel
import com.example.closetly.model.UnderusedItem
import com.example.closetly.model.WornColor
import com.example.closetly.repository.AnalysisRepo

class AnalysisViewModel(val repo: AnalysisRepo) : ViewModel() {
    
    fun calculateActivityStats(clothesList: List<ClothesModel>): ActivityStats {
        return repo.calculateActivityStats(clothesList)
    }
    
    fun calculateClosetBreakdown(clothesList: List<ClothesModel>): List<ClosetCategory> {
        return repo.calculateClosetBreakdown(clothesList)
    }
    
    fun calculateMostWornColors(clothesList: List<ClothesModel>): List<WornColor> {
        return repo.calculateMostWornColors(clothesList)
    }
    
    fun findUnderusedItem(clothesList: List<ClothesModel>): UnderusedItem? {
        return repo.findUnderusedItem(clothesList)
    }
}

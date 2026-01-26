package com.example.closetly.repository

import com.example.closetly.model.ActivityStats
import com.example.closetly.model.ClosetCategory
import com.example.closetly.model.ClothesModel
import com.example.closetly.model.UnderusedItem
import com.example.closetly.model.WornColor

interface AnalysisRepo {
    fun calculateActivityStats(clothesList: List<ClothesModel>): ActivityStats
    
    fun calculateClosetBreakdown(clothesList: List<ClothesModel>): List<ClosetCategory>
    
    fun calculateMostWornColors(clothesList: List<ClothesModel>): List<WornColor>
    
    fun findUnderusedItem(clothesList: List<ClothesModel>): UnderusedItem?
}

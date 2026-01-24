package com.example.closetly.utils

import androidx.compose.ui.graphics.Color
import com.example.closetly.model.ActivityStats
import com.example.closetly.model.ClosetCategory
import com.example.closetly.model.ClothesModel
import com.example.closetly.model.UnderusedItem
import com.example.closetly.model.WornColor

object AnalysisUtils {
    
    fun calculateActivityStats(clothesList: List<ClothesModel>): ActivityStats {
        val totalItems = clothesList.size
        val totalOutfits = 0 // This can be calculated based on outfit combinations if you have that data
        val totalWearCount = clothesList.sumOf { it.wearCount }
        
        return ActivityStats(
            items = totalItems,
            outfits = totalOutfits,
            reuse = totalWearCount
        )
    }
    
    fun calculateClosetBreakdown(clothesList: List<ClothesModel>): List<ClosetCategory> {
        if (clothesList.isEmpty()) return emptyList()
        
        val categoryGroups = clothesList.groupBy { it.categoryName }
        val totalItems = clothesList.size.toFloat()
        
        val colors = listOf(
            Color(0xFFE8B4BC),
            Color(0xFFD4A5AE),
            Color(0xFFC096A0),
            Color(0xFFAC8792),
            Color(0xFF987884),
            Color(0xFFB4A5BC),
            Color(0xFFA596A0)
        )
        
        return categoryGroups.entries.mapIndexed { index, entry ->
            val percentage = (entry.value.size / totalItems) * 100f
            ClosetCategory(
                name = entry.key.ifEmpty { "Uncategorized" },
                percentage = percentage,
                color = colors[index % colors.size]
            )
        }.sortedByDescending { it.percentage }
    }
    
    fun calculateMostWornColors(clothesList: List<ClothesModel>): List<WornColor> {
        if (clothesList.isEmpty()) return emptyList()
        
        val colorCounts = mutableMapOf<String, Int>()
        
        clothesList.forEach { clothes ->
            if (clothes.color.isNotBlank()) {
                // Split by comma in case multiple colors are stored
                clothes.color.split(",").forEach { color ->
                    val trimmedColor = color.trim()
                    if (trimmedColor.isNotEmpty()) {
                        colorCounts[trimmedColor] = colorCounts.getOrDefault(trimmedColor, 0) + clothes.wearCount + 1
                    }
                }
            }
        }
        
        return colorCounts.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { (colorName, _) ->
                WornColor(
                    name = colorName,
                    color = getColorFromName(colorName)
                )
            }
    }
    
    fun findUnderusedItem(clothesList: List<ClothesModel>): UnderusedItem? {
        if (clothesList.isEmpty()) return null
        
        // Find item with lowest wear count
        val underusedClothes = clothesList.minByOrNull { it.wearCount }
        
        return underusedClothes?.let {
            UnderusedItem(
                name = it.clothesName,
                imageUrl = it.image
            )
        }
    }
    
    private fun getColorFromName(colorName: String): Color {
        return when (colorName.lowercase()) {
            "red" -> Color(0xFFFF5252)
            "blue" -> Color(0xFF2196F3)
            "black" -> Color(0xFF000000)
            "white" -> Color(0xFFFFFFFF)
            "green" -> Color(0xFF4CAF50)
            "yellow" -> Color(0xFFFDD835)
            "pink" -> Color(0xFFE91E63)
            "brown" -> Color(0xFF795548)
            "gray", "grey" -> Color(0xFF9E9E9E)
            "beige" -> Color(0xFFF5F5DC)
            "navy" -> Color(0xFF001F3F)
            "purple" -> Color(0xFF9C27B0)
            "orange" -> Color(0xFFFF9800)
            "cyan" -> Color(0xFF00BCD4)
            "lime" -> Color(0xFFCDDC39)
            "indigo" -> Color(0xFF3F51B5)
            "teal" -> Color(0xFF009688)
            "amber" -> Color(0xFFFFC107)
            else -> Color(0xFF9E9E9E) // Default to gray for unknown colors
        }
    }
}

package com.example.closetly.model

import androidx.compose.ui.graphics.Color

data class ActivityStats(
    val items: Int,
    val outfits: Int,
    val reuse: Int
)

data class ClosetCategory(
    val name: String,
    val percentage: Float,
    val color: Color
)

data class WornColor(
    val name: String,
    val color: Color
)

data class UnderusedItem(
    val name: String,
    val imageUrl: String = ""
)

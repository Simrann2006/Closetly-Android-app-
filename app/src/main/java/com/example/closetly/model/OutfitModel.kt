package com.example.closetly.model

data class OutfitItemModel(
    val clothesId: String = "",
    val clothesName: String = "",
    val categoryName: String = "",
    val image: String = "",
    val position: Int = 0, // For ordering items in outfit
    val offsetX: Float = 0f, // X position in canvas
    val offsetY: Float = 0f, // Y position in canvas
    val scale: Float = 1f // Scale factor
)

data class OutfitModel(
    var outfitId: String = "",
    val outfitName: String = "",
    val items: List<OutfitItemModel> = emptyList(),
    val userId: String = "",
    
    // Date planning
    val plannedDate: String = "", // Format: "yyyy-MM-dd"
    val startDate: String = "", // For multi-day events
    val endDate: String = "", // For multi-day events
    
    // Occasion details
    val occasion: String = "", // e.g., "Casual", "Formal", "Work", "Party", "Date"
    val occasionNotes: String = "",
    
    // Weather and season
    val season: String = "",
    val weatherCondition: String = "",
    
    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val wornCount: Int = 0,
    val lastWornDate: String = "",
    
    // Thumbnail (composite image or first item)
    val thumbnailUrl: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "outfitId" to outfitId,
            "outfitName" to outfitName,
            "items" to items.map { mapOf(
                "clothesId" to it.clothesId,
                "clothesName" to it.clothesName,
                "categoryName" to it.categoryName,
                "image" to it.image,
                "position" to it.position,
                "offsetX" to it.offsetX,
                "offsetY" to it.offsetY,
                "scale" to it.scale
            )},
            "userId" to userId,
            "plannedDate" to plannedDate,
            "startDate" to startDate,
            "endDate" to endDate,
            "occasion" to occasion,
            "occasionNotes" to occasionNotes,
            "season" to season,
            "weatherCondition" to weatherCondition,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "isFavorite" to isFavorite,
            "wornCount" to wornCount,
            "lastWornDate" to lastWornDate,
            "thumbnailUrl" to thumbnailUrl
        )
    }
    
    fun hasDate(): Boolean = plannedDate.isNotEmpty()
    
    fun isMultiDay(): Boolean = startDate.isNotEmpty() && endDate.isNotEmpty()
    
    fun getDisplayDate(): String {
        return when {
            isMultiDay() -> "$startDate to $endDate"
            hasDate() -> plannedDate
            else -> "Not scheduled"
        }
    }
}

package com.example.closetly.model

data class ClothesModel(
    var clothesId : String = "",
    val clothesName : String = "",
    val brand : String = "",
    val season : String = "",
    val color : String = "",
    val notes : String = "",
    val categoryId : String = "",
    val categoryName : String = "",
    var image : String = "",
    var userId : String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "clothesId" to clothesId,
            "clothesName" to clothesName,
            "brand" to brand,
            "season" to season,
            "color" to color,
            "notes" to notes,
            "categoryId" to categoryId,
            "categoryName" to categoryName,
            "image" to image,
            "userId" to userId
        )
    }
}
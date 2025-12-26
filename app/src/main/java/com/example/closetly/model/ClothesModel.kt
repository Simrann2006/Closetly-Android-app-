package com.example.closetly.model

data class ClothesModel(
    var clothesId : String = "",
    val clothesName : String = "",
    val brand : String = "",
    val season : String = "",
    val notes : String = "",
    val categoryId : String = "",
    var image : String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "clothesId" to clothesId,
            "clothesName" to clothesName,
            "brand" to brand,
            "season" to season,
            "notes" to notes,
            "categoryId" to categoryId,
            "image" to image
        )
    }
}
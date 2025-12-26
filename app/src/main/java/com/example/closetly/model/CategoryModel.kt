package com.example.closetly.model

data class CategoryModel(
    var categoryId : String = "",
    val categoryName : String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "categoryId" to categoryId,
            "categoryName" to categoryName
        )
    }
}

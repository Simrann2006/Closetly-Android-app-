package com.example.closetly.model

data class ProductModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val size: String = "",
    val brand: String = "",
    val condition: String = "",
    val listingType: ListingType = ListingType.THRIFT,
    val sellerId: String = "",
    val sellerName: String = "",
    val sellerProfilePic: String = "",
    val rentPricePerDay: Double? = null,
    val status: String = "Available",
    val timestamp: Long = System.currentTimeMillis()
)

enum class ListingType {
    RENT, THRIFT
}
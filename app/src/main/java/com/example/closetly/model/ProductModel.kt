package com.example.closetly.model

data class MarketplaceProduct(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val size: String,
    val brand: String,
    val condition: String,
    val listingType: ListingType,
    val sellerName: String,
    val rentPricePerDay: Double? = null
)

enum class ListingType {
    SALE, RENT, THRIFT
}
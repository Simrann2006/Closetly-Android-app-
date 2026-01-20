package com.example.closetly.model

/**
 * Model representing a listing post item (clothes, accessories, etc.)
 * Each listing belongs to a user and will be shown as a card in the slider
 */
data class ListingItem(
    val listingId: String = "",
    val imageUrl: String = "",
    val itemName: String = "",
    val price: String = "",
    val timestamp: Long = 0L
)

/**
 * Model representing a slider item for the auto-carousel on the Home Screen.
 * Each slider represents ONE USER with their profile picture as background
 * and their MULTIPLE LISTINGS shown as small cards inside.
 * 
 * This data comes from Firebase and updates in real-time.
 * When a user posts a new listing, it automatically appears in their slider.
 */
data class SliderItemModel(
    val userId: String = "",
    val username: String = "",
    val profilePictureUrl: String = "",  // This becomes the BACKGROUND image
    val listings: List<ListingItem> = emptyList(),  // User's listings shown as cards
    val totalListings: Int = 0,
    val lastUpdated: Long = 0L  // Latest listing timestamp for sorting
)

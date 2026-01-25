package com.example.closetly.model


data class PostModel(
    val postId: String = "",
    
    val caption: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val images: List<String> = emptyList(),
    
    val userId: String = "",
    val username: String = "",
    val userProfilePic: String = "",
    val profilePicture: String = "",
    
    val price: Double = 0.0,
    val priceText: String = "",
    val title: String = "",
    
    val timestamp: Long = System.currentTimeMillis(),
    val postTimestamp: Long = 0L,
    
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    
    val postType: String = "post"
) {
    
    /**
     * Get the appropriate caption/description
     */
    fun resolveCaption(): String {
        return caption.ifEmpty { text }
    }
    

    fun formatPrice(): String {
        return if (priceText.isNotEmpty()) {
            priceText
        } else if (price > 0.0) {
            "Rs. ${price.toInt()}"
        } else {
            ""
        }
    }
}

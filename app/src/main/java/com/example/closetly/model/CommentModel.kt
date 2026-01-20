package com.example.closetly.model

data class CommentModel(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String = "",
    val commentText: String = "",
    val timestamp: Long = 0L,
    val likes: Map<String, Boolean> = emptyMap()
) {
    // Computed property for likes count
    val likesCount: Int
        get() = likes.size
    
    // Check if specific user liked this comment
    fun isLikedBy(userId: String): Boolean = likes.containsKey(userId)
}
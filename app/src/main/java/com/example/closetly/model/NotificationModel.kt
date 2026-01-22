package com.example.closetly.model

enum class NotificationType {
    FOLLOW,
    LIKE,
    COMMENT,
    POST
}
data class NotificationModel(
    var userName: String = "",
    var userProfileImage: Any = "", // drawable resource ID or URL string
    var type: NotificationType = NotificationType.FOLLOW,
    var message: String = "",
    var time: String = "",
    var isRead: Boolean = false,
    var senderId: String = "",
    var postId: String = "",
    var postImage: String = "",
    var commentText: String = "",
    var timestamp: Long = 0L

) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userName" to userName,
            "userProfileImage" to userProfileImage,
            "type" to type.name,
            "message" to message,
            "time" to time,
            "isRead" to isRead,
            "senderId" to senderId,
            "postId" to postId,
            "postImage" to postImage,
            "commentText" to commentText
        )
    }
}
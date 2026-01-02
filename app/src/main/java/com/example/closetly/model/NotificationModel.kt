package com.example.closetly.model

enum class NotificationType {
    FOLLOW,
    LIKE
}

data class NotificationModel(
    var userName: String = "",
    var userProfileImage: Any = "", // drawable resource ID or URL string
    var type: NotificationType = NotificationType.FOLLOW,
    var message: String = "",
    var time: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userName" to userName,
            "userProfileImage" to userProfileImage,
            "type" to type.name,
            "message" to message,
            "time" to time
        )
    }
}

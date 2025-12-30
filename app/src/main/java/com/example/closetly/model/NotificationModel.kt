package com.example.closetly.model

data class NotificationModel(
    var userName: String = "",
    var userProfileImage: String = "",
    var type: String = "FOLLOW",
    var message: String = "",
    var time: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userName" to userName,
            "userProfileImage" to userProfileImage,
            "type" to type,
            "message" to message,
            "time" to time
        )
    }
}

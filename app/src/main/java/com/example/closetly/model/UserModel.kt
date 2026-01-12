package com.example.closetly.model

data class UserModel(
    val userId : String = "",
    val fullName : String = "",
    val email : String = "",
    val phoneNumber : String = "",
    val selectedCountry : String = "",
    val profilePicture : String = "",
    val username : String = "",
    val bio : String = "",
    val fcmToken: String = ""
) {
    fun toMap() : Map<String, Any?>{
        return mapOf(
            "userId" to userId,
            "fullName" to fullName,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "selectedCountry" to selectedCountry,
            "profilePicture" to profilePicture,
            "username" to username,
            "bio" to bio,
            "fcmToken" to fcmToken
        )
    }
}


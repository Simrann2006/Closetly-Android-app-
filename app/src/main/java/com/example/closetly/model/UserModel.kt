package com.example.closetly.model

data class UserModel(
    val userId : String = "",
    val fullName : String = "",
    val email : String = "",
    val phoneNumber : String = ""
) {
    fun toMap() : Map<String, Any?>{
        return mapOf(
            "userId" to userId,
            "fullName" to fullName,
            "email" to email,
            "phoneNumber" to phoneNumber
        )
    }
}


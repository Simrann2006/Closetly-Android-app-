package com.example.closetly.model

data class UserModel(
    var userId : String = "",
    var fullName : String = "",
    val dob : String = "",
    var email : String = "",
) {
    fun toMap() : Map<String, Any?>{
        return mapOf(
            "userId" to userId,
            "email" to email,
            "fullName" to fullName,
            "dob" to dob
        )
    }
}


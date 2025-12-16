package com.example.closetly.model

data class UserModel(
    val userId : String = "",
    val fullName : String = "",
    val selectedDate : String = "",
    val email : String = "",
) {
    fun toMap() : Map<String, Any?>{
        return mapOf(
            "userId" to userId,
            "fullName" to fullName,
            "selectedDate" to selectedDate,
            "email" to email,
        )
    }
}


package com.example.closetly.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    var name by mutableStateOf("Aabriti")
        private set

    var username by mutableStateOf("aabriti")
        private set

    var bio by mutableStateOf("sup'")
        private set

    var imageUri by mutableStateOf<Uri?>(null)
        private set

    fun updateProfile(newName: String, newUsername: String, newBio: String) {
        name = newName
        username = newUsername
        bio = newBio
    }

    fun updateImage(uri: Uri?) {
        imageUri = uri
    }
}

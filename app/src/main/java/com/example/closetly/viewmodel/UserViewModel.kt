package com.example.closetly.viewmodel

import androidx.lifecycle.ViewModel
import com.example.closetly.model.UserModel
import com.example.closetly.repository.UserRepo
import com.google.firebase.auth.FirebaseUser

class UserViewModel (val repo : UserRepo) : ViewModel(){

    fun login(
        email: String, password: String,
        callback: (Boolean, String) -> Unit
    ){
        repo.login(email, password, callback)
    }

    fun register(
        email: String, password: String, fullName: String, selectedDate: String,
        callback: (Boolean, String, String) -> Unit
    ){
        repo.register(email, password, fullName, selectedDate, callback)
    }

    fun addUserToDatabase(
        userId: String, model: UserModel,
        callback: (Boolean, String) -> Unit
    ){
        repo.addUserToDatabase(userId, model, callback)
    }

    fun forgotPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ){
        repo.forgotPassword(email, callback)
    }

    fun editProfile(
        userId: String,
        model: UserModel, callback: (Boolean, String) -> Unit
    ){
        repo.editProfile(userId, model, callback)
    }

    fun logout(
        callback: (Boolean, String) -> Unit
    ){
        repo.logout(callback)
    }

    fun deleteAccount(
        userId: String,
        callback: (Boolean, String) -> Unit
    ){
        repo.deleteAccount(userId, callback)
    }

    fun getCurrentUser() : FirebaseUser?{
        return repo.getCurrentUser()
    }

    fun signInWithGoogle(
        idToken: String,
        callback: (Boolean, String) -> Unit
    ){
        repo.signInWithGoogle(idToken, callback)
    }

    fun getUserById(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ){
        repo.getUserById(userId, callback)
    }

    fun getAllUser(
        callback: (Boolean, String, List<UserModel>) -> Unit
    ){
        repo.getAllUser(callback)
    }
    
    fun toggleFollow(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean, String) -> Unit
    ){
        repo.toggleFollow(currentUserId, targetUserId, callback)
    }
    
    fun isFollowing(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean) -> Unit
    ){
        repo.isFollowing(currentUserId, targetUserId, callback)
    }
    
    fun getFollowersCount(
        userId: String,
        callback: (Int) -> Unit
    ){
        repo.getFollowersCount(userId, callback)
    }
    
    fun getFollowingCount(
        userId: String,
        callback: (Int) -> Unit
    ){
        repo.getFollowingCount(userId, callback)
    }
    
    fun getFollowersList(
        userId: String,
        callback: (List<UserModel>) -> Unit
    ){
        repo.getFollowersList(userId, callback)
    }
    
    fun getFollowingList(
        userId: String,
        callback: (List<UserModel>) -> Unit
    ){
        repo.getFollowingList(userId, callback)
    }
    
    fun blockUser(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean, String) -> Unit
    ){
        repo.blockUser(currentUserId, targetUserId, callback)
    }
    
    fun unblockUser(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean, String) -> Unit
    ){
        repo.unblockUser(currentUserId, targetUserId, callback)
    }
    
    fun isUserBlocked(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean) -> Unit
    ){
        repo.isUserBlocked(currentUserId, targetUserId, callback)
    }
    
    fun getBlockedUsersList(
        userId: String,
        callback: (List<UserModel>) -> Unit
    ){
        repo.getBlockedUsersList(userId, callback)
    }
}
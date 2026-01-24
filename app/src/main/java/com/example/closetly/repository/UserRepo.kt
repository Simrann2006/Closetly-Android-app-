package com.example.closetly.repository

import com.example.closetly.model.UserModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface UserRepo {

    fun login(
        email: String, password: String,
        callback: (Boolean, String) -> Unit
    )

    fun register(
        email: String, password: String, fullName: String, dob: String,
        callback: (Boolean, String, String) -> Unit
    )

    fun addUserToDatabase(
        userId: String, model: UserModel,
        callback: (Boolean, String) -> Unit
    )

    fun forgotPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    )

    fun editProfile(
        userId: String,
        model: UserModel, callback: (Boolean, String) -> Unit
    )

    fun logout(
        callback: (Boolean, String) -> Unit
    )

    fun deleteAccount(
        userId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getCurrentUser() : FirebaseUser?

    fun signInWithGoogle(
        idToken: String,
        callback: (Boolean, String) -> Unit
    )

    fun getUserById(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    )

    fun getAllUser(
        callback: (Boolean, String, List<UserModel>) -> Unit
    )
    
    fun checkUsernameExists(
        username: String,
        currentUserId: String,
        callback: (Boolean) -> Unit
    )
    
    fun toggleFollow(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean, String) -> Unit
    )
    
    fun isFollowing(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean) -> Unit
    )
    
    fun getFollowersCount(
        userId: String,
        callback: (Int) -> Unit
    )
    
    fun getFollowingCount(
        userId: String,
        callback: (Int) -> Unit
    )
    
    fun getFollowersList(
        userId: String,
        callback: (List<UserModel>) -> Unit
    )
    
    fun getFollowingList(
        userId: String,
        callback: (List<UserModel>) -> Unit
    )
    
    fun blockUser(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean, String) -> Unit
    )
    
    fun unblockUser(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean, String) -> Unit
    )
    
    fun isUserBlocked(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean) -> Unit
    )
    
    fun getBlockedUsersList(
        userId: String,
        callback: (List<UserModel>) -> Unit
    )
    
    fun getBlockedUsersListFlow(
        userId: String
    ): Flow<List<UserModel>>
}
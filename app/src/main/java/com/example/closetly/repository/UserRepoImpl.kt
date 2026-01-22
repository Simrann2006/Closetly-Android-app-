package com.example.closetly.repository

import android.content.Context
import com.example.closetly.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.toMap

class UserRepoImpl(private val context: Context) : UserRepo{

    val auth : FirebaseAuth = FirebaseAuth.getInstance()
    val database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val ref : DatabaseReference = database.getReference("Users")
    private val notificationRepo = NotificationRepoImpl()

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    callback(true, "Login success")
                } else{
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun register(
        email: String,
        password: String,
        fullName: String,
        dob: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    callback(true, "registration success", "${auth.currentUser?.uid}")
                } else{
                    callback(false, "${it.exception?.message}", "")
                }
            }
    }

    override fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful){
                callback(true, "Registration successfully")
            } else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun forgotPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    callback(true, "Link sent to $email")
                } else{
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun editProfile(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).updateChildren(model.toMap()).addOnCompleteListener {
            if (it.isSuccessful){
                callback(true, "Profile Updated successfully")
            } else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        try{
            auth.signOut()
            callback(true, "Logout successfully")
        } catch (e : Exception){
            callback(false, e.message.toString())
        }
    }

    override fun deleteAccount(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).removeValue().addOnCompleteListener {
            if (it.isSuccessful){
                callback(true, "Deleted successfully")
            } else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return  auth.currentUser
    }

    override fun signInWithGoogle(idToken: String, callback: (Boolean, String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = auth.currentUser
                    callback(true, user?.uid ?: "")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun getUserById(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        ref.child(userId).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue(UserModel::class.java)
                    if (user != null){
                        callback(true, "Profile fetched", user)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message,null)
            }

        })
    }

    override fun getAllUser(callback: (Boolean, String, List<UserModel>) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var allUsers = mutableListOf<UserModel>()
                    for(data in snapshot.children){
                        var user = data.getValue(UserModel::class.java)
                        if (user != null){
                            allUsers.add(user)
                        }
                    }
                    callback(true, "User fetched", allUsers)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false,error.message,emptyList())
            }
        })
    }
    
    override fun checkUsernameExists(
        username: String,
        currentUserId: String,
        callback: (Boolean) -> Unit
    ) {
        ref.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (data in snapshot.children) {
                            val userId = data.key
                            if (userId != currentUserId) {
                                callback(true)
                                return
                            }
                        }
                        callback(false)
                    } else {
                        callback(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }
    
    override fun toggleFollow(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val followingRef = ref.child(currentUserId).child("following").child(targetUserId)
        val followerRef = ref.child(targetUserId).child("followers").child(currentUserId)
        
        followingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    followingRef.removeValue()
                    followerRef.removeValue().addOnCompleteListener {
                        if (it.isSuccessful) {
                            callback(true, "Unfollowed successfully")
                        } else {
                            callback(false, it.exception?.message ?: "Failed to unfollow")
                        }
                    }
                } else {
                    followingRef.setValue(true)
                    followerRef.setValue(true).addOnCompleteListener {
                        if (it.isSuccessful) {
                            ref.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val currentUser = userSnapshot.getValue(UserModel::class.java)
                                    if (currentUser != null) {
                                        notificationRepo.sendFollowNotification(
                                            context = context,
                                            senderId = currentUserId,
                                            senderName = currentUser.username,
                                            senderImage = currentUser.profilePicture,
                                            receiverId = targetUserId
                                        )
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {}
                            })
                            callback(true, "Followed successfully")
                        } else {
                            callback(false, it.exception?.message ?: "Failed to follow")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }
    
    override fun isFollowing(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean) -> Unit
    ) {
        ref.child(currentUserId).child("following").child(targetUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.exists())
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }
    
    override fun getFollowersCount(userId: String, callback: (Int) -> Unit) {
        ref.child(userId).child("followers")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.childrenCount.toInt())
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(0)
                }
            })
    }
    
    override fun getFollowingCount(userId: String, callback: (Int) -> Unit) {
        ref.child(userId).child("following")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.childrenCount.toInt())
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(0)
                }
            })
    }
    
    override fun getFollowersList(userId: String, callback: (List<UserModel>) -> Unit) {
        ref.child(userId).child("followers")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val followersList = mutableListOf<UserModel>()
                    val followerIds = snapshot.children.mapNotNull { it.key }
                    
                    if (followerIds.isEmpty()) {
                        callback(emptyList())
                        return
                    }
                    
                    var processedCount = 0
                    followerIds.forEach { followerId ->
                        ref.child(followerId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                userSnapshot.getValue(UserModel::class.java)?.let {
                                    followersList.add(it)
                                }
                                processedCount++
                                if (processedCount == followerIds.size) {
                                    callback(followersList)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                processedCount++
                                if (processedCount == followerIds.size) {
                                    callback(followersList)
                                }
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }
    
    override fun getFollowingList(userId: String, callback: (List<UserModel>) -> Unit) {
        ref.child(userId).child("following")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val followingList = mutableListOf<UserModel>()
                    val followingIds = snapshot.children.mapNotNull { it.key }
                    
                    if (followingIds.isEmpty()) {
                        callback(emptyList())
                        return
                    }
                    
                    var processedCount = 0
                    followingIds.forEach { followingId ->
                        ref.child(followingId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                userSnapshot.getValue(UserModel::class.java)?.let {
                                    followingList.add(it)
                                }
                                processedCount++
                                if (processedCount == followingIds.size) {
                                    callback(followingList)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                processedCount++
                                if (processedCount == followingIds.size) {
                                    callback(followingList)
                                }
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }
}
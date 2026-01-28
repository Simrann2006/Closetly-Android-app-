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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
        ref.child(userId).addListenerForSingleValueEvent(object: ValueEventListener{
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
        val currentUserId = auth.currentUser?.uid
        
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val allUsers = mutableListOf<UserModel>()
                    
                    if (currentUserId == null) {
                        // If no current user, return all users
                        for(data in snapshot.children){
                            val user = data.getValue(UserModel::class.java)
                            if (user != null){
                                allUsers.add(user)
                            }
                        }
                        callback(true, "User fetched", allUsers)
                        return
                    }
                    
                    // Get blocked users list for current user
                    ref.child(currentUserId).child("blocked").get()
                        .addOnSuccessListener { blockedSnapshot ->
                            val blockedUserIds = blockedSnapshot.children.mapNotNull { it.key }.toSet()
                            
                            // Filter out blocked users and current user
                            for(data in snapshot.children){
                                val user = data.getValue(UserModel::class.java)
                                if (user != null && 
                                    user.userId != currentUserId && 
                                    !blockedUserIds.contains(user.userId)) {
                                    allUsers.add(user)
                                }
                            }
                            callback(true, "User fetched", allUsers)
                        }
                        .addOnFailureListener {
                            // If failed to get blocked users, return all users except current
                            for(data in snapshot.children){
                                val user = data.getValue(UserModel::class.java)
                                if (user != null && user.userId != currentUserId){
                                    allUsers.add(user)
                                }
                            }
                            callback(true, "User fetched", allUsers)
                        }
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
        // First check if either user has blocked the other
        ref.child(currentUserId).child("blocked").child(targetUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(blockSnapshot1: DataSnapshot) {
                    if (blockSnapshot1.exists()) {
                        // Current user has blocked target user - silently prevent action
                        callback(false, "Cannot follow this user")
                        return
                    }
                    
                    // Check if target user has blocked current user
                    ref.child(targetUserId).child("blocked").child(currentUserId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(blockSnapshot2: DataSnapshot) {
                                if (blockSnapshot2.exists()) {
                                    // Target user has blocked current user - silently prevent action
                                    callback(false, "Cannot follow this user")
                                    return
                                }
                                
                                // Neither user has blocked the other, proceed with follow/unfollow
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
                            
                            override fun onCancelled(error: DatabaseError) {
                                callback(false, error.message)
                            }
                        })
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
            .addListenerForSingleValueEvent(object : ValueEventListener {
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
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val followerIds = snapshot.children.mapNotNull { it.key }.toSet()
                    
                    // Get blocked users to exclude from count
                    ref.child(userId).child("blocked")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(blockedSnapshot: DataSnapshot) {
                                val blockedIds = blockedSnapshot.children.mapNotNull { it.key }.toSet()
                                // Count only followers who are not blocked
                                val validFollowersCount = followerIds.filter { !blockedIds.contains(it) }.size
                                callback(validFollowersCount)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                callback(followerIds.size)
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(0)
                }
            })
    }
    
    override fun getFollowingCount(userId: String, callback: (Int) -> Unit) {
        ref.child(userId).child("following")
            .addListenerForSingleValueEvent(object : ValueEventListener {
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
                    val followerIds = snapshot.children.mapNotNull { it.key }
                    
                    if (followerIds.isEmpty()) {
                        callback(emptyList())
                        return
                    }
                    
                    // Get blocked users first (users that userId has blocked)
                    ref.child(userId).child("blocked").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(blockedSnapshot: DataSnapshot) {
                            val blockedIds = blockedSnapshot.children.mapNotNull { it.key }.toSet()
                            val followersList = mutableListOf<UserModel>()
                            var processedCount = 0
                            
                            followerIds.forEach { followerId ->
                                // Skip if userId has blocked followerId
                                if (!blockedIds.contains(followerId)) {
                                    // Also check if followerId has blocked userId (two-way check)
                                    ref.child(followerId).child("blocked").child(userId)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(followerBlockSnapshot: DataSnapshot) {
                                                if (!followerBlockSnapshot.exists()) {
                                                    // Neither has blocked the other, add to list
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
                                                } else {
                                                    // Follower has blocked userId, skip
                                                    processedCount++
                                                    if (processedCount == followerIds.size) {
                                                        callback(followersList)
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                processedCount++
                                                if (processedCount == followerIds.size) {
                                                    callback(followersList)
                                                }
                                            }
                                        })
                                } else {
                                    processedCount++
                                    if (processedCount == followerIds.size) {
                                        callback(followersList)
                                    }
                                }
                            }
                        }
                        
                        override fun onCancelled(error: DatabaseError) {
                            callback(emptyList())
                        }
                    })
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
                    val followingIds = snapshot.children.mapNotNull { it.key }
                    
                    if (followingIds.isEmpty()) {
                        callback(emptyList())
                        return
                    }
                    
                    // Get blocked users first (users that userId has blocked)
                    ref.child(userId).child("blocked").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(blockedSnapshot: DataSnapshot) {
                            val blockedIds = blockedSnapshot.children.mapNotNull { it.key }.toSet()
                            val followingList = mutableListOf<UserModel>()
                            var processedCount = 0
                            
                            followingIds.forEach { followingId ->
                                // Skip if userId has blocked followingId
                                if (!blockedIds.contains(followingId)) {
                                    // Also check if followingId has blocked userId (two-way check)
                                    ref.child(followingId).child("blocked").child(userId)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(followingBlockSnapshot: DataSnapshot) {
                                                if (!followingBlockSnapshot.exists()) {
                                                    // Neither has blocked the other, add to list
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
                                                } else {
                                                    // Following user has blocked userId, skip
                                                    processedCount++
                                                    if (processedCount == followingIds.size) {
                                                        callback(followingList)
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                processedCount++
                                                if (processedCount == followingIds.size) {
                                                    callback(followingList)
                                                }
                                            }
                                        })
                                } else {
                                    processedCount++
                                    if (processedCount == followingIds.size) {
                                        callback(followingList)
                                    }
                                }
                            }
                        }
                        
                        override fun onCancelled(error: DatabaseError) {
                            callback(emptyList())
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }
    
    override fun blockUser(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Two-way blocking: Both users block each other
        // Remove all follow relationships in both directions
        val updates = hashMapOf<String, Any?>(
            "$currentUserId/blocked/$targetUserId" to true,
            "$targetUserId/blocked/$currentUserId" to true,  // Add reciprocal block
            "$currentUserId/following/$targetUserId" to null,
            "$targetUserId/followers/$currentUserId" to null,
            "$targetUserId/following/$currentUserId" to null,
            "$currentUserId/followers/$targetUserId" to null
        )
        
        ref.updateChildren(updates)
            .addOnSuccessListener {
                // Remove all interaction history between users
                removeAllInteractions(currentUserId, targetUserId)
                
                // Delete existing chats between the two users
                deleteChatsWithUser(currentUserId, targetUserId)
                
                callback(true, "User blocked successfully")
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Failed to block user")
            }
    }
    
    /**
     * Remove all interaction history between two users:
     * - Likes on each other's posts
     * - Comments on each other's posts
     * - Notifications from each other
     */
    private fun removeAllInteractions(userId1: String, userId2: String) {
        val database = FirebaseDatabase.getInstance()
        val postsRef = database.getReference("Posts")
        val commentsRef = database.getReference("Comments")
        val notificationsRef = database.getReference("Notifications")
        
        // Remove likes: userId1 likes on userId2's posts and vice versa
        postsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { postSnapshot ->
                    val postOwnerId = postSnapshot.child("userId").getValue(String::class.java)
                    val postId = postSnapshot.key
                    
                    if (postId != null) {
                        // Remove userId1's like from userId2's posts
                        if (postOwnerId == userId2) {
                            postSnapshot.child("likes").child(userId1).ref.removeValue()
                        }
                        // Remove userId2's like from userId1's posts
                        if (postOwnerId == userId1) {
                            postSnapshot.child("likes").child(userId2).ref.removeValue()
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        
        // Remove all comments from userId1 on userId2's posts and vice versa
        commentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentsToDelete = mutableListOf<String>()
                
                snapshot.children.forEach { commentSnapshot ->
                    val commentUserId = commentSnapshot.child("userId").getValue(String::class.java)
                    val commentId = commentSnapshot.key
                    
                    // Get the post to check the post owner
                    val postId = commentSnapshot.child("postId").getValue(String::class.java)
                    
                    if (commentId != null && postId != null) {
                        // Check if this comment should be deleted
                        postsRef.child(postId).child("userId").get().addOnSuccessListener { postOwnerSnapshot ->
                            val postOwnerId = postOwnerSnapshot.getValue(String::class.java)
                            
                            // Delete if: userId1 commented on userId2's post OR userId2 commented on userId1's post
                            if ((commentUserId == userId1 && postOwnerId == userId2) ||
                                (commentUserId == userId2 && postOwnerId == userId1)) {
                                commentsRef.child(commentId).removeValue()
                            }
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        
        // Remove notifications from each other
        // Remove notifications from userId2 to userId1
        notificationsRef.child(userId1).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { notificationSnapshot ->
                    val senderId = notificationSnapshot.child("senderId").getValue(String::class.java)
                    if (senderId == userId2) {
                        notificationSnapshot.ref.removeValue()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        
        // Remove notifications from userId1 to userId2
        notificationsRef.child(userId2).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { notificationSnapshot ->
                    val senderId = notificationSnapshot.child("senderId").getValue(String::class.java)
                    if (senderId == userId1) {
                        notificationSnapshot.ref.removeValue()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    
    /**
     * Delete all chats and messages between two users
     */
    private fun deleteChatsWithUser(userId1: String, userId2: String) {
        val database = FirebaseDatabase.getInstance()
        val chatsRef = database.getReference("Chats")
        val messagesRef = database.getReference("Messages")
        
        // Create chatId from sorted participant IDs
        val participants = listOf(userId1, userId2).sorted()
        val chatId = participants.joinToString("_")
        
        // Delete messages first
        messagesRef.child(chatId).removeValue().addOnSuccessListener {
            // Then delete the chat
            chatsRef.child(chatId).removeValue()
        }
    }
    
    override fun unblockUser(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Remove two-way block entries
        val updates = hashMapOf<String, Any?>(
            "$currentUserId/blocked/$targetUserId" to null,
            "$targetUserId/blocked/$currentUserId" to null
        )
        
        ref.updateChildren(updates)
            .addOnSuccessListener {
                callback(true, "User unblocked successfully")
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Failed to unblock user")
            }
    }
    
    override fun isUserBlocked(
        currentUserId: String,
        targetUserId: String,
        callback: (Boolean) -> Unit
    ) {
        // Check if either user has blocked the other (two-way check)
        ref.child(currentUserId).child("blocked").child(targetUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot1: DataSnapshot) {
                    if (snapshot1.exists()) {
                        callback(true)
                        return
                    }
                    
                    // Check if target user has blocked current user
                    ref.child(targetUserId).child("blocked").child(currentUserId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot2: DataSnapshot) {
                                callback(snapshot2.exists())
                            }
                            
                            override fun onCancelled(error: DatabaseError) {
                                callback(false)
                            }
                        })
                }
                
                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }
    
    override fun getBlockedUsersList(userId: String, callback: (List<UserModel>) -> Unit) {
        ref.child(userId).child("blocked")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val blockedList = mutableListOf<UserModel>()
                    val blockedIds = snapshot.children.mapNotNull { it.key }
                    
                    if (blockedIds.isEmpty()) {
                        callback(emptyList())
                        return
                    }
                    
                    var processedCount = 0
                    blockedIds.forEach { blockedId ->
                        ref.child(blockedId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                userSnapshot.getValue(UserModel::class.java)?.let {
                                    blockedList.add(it)
                                }
                                processedCount++
                                if (processedCount == blockedIds.size) {
                                    callback(blockedList)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                processedCount++
                                if (processedCount == blockedIds.size) {
                                    callback(blockedList)
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
    
    override fun getBlockedUsersListFlow(userId: String): Flow<List<UserModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val blockedIds = snapshot.children.mapNotNull { it.key }
                
                if (blockedIds.isEmpty()) {
                    trySend(emptyList())
                    return
                }
                
                // Use synchronized map to avoid concurrent modification
                val blockedMap = mutableMapOf<String, UserModel>()
                var processedCount = 0
                
                blockedIds.forEach { blockedId ->
                    ref.child(blockedId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            synchronized(blockedMap) {
                                userSnapshot.getValue(UserModel::class.java)?.let {
                                    blockedMap[blockedId] = it
                                }
                                processedCount++
                                if (processedCount == blockedIds.size) {
                                    trySend(blockedMap.values.toList())
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            synchronized(blockedMap) {
                                processedCount++
                                if (processedCount == blockedIds.size) {
                                    trySend(blockedMap.values.toList())
                                }
                            }
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }

        ref.child(userId).child("blocked").addValueEventListener(listener)

        awaitClose {
            ref.child(userId).child("blocked").removeEventListener(listener)
        }
    }
}
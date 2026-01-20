package com.example.closetly.repository

import com.example.closetly.model.PostModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PostRepoImpl : PostRepo {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Posts")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val notificationRepo = NotificationRepoImpl()

    override fun addPost(model: PostModel, callback: (Boolean, String) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            callback(false, "User not authenticated")
            return
        }
        val id = ref.push().key.toString()
        val usersRef = database.getReference("Users").child(currentUserId)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("username").getValue(String::class.java) ?: "Unknown"
                val profilePic = snapshot.child("profilePicture").getValue(String::class.java) ?: ""
                val post = model.copy(
                    postId = id,
                    userId = currentUserId,
                    username = userName,
                    userProfilePic = profilePic
                )
                ref.child(id).setValue(post).addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (post.caption.contains("sale", ignoreCase = true)) {
                            notificationRepo.sendPostNotification(
                                postOwnerId = currentUserId,
                                postOwnerName = userName,
                                postOwnerImage = profilePic,
                                postId = id,
                                postImage = post.imageUrl,
                                postCaption = post.caption
                            )
                        }
                        callback(true, "Post added successfully")
                    } else {
                        callback(false, "${it.exception?.message}")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun updatePost(model: PostModel, callback: (Boolean, String) -> Unit) {
        ref.child(model.postId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Post updated successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deletePost(postId: String, callback: (Boolean, String) -> Unit) {
        ref.child(postId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Post deleted successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getPostById(postId: String, callback: (Boolean, String, PostModel?) -> Unit) {
        ref.child(postId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val post = snapshot.getValue(PostModel::class.java)
                    callback(true, "Post fetched", post)
                } else {
                    callback(false, "Post not found", null)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getAllPosts(callback: (Boolean, String, List<PostModel>?) -> Unit) {
        ref.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<PostModel>()
                for (data in snapshot.children) {
                    val post = data.getValue(PostModel::class.java)
                    if (post != null) {
                        posts.add(post)
                    }
                }
                posts.reverse()
                callback(true, "Posts fetched", posts)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getUserPosts(userId: String, callback: (Boolean, String, List<PostModel>?) -> Unit) {
        ref.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<PostModel>()
                for (data in snapshot.children) {
                    val post = data.getValue(PostModel::class.java)
                    if (post != null) {
                        posts.add(post)
                    }
                }
                posts.sortByDescending { it.timestamp }
                callback(true, "User posts fetched", posts)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }
}

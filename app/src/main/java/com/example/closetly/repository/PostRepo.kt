package com.example.closetly.repository

import com.example.closetly.model.PostModel

interface PostRepo {
    fun addPost(model: PostModel, callback: (Boolean, String) -> Unit)
    fun updatePost(model: PostModel, callback: (Boolean, String) -> Unit)
    fun deletePost(postId: String, callback: (Boolean, String) -> Unit)
    fun getPostById(postId: String, callback: (Boolean, String, PostModel?) -> Unit)
    fun getAllPosts(callback: (Boolean, String, List<PostModel>?) -> Unit)
    fun getUserPosts(userId: String, callback: (Boolean, String, List<PostModel>?) -> Unit)
}

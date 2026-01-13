package com.example.closetly.viewmodel

import androidx.lifecycle.ViewModel
import com.example.closetly.model.PostModel
import com.example.closetly.repository.PostRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PostViewModel(val repo: PostRepo) : ViewModel() {
    private val _posts = MutableStateFlow<List<PostModel>>(emptyList())
    val posts: StateFlow<List<PostModel>> = _posts

    fun loadPosts() {
        repo.getAllPosts { success, _, data ->
            if (success && data != null) {
                _posts.value = data
            }
        }
    }

    fun addPost(post: PostModel, onResult: (Boolean, String) -> Unit) {
        repo.addPost(post) { success, msg ->
            if (success) loadPosts()
            onResult(success, msg)
        }
    }

    fun updatePost(post: PostModel, onResult: (Boolean, String) -> Unit) {
        repo.updatePost(post) { success, msg ->
            if (success) loadPosts()
            onResult(success, msg)
        }
    }

    fun deletePost(postId: String, onResult: (Boolean, String) -> Unit) {
        repo.deletePost(postId) { success, msg ->
            if (success) loadPosts()
            onResult(success, msg)
        }
    }

    fun getPostById(postId: String, onResult: (Boolean, String, PostModel?) -> Unit) {
        repo.getPostById(postId) { success, msg, data ->
            onResult(success, msg, data)
        }
    }

    fun getUserPosts(userId: String, onResult: (List<PostModel>) -> Unit) {
        repo.getUserPosts(userId) { success, _, data ->
            onResult(data ?: emptyList())
        }
    }
}

package com.example.closetly.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.closetly.model.CategoryModel
import com.example.closetly.repository.CategoryRepo

class CategoryViewModel(val repo: CategoryRepo) : ViewModel() {

    private val _categories = MutableLiveData<List<CategoryModel>?>()
    val categories: MutableLiveData<List<CategoryModel>?> get() = _categories

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    fun addCategory(model: CategoryModel, callback: (Boolean, String) -> Unit) {
        repo.addCategory(model, callback)
    }

    fun deleteCategory(categoryId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteCategory(categoryId, callback)
    }

    fun getAllCategories(callback: (Boolean, String, List<CategoryModel>?) -> Unit) {
        _loading.postValue(true)
        repo.getAllCategories { success, msg, data ->
            if (success) {
                _loading.postValue(false)
                _categories.postValue(data)
            }
            callback(success, msg, data)
        }
    }

    fun getCategoryById(categoryId: String, callback: (Boolean, String, CategoryModel?) -> Unit) {
        repo.getCategoryById(categoryId, callback)
    }
}

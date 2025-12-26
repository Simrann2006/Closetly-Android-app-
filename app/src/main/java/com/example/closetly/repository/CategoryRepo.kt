package com.example.closetly.repository

import com.example.closetly.model.CategoryModel

interface CategoryRepo {
    fun addCategory(model: CategoryModel, callback: (Boolean, String) -> Unit)

    fun deleteCategory(categoryId: String, callback: (Boolean, String) -> Unit)

    fun getAllCategories(callback: (Boolean, String, List<CategoryModel>?) -> Unit)

    fun getCategoryById(categoryId: String, callback: (Boolean, String, CategoryModel?) -> Unit)
}

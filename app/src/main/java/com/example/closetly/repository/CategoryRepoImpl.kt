package com.example.closetly.repository

import com.example.closetly.model.CategoryModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoryRepoImpl : CategoryRepo {

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val categoryRef: DatabaseReference = database.getReference("Categories")
    val clothesRef: DatabaseReference = database.getReference("Clothess")
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun addCategory(model: CategoryModel, callback: (Boolean, String) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            callback(false, "User not authenticated")
            return
        }
        
        val id = categoryRef.push().key.toString()
        model.categoryId = id
        model.userId = currentUserId
        categoryRef.child(id).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Category added successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deleteCategory(categoryId: String, callback: (Boolean, String) -> Unit) {
        clothesRef.orderByChild("categoryId").equalTo(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        data.ref.removeValue()
                    }
                    
                    categoryRef.child(categoryId).removeValue().addOnCompleteListener {
                        if (it.isSuccessful) {
                            callback(true, "Category and all items deleted successfully")
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

    override fun getAllCategories(callback: (Boolean, String, List<CategoryModel>?) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            callback(false, "User not authenticated", null)
            return
        }

        categoryRef.orderByChild("userId").equalTo(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val allCategories = mutableListOf<CategoryModel>()
                        for (data in snapshot.children) {
                            val category = data.getValue(CategoryModel::class.java)
                            if (category != null) {
                                allCategories.add(category)
                            }
                        }
                        callback(true, "Categories fetched successfully", allCategories)
                    } else {
                        callback(true, "No categories found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getCategoryById(
        categoryId: String,
        callback: (Boolean, String, CategoryModel?) -> Unit
    ) {
        categoryRef.child(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val category = snapshot.getValue(CategoryModel::class.java)
                    if (category != null) {
                        callback(true, "Category fetched successfully", category)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }
}

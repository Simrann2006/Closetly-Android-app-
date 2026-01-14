package com.example.closetly.repository

import com.example.closetly.model.ProductModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProductRepoImpl : ProductRepo {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Products")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun addProduct(model: ProductModel, callback: (Boolean, String) -> Unit) {
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
                val product = model.copy(
                    id = id,
                    sellerId = currentUserId,
                    sellerName = userName,
                    sellerProfilePic = profilePic
                )
                ref.child(id).setValue(product).addOnCompleteListener {
                    if (it.isSuccessful) {
                        callback(true, "Product added successfully")
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

    override fun editProduct(model: ProductModel, callback: (Boolean, String) -> Unit) {
        ref.child(model.id).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product updated successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deleteProduct(productId: String, callback: (Boolean, String) -> Unit) {
        ref.child(productId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Product deleted successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getProductById(productId: String, callback: (Boolean, String, ProductModel?) -> Unit) {
        ref.child(productId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val product = snapshot.getValue(ProductModel::class.java)
                    callback(true, "Product fetched", product)
                } else {
                    callback(false, "Product not found", null)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getAllProducts(callback: (Boolean, String, List<ProductModel>?) -> Unit) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = mutableListOf<ProductModel>()
                var remainingFetches = snapshot.childrenCount.toInt()
                
                if (remainingFetches == 0) {
                    callback(true, "Products fetched", products)
                    return
                }
                
                for (data in snapshot.children) {
                    val product = data.getValue(ProductModel::class.java)
                    if (product != null) {
                        // Fetch fresh user data for each product
                        val usersRef = database.getReference("Users").child(product.sellerId)
                        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val freshUserName = userSnapshot.child("username").getValue(String::class.java) ?: product.sellerName
                                val freshProfilePic = userSnapshot.child("profilePicture").getValue(String::class.java) ?: product.sellerProfilePic
                                
                                // Update product with fresh user data
                                val updatedProduct = product.copy(
                                    sellerName = freshUserName,
                                    sellerProfilePic = freshProfilePic
                                )
                                products.add(updatedProduct)
                                
                                remainingFetches--
                                if (remainingFetches == 0) {
                                    callback(true, "Products fetched", products.sortedByDescending { it.timestamp })
                                }
                            }
                            
                            override fun onCancelled(error: DatabaseError) {
                                // If user fetch fails, use existing data
                                products.add(product)
                                remainingFetches--
                                if (remainingFetches == 0) {
                                    callback(true, "Products fetched", products.sortedByDescending { it.timestamp })
                                }
                            }
                        })
                    } else {
                        remainingFetches--
                        if (remainingFetches == 0) {
                            callback(true, "Products fetched", products.sortedByDescending { it.timestamp })
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getUserProducts(userId: String, callback: (Boolean, String, List<ProductModel>?) -> Unit) {
        ref.orderByChild("sellerId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = mutableListOf<ProductModel>()
                for (data in snapshot.children) {
                    val product = data.getValue(ProductModel::class.java)
                    if (product != null) {
                        products.add(product)
                    }
                }
                callback(true, "User products fetched", products)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }
}

package com.example.closetly.repository

import com.example.closetly.model.OutfitModel
import com.example.closetly.model.OutfitItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OutfitRepoImpl : OutfitRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Outfits")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun addOutfit(model: OutfitModel, callback: (Boolean, String) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            callback(false, "User not authenticated")
            return
        }

        val id = ref.push().key.toString()
        val outfitWithId = model.copy(
            outfitId = id,
            userId = currentUserId
        )
        
        ref.child(id).setValue(outfitWithId.toMap()).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Outfit created successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun editOutfit(model: OutfitModel, callback: (Boolean, String) -> Unit) {
        val updatedModel = model.copy(updatedAt = System.currentTimeMillis())
        ref.child(model.outfitId).updateChildren(updatedModel.toMap()).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Outfit updated successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deleteOutfit(outfitId: String, callback: (Boolean, String) -> Unit) {
        ref.child(outfitId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Outfit deleted successfully")
            } else {
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getOutfitById(
        outfitId: String,
        callback: (Boolean, String, OutfitModel?) -> Unit
    ) {
        ref.child(outfitId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val outfit = parseOutfit(snapshot)
                    callback(true, "Success", outfit)
                } else {
                    callback(false, "Outfit not found", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getAllOutfits(callback: (Boolean, String, List<OutfitModel>?) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            callback(false, "User not authenticated", null)
            return
        }

        ref.orderByChild("userId").equalTo(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val outfits = mutableListOf<OutfitModel>()
                    snapshot.children.forEach {
                        val outfit = parseOutfit(it)
                        outfit?.let { outfits.add(it) }
                    }
                    callback(true, "Success", outfits.sortedByDescending { it.updatedAt })
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getOutfitsByDate(
        date: String,
        callback: (Boolean, String, List<OutfitModel>?) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            callback(false, "User not authenticated", null)
            return
        }

        ref.orderByChild("userId").equalTo(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val outfits = mutableListOf<OutfitModel>()
                    snapshot.children.forEach {
                        val outfit = parseOutfit(it)
                        outfit?.let {
                            if (outfit.plannedDate == date || 
                                (outfit.startDate <= date && outfit.endDate >= date)) {
                                outfits.add(outfit)
                            }
                        }
                    }
                    callback(true, "Success", outfits)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getOutfitsByDateRange(
        startDate: String,
        endDate: String,
        callback: (Boolean, String, List<OutfitModel>?) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            callback(false, "User not authenticated", null)
            return
        }

        ref.orderByChild("userId").equalTo(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val outfits = mutableListOf<OutfitModel>()
                    snapshot.children.forEach {
                        val outfit = parseOutfit(it)
                        outfit?.let {
                            val hasOverlap = outfit.plannedDate.isNotEmpty() && 
                                           outfit.plannedDate >= startDate && 
                                           outfit.plannedDate <= endDate
                            val isMultiDay = outfit.startDate.isNotEmpty() && 
                                           outfit.endDate.isNotEmpty() &&
                                           outfit.startDate <= endDate && 
                                           outfit.endDate >= startDate
                            
                            if (hasOverlap || isMultiDay) {
                                outfits.add(outfit)
                            }
                        }
                    }
                    callback(true, "Success", outfits)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getFavoriteOutfits(callback: (Boolean, String, List<OutfitModel>?) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            callback(false, "User not authenticated", null)
            return
        }

        ref.orderByChild("userId").equalTo(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val outfits = mutableListOf<OutfitModel>()
                    snapshot.children.forEach {
                        val outfit = parseOutfit(it)
                        outfit?.let { 
                            if (outfit.isFavorite) {
                                outfits.add(outfit)
                            }
                        }
                    }
                    callback(true, "Success", outfits.sortedByDescending { it.updatedAt })
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun markAsWorn(
        outfitId: String,
        wornDate: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(outfitId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val outfit = parseOutfit(snapshot)
                outfit?.let {
                    val updatedOutfit = outfit.copy(
                        wornCount = outfit.wornCount + 1,
                        lastWornDate = wornDate,
                        updatedAt = System.currentTimeMillis()
                    )
                    ref.child(outfitId).updateChildren(updatedOutfit.toMap())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                callback(true, "Marked as worn")
                            } else {
                                callback(false, "${task.exception?.message}")
                            }
                        }
                }
            } else {
                callback(false, "Outfit not found")
            }
        }.addOnFailureListener {
            callback(false, "${it.message}")
        }
    }

    private fun parseOutfit(snapshot: DataSnapshot): OutfitModel? {
        return try {
            val outfitId = snapshot.child("outfitId").getValue(String::class.java) ?: ""
            val outfitName = snapshot.child("outfitName").getValue(String::class.java) ?: ""
            val userId = snapshot.child("userId").getValue(String::class.java) ?: ""
            val plannedDate = snapshot.child("plannedDate").getValue(String::class.java) ?: ""
            val startDate = snapshot.child("startDate").getValue(String::class.java) ?: ""
            val endDate = snapshot.child("endDate").getValue(String::class.java) ?: ""
            val occasion = snapshot.child("occasion").getValue(String::class.java) ?: ""
            val occasionNotes = snapshot.child("occasionNotes").getValue(String::class.java) ?: ""
            val season = snapshot.child("season").getValue(String::class.java) ?: ""
            val weatherCondition = snapshot.child("weatherCondition").getValue(String::class.java) ?: ""
            val createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: 0L
            val updatedAt = snapshot.child("updatedAt").getValue(Long::class.java) ?: 0L
            val isFavorite = snapshot.child("isFavorite").getValue(Boolean::class.java) ?: false
            val wornCount = snapshot.child("wornCount").getValue(Int::class.java) ?: 0
            val lastWornDate = snapshot.child("lastWornDate").getValue(String::class.java) ?: ""
            val thumbnailUrl = snapshot.child("thumbnailUrl").getValue(String::class.java) ?: ""

            val items = mutableListOf<OutfitItemModel>()
            snapshot.child("items").children.forEach { itemSnapshot ->
                val clothesId = itemSnapshot.child("clothesId").getValue(String::class.java) ?: ""
                val clothesName = itemSnapshot.child("clothesName").getValue(String::class.java) ?: ""
                val categoryName = itemSnapshot.child("categoryName").getValue(String::class.java) ?: ""
                val image = itemSnapshot.child("image").getValue(String::class.java) ?: ""
                val position = itemSnapshot.child("position").getValue(Int::class.java) ?: 0
                val offsetX = itemSnapshot.child("offsetX").getValue(Float::class.java) ?: 0f
                val offsetY = itemSnapshot.child("offsetY").getValue(Float::class.java) ?: 0f
                val scale = itemSnapshot.child("scale").getValue(Float::class.java) ?: 1f
                
                items.add(OutfitItemModel(clothesId, clothesName, categoryName, image, position, offsetX, offsetY, scale))
            }

            OutfitModel(
                outfitId = outfitId,
                outfitName = outfitName,
                items = items,
                userId = userId,
                plannedDate = plannedDate,
                startDate = startDate,
                endDate = endDate,
                occasion = occasion,
                occasionNotes = occasionNotes,
                season = season,
                weatherCondition = weatherCondition,
                createdAt = createdAt,
                updatedAt = updatedAt,
                isFavorite = isFavorite,
                wornCount = wornCount,
                lastWornDate = lastWornDate,
                thumbnailUrl = thumbnailUrl
            )
        } catch (e: Exception) {
            null
        }
    }
}

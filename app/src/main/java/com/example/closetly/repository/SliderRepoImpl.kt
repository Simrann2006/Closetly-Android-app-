package com.example.closetly.repository

import android.util.Log
import com.example.closetly.model.ListingItem
import com.example.closetly.model.SliderItemModel
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementation of SliderRepo that provides real-time slider data from Firebase.
 * 
 * LOGIC:
 * 1. Fetches all listings (sale + rent items) from Firebase in real-time
 * 2. Groups listings by userId (so each user gets one slider)
 * 3. Each slider shows user's profile picture as background
 * 4. Multiple listing cards (images, names, prices) shown inside each slider
 * 5. Automatically updates when any user posts a new listing or rent item
 */
class SliderRepoImpl : SliderRepo {
    
    private val database = FirebaseDatabase.getInstance()
    private val productsRef = database.getReference("Products")  // Sale items
    private val rentRef = database.getReference("Rent")  // Rent items
    private val usersRef = database.getReference("Users")
    
    companion object {
        private const val TAG = "SliderRepoImpl"
        private const val MAX_LISTINGS_PER_USER = 3  // Show max 3 listing cards per slider
        private const val MAX_USERS_IN_SLIDER = 10   // Show max 10 users in slider
    }
    
    /**
     * Returns a Flow that emits slider items grouped by user in real-time.
     * Fetches from both Products (sale) and Rent nodes.
     * 
     * HOW IT WORKS:
     * - Listens to both Products and Rent Firebase collections
     * - Combines all items (sale + rent) from both sources
     * - Groups all items by userId
     * - For each user, fetches their profile picture from users collection
     * - Creates one SliderItemModel per user with their items
     * - Sorts users by their latest item timestamp (most recent first)
     * - Returns top 10 active users
     */
    override fun getSliderItems(): Flow<List<SliderItemModel>> = callbackFlow {
        val allListings = mutableListOf<Triple<String, String, ListingItem>>()
        val sliderItems = mutableListOf<SliderItemModel>()
        var productsLoaded = false
        var rentLoaded = false
        
        fun processAndEmitItems() {
            if (!productsLoaded || !rentLoaded) return
            
            // Group all items by userId
            val listingsByUser = allListings.groupBy { it.first }
            Log.d(TAG, "Grouped ${allListings.size} items into ${listingsByUser.size} users")
            
            sliderItems.clear()
            
            for ((userId, userListings) in listingsByUser) {
                try {
                    val listings = userListings
                        .map { it.third }
                        .sortedByDescending { it.timestamp }
                        .take(MAX_LISTINGS_PER_USER)
                    
                    val lastUpdated = listings.maxOfOrNull { it.timestamp } ?: 0L
                    
                    // Fetch user data from Users node
                    usersRef.child(userId).get().addOnSuccessListener { userSnapshot ->
                        if (userSnapshot.exists()) {
                            val username = userSnapshot.child("fullName").getValue(String::class.java) 
                                ?: userSnapshot.child("username").getValue(String::class.java) 
                                ?: "User"
                            val profilePictureUrl = userSnapshot.child("profilePicture").getValue(String::class.java) 
                                ?: userSnapshot.child("profilePic").getValue(String::class.java)
                                ?: ""
                            
                            Log.d(TAG, "User data: $username - ${listings.size} items")
                            
                            if (listings.isNotEmpty()) {
                                val sliderItem = SliderItemModel(
                                    userId = userId,
                                    username = username,
                                    profilePictureUrl = profilePictureUrl,
                                    listings = listings,
                                    totalListings = userListings.size,
                                    lastUpdated = lastUpdated
                                )
                                
                                sliderItems.add(sliderItem)
                                
                                val sortedItems = sliderItems
                                    .sortedByDescending { it.lastUpdated }
                                    .take(MAX_USERS_IN_SLIDER)
                                
                                trySend(sortedItems)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating slider for user $userId: ${e.message}", e)
                }
            }
        }
        
        // Listener for Products (sale items)
        val productsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Products data changed: ${snapshot.childrenCount} items")
                
                // Remove old products and add new ones
                allListings.removeAll { it.third.listingId.startsWith("product_") }
                
                for (child in snapshot.children) {
                    try {
                        val listingId = child.key ?: ""
                        val userId = child.child("sellerId").getValue(String::class.java) ?: ""
                        val imageUrl = child.child("imageUrl").getValue(String::class.java) ?: ""
                        val itemName = child.child("title").getValue(String::class.java) ?: ""
                        val listingTypeStr = child.child("listingType").getValue(String::class.java) ?: "THRIFT"
                        
                        // Get appropriate price based on listing type
                        val price = if (listingTypeStr == "RENT") {
                            val rentPriceDouble = child.child("rentPricePerDay").getValue(Double::class.java) ?: 0.0
                            "₹${rentPriceDouble.toInt()}/day"
                        } else {
                            val priceDouble = child.child("price").getValue(Double::class.java) ?: 0.0
                            "₹${priceDouble.toInt()}"
                        }
                        
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                        val status = child.child("status").getValue(String::class.java) ?: "Available"
                        
                        Log.d(TAG, "Product: id=$listingId, userId=$userId, type=$listingTypeStr, status=$status")
                        
                        if (listingId.isNotEmpty() && userId.isNotEmpty() && imageUrl.isNotEmpty() && status == "Available") {
                            val listing = ListingItem(
                                listingId = "product_$listingId",
                                imageUrl = imageUrl,
                                itemName = itemName,
                                price = price,
                                timestamp = timestamp
                            )
                            allListings.add(Triple(userId, "", listing))
                            Log.d(TAG, "✓ Added ${listingTypeStr} item for user: $userId")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing product: ${e.message}", e)
                    }
                }
                
                productsLoaded = true
                processAndEmitItems()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Products listener cancelled: ${error.message}")
            }
        }
        
        // Listener for Rent items  
        val rentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Rent data changed: ${snapshot.childrenCount} items")
                
                // Remove old rent items and add new ones
                allListings.removeAll { it.third.listingId.startsWith("rent_") }
                
                for (child in snapshot.children) {
                    try {
                        val listingId = child.key ?: ""
                        // Try multiple field names for userId
                        val userId = child.child("sellerId").getValue(String::class.java) 
                            ?: child.child("ownerId").getValue(String::class.java)
                            ?: child.child("userId").getValue(String::class.java) ?: ""
                        val imageUrl = child.child("imageUrl").getValue(String::class.java) ?: ""
                        val itemName = child.child("title").getValue(String::class.java) ?: ""
                        // Try multiple field names for rent price
                        val rentPriceDouble = child.child("rentPricePerDay").getValue(Double::class.java) 
                            ?: child.child("rentPrice").getValue(Double::class.java) 
                            ?: child.child("price").getValue(Double::class.java) ?: 0.0
                        val price = "₹${rentPriceDouble.toInt()}/day"
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                        val status = child.child("status").getValue(String::class.java) ?: "Available"
                        val listingType = child.child("listingType").getValue(String::class.java) ?: ""
                        
                        Log.d(TAG, "Rent item: id=$listingId, userId=$userId, status=$status, listingType=$listingType")
                        
                        if (listingId.isNotEmpty() && userId.isNotEmpty() && imageUrl.isNotEmpty() && status == "Available") {
                            val listing = ListingItem(
                                listingId = "rent_$listingId",
                                imageUrl = imageUrl,
                                itemName = itemName,
                                price = price,
                                timestamp = timestamp
                            )
                            allListings.add(Triple(userId, "", listing))
                            Log.d(TAG, "✓ Added rent item for user: $userId")
                        } else {
                            Log.w(TAG, "✗ Skipped rent item: id=$listingId, userId=$userId, imageUrl=${imageUrl.take(20)}, status=$status")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing rent item: ${e.message}", e)
                    }
                }
                
                rentLoaded = true
                processAndEmitItems()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Rent listener cancelled: ${error.message}")
            }
        }
        
        // Attach both listeners
        productsRef.addValueEventListener(productsListener)
        rentRef.addValueEventListener(rentListener)
        Log.d(TAG, "Firebase listeners attached for Products and Rent")
        
        // Clean up listeners when Flow is cancelled
        awaitClose {
            Log.d(TAG, "Removing Firebase listeners")
            productsRef.removeEventListener(productsListener)
            rentRef.removeEventListener(rentListener)
        }
    }
}

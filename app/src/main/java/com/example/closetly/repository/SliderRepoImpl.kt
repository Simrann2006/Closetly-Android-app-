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
        private const val MAX_LISTINGS_PER_USER = 5  // Show max 5 listing cards per user slider
        private const val MAX_USERS_IN_SLIDER = 5    // Show only latest 5 users in slider
    }

    override fun getSliderItems(): Flow<List<SliderItemModel>> = callbackFlow {
        val allPostsMap = mutableMapOf<String, Pair<String, ListingItem>>() // postId -> (userId, listing)
        val userDataCache = mutableMapOf<String, Pair<String, String>>() // userId -> (username, profilePic)
        var productsLoaded = false
        var rentLoaded = false
        
        fun emitSliderItems(
            userListingsMap: Map<String, List<ListingItem>>,
            userData: Map<String, Pair<String, String>>
        ) {
            val sliderItems = userListingsMap.map { (userId, listings) ->
                val (username, profilePic) = userData[userId] ?: Pair("User", "")
                val lastUpdated = listings.maxOfOrNull { it.timestamp } ?: 0L
                
                SliderItemModel(
                    userId = userId,
                    username = username,
                    profilePictureUrl = profilePic,
                    listings = listings,
                    totalListings = listings.size,
                    lastUpdated = lastUpdated
                )
            }.sortedByDescending { it.lastUpdated }
            
            Log.d(TAG, "✅ Emitting ${sliderItems.size} slider items (newest users first)")
            trySend(sliderItems)
        }
        
        fun processAndEmitItems() {
            if (!productsLoaded || !rentLoaded) return
            
            Log.d(TAG, "Processing ${allPostsMap.size} total posts for slider")
            
            // Group all posts by userId
            val postsByUser = allPostsMap.values.groupBy { it.first }
            
            Log.d(TAG, "Grouped into ${postsByUser.size} users")
            
            // For each user, get their listings sorted by timestamp (newest first)
            // Take max 5 listings per user
            val userListings = postsByUser.mapValues { (_, posts) ->
                posts.map { it.second }
                    .sortedByDescending { it.timestamp }
                    .take(MAX_LISTINGS_PER_USER)
            }
            
            // Sort users by their latest post timestamp and take top 5 users
            val topUsers = userListings.entries
                .sortedByDescending { (_, listings) ->
                    listings.maxOfOrNull { it.timestamp } ?: 0L
                }
                .take(MAX_USERS_IN_SLIDER)
                .associate { it.key to it.value }
            
            Log.d(TAG, "Selected top ${topUsers.size} users for slider")
            
            // Fetch user data for top users
            val pendingFetches = topUsers.keys
            var fetchedCount = 0
            
            if (pendingFetches.isEmpty()) {
                trySend(emptyList())
                return
            }
            
            for (userId in pendingFetches) {
                // Check cache first
                if (userDataCache.containsKey(userId)) {
                    fetchedCount++
                    if (fetchedCount == pendingFetches.size) {
                        emitSliderItems(topUsers, userDataCache)
                    }
                } else {
                    // Fetch from Firebase
                    usersRef.child(userId).get().addOnSuccessListener { userSnapshot ->
                        if (userSnapshot.exists()) {
                            val username = userSnapshot.child("fullName").getValue(String::class.java) 
                                ?: userSnapshot.child("username").getValue(String::class.java) 
                                ?: "User"
                            val profilePictureUrl = userSnapshot.child("profilePicture").getValue(String::class.java) 
                                ?: userSnapshot.child("profilePic").getValue(String::class.java)
                                ?: ""
                            
                            userDataCache[userId] = Pair(username, profilePictureUrl)
                            Log.d(TAG, "Cached user data: $username ($userId)")
                        } else {
                            userDataCache[userId] = Pair("User", "")
                        }
                        
                        fetchedCount++
                        if (fetchedCount == pendingFetches.size) {
                            emitSliderItems(topUsers, userDataCache)
                        }
                    }.addOnFailureListener {
                        userDataCache[userId] = Pair("User", "")
                        fetchedCount++
                        if (fetchedCount == pendingFetches.size) {
                            emitSliderItems(topUsers, userDataCache)
                        }
                    }
                }
            }
        }
        
        // Listener for Products (sale/thrift items)
        val productsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "🔄 Products data changed: ${snapshot.childrenCount} items")
                
                // Clear old products from map
                allPostsMap.keys.removeAll { it.startsWith("product_") }
                
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
                        
                        if (listingId.isNotEmpty() && userId.isNotEmpty() && imageUrl.isNotEmpty() && status == "Available") {
                            val listing = ListingItem(
                                listingId = "product_$listingId",
                                imageUrl = imageUrl,
                                itemName = itemName,
                                price = price,
                                timestamp = timestamp
                            )
                            allPostsMap["product_$listingId"] = Pair(userId, listing)
                            Log.d(TAG, "✓ Added ${listingTypeStr} post: $listingId (${itemName})")
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
                Log.d(TAG, "🔄 Rent data changed: ${snapshot.childrenCount} items")
                
                // Clear old rent items from map
                allPostsMap.keys.removeAll { it.startsWith("rent_") }
                
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
                        
                        if (listingId.isNotEmpty() && userId.isNotEmpty() && imageUrl.isNotEmpty() && status == "Available") {
                            val listing = ListingItem(
                                listingId = "rent_$listingId",
                                imageUrl = imageUrl,
                                itemName = itemName,
                                price = price,
                                timestamp = timestamp
                            )
                            allPostsMap["rent_$listingId"] = Pair(userId, listing)
                            Log.d(TAG, "✓ Added rent post: $listingId (${itemName})")
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

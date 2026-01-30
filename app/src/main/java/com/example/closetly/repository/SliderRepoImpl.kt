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

    override fun getSliderItems(excludeUserId: String?): Flow<List<SliderItemModel>> = callbackFlow {
        val allPostsMap = mutableMapOf<String, Pair<String, ListingItem>>() // postId -> (userId, listing)
        val userDataCache = mutableMapOf<String, Pair<String, String>>() // userId -> (username, profilePic)
        val blockedUserIds = mutableSetOf<String>() // Blocked users list
        var productsLoaded = false
        var rentLoaded = false
        var blockedUsersLoaded = false
        
        fun emitSliderItems(
            userListingsMap: Map<String, List<ListingItem>>,
            userData: Map<String, Pair<String, String>>
        ) {
            val sliderItems = userListingsMap.mapNotNull { (userId, listings) ->
                val (username, profilePic) = userData[userId] ?: Pair("", "")
                val lastUpdated = listings.maxOfOrNull { it.timestamp } ?: 0L
                
                // Only create slider if user has profile picture, username, and listings
                if (profilePic.isNotEmpty() && username.isNotEmpty() && listings.isNotEmpty()) {
                    SliderItemModel(
                        userId = userId,
                        username = username,
                        profilePictureUrl = profilePic,
                        listings = listings,
                        totalListings = listings.size,
                        lastUpdated = lastUpdated
                    )
                } else {
                    null
                }
            }.sortedByDescending { it.lastUpdated }
            
            Log.d(TAG, "✅ Emitting ${sliderItems.size} slider items (newest users first)")
            trySend(sliderItems)
        }
        
        fun processAndEmitItems() {
            if (!productsLoaded || !rentLoaded || !blockedUsersLoaded) return
            
            Log.d(TAG, "Processing ${allPostsMap.size} total posts for slider")
            
            // Filter out excluded user's posts and blocked users
            val filteredPosts = if (excludeUserId != null) {
                allPostsMap.filterValues { it.first != excludeUserId && !blockedUserIds.contains(it.first) }
            } else {
                allPostsMap.filterValues { !blockedUserIds.contains(it.first) }
            }
            
            // Group all posts by userId
            val postsByUser = filteredPosts.values.groupBy { it.first }
            
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
        
        // Fetch blocked users list for current user (if logged in)
        if (excludeUserId != null) {
            usersRef.child(excludeUserId).child("blocked").get().addOnSuccessListener { blockedSnapshot ->
                if (blockedSnapshot.exists()) {
                    for (blockedChild in blockedSnapshot.children) {
                        blockedChild.key?.let { blockedUserIds.add(it) }
                    }
                    Log.d(TAG, "Loaded ${blockedUserIds.size} blocked users")
                } else {
                    Log.d(TAG, "No blocked users found")
                }
                blockedUsersLoaded = true
                processAndEmitItems()
            }.addOnFailureListener {
                Log.e(TAG, "Failed to load blocked users: ${it.message}")
                blockedUsersLoaded = true
                processAndEmitItems()
            }
        } else {
            // No current user, skip blocked users loading
            blockedUsersLoaded = true
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
                            "Rs. ${rentPriceDouble.toInt()}/day"
                        } else {
                            val priceDouble = child.child("price").getValue(Double::class.java) ?: 0.0
                            "Rs. ${priceDouble.toInt()}"
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
                        val price = "Rs. ${rentPriceDouble.toInt()}/day"
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
    
    /**
     * Get slider items ONLY from followed users.
     * Shows posts from users that the current user follows.
     * Limited to 5 items, sorted by newest first.
     * Real-time updates when new content is added.
     */
    override fun getFollowedUsersSliderItems(
        currentUserId: String,
        limit: Int
    ): Flow<List<SliderItemModel>> = callbackFlow {
        val allPostsMap = mutableMapOf<String, Pair<String, ListingItem>>() // postId -> (userId, listing)
        val userDataCache = mutableMapOf<String, Pair<String, String>>() // userId -> (username, profilePic)
        var followedUserIds = setOf<String>()
        var blockedUserIds = setOf<String>()
        var productsLoaded = false
        var rentLoaded = false
        var followingLoaded = false
        var blockedLoaded = false
        
        fun emitSliderItems(
            userListingsMap: Map<String, List<ListingItem>>,
            userData: Map<String, Pair<String, String>>
        ) {
            val sliderItems = userListingsMap.mapNotNull { (userId, listings) ->
                val (username, profilePic) = userData[userId] ?: Pair("", "")
                val lastUpdated = listings.maxOfOrNull { it.timestamp } ?: 0L
                
                // Only create slider if user has profile picture, username, and listings
                if (profilePic.isNotEmpty() && username.isNotEmpty() && listings.isNotEmpty()) {
                    SliderItemModel(
                        userId = userId,
                        username = username,
                        profilePictureUrl = profilePic,
                        listings = listings,
                        totalListings = listings.size,
                        lastUpdated = lastUpdated
                    )
                } else {
                    null
                }
            }.sortedByDescending { it.lastUpdated }.take(limit)
            
            Log.d(TAG, "✅ Emitting ${sliderItems.size} followed-users slider items")
            trySend(sliderItems)
        }
        
        fun processAndEmitItems() {
            if (!productsLoaded || !rentLoaded || !followingLoaded || !blockedLoaded) return
            
            Log.d(TAG, "Processing ${allPostsMap.size} posts for followed-users slider")
            Log.d(TAG, "Following ${followedUserIds.size} users")
            
            // Filter posts: only from followed users, not blocked
            val filteredPosts = allPostsMap.filterValues { (userId, _) ->
                followedUserIds.contains(userId) && !blockedUserIds.contains(userId)
            }
            
            if (filteredPosts.isEmpty()) {
                Log.d(TAG, "No posts from followed users found")
                trySend(emptyList())
                return
            }
            
            // Group all posts by userId
            val postsByUser = filteredPosts.values.groupBy { it.first }
            
            Log.d(TAG, "Found posts from ${postsByUser.size} followed users")
            
            // For each user, get their listings sorted by timestamp (newest first)
            val userListings = postsByUser.mapValues { (_, posts) ->
                posts.map { it.second }
                    .sortedByDescending { it.timestamp }
                    .take(MAX_LISTINGS_PER_USER)
            }
            
            // Sort users by their latest post timestamp and take top users
            val topUsers = userListings.entries
                .sortedByDescending { (_, listings) ->
                    listings.maxOfOrNull { it.timestamp } ?: 0L
                }
                .take(limit)
                .associate { it.key to it.value }
            
            // Fetch user data for top users
            val pendingFetches = topUsers.keys
            var fetchedCount = 0
            
            if (pendingFetches.isEmpty()) {
                trySend(emptyList())
                return
            }
            
            for (userId in pendingFetches) {
                if (userDataCache.containsKey(userId)) {
                    fetchedCount++
                    if (fetchedCount == pendingFetches.size) {
                        emitSliderItems(topUsers, userDataCache)
                    }
                } else {
                    usersRef.child(userId).get().addOnSuccessListener { userSnapshot ->
                        if (userSnapshot.exists()) {
                            val username = userSnapshot.child("fullName").getValue(String::class.java) 
                                ?: userSnapshot.child("username").getValue(String::class.java) 
                                ?: "User"
                            val profilePictureUrl = userSnapshot.child("profilePicture").getValue(String::class.java) 
                                ?: userSnapshot.child("profilePic").getValue(String::class.java)
                                ?: ""
                            
                            userDataCache[userId] = Pair(username, profilePictureUrl)
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
        
        // Listen for following changes
        val followingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                followedUserIds = snapshot.children.mapNotNull { it.key }.toSet()
                Log.d(TAG, "Following list updated: ${followedUserIds.size} users")
                followingLoaded = true
                processAndEmitItems()
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Following listener cancelled: ${error.message}")
            }
        }
        
        // Listen for blocked users
        val blockedListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blockedUserIds = snapshot.children.mapNotNull { it.key }.toSet()
                blockedLoaded = true
                processAndEmitItems()
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Blocked listener cancelled: ${error.message}")
            }
        }
        
        // Listener for Products
        val productsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "🔄 Products data changed for followed slider")
                
                allPostsMap.keys.filter { it.startsWith("product_") }.toList().forEach { allPostsMap.remove(it) }
                
                for (child in snapshot.children) {
                    try {
                        val listingId = child.key ?: ""
                        val userId = child.child("sellerId").getValue(String::class.java) ?: ""
                        val imageUrl = child.child("imageUrl").getValue(String::class.java) ?: ""
                        val itemName = child.child("title").getValue(String::class.java) ?: ""
                        val listingTypeStr = child.child("listingType").getValue(String::class.java) ?: "THRIFT"
                        
                        val price = if (listingTypeStr == "RENT") {
                            val rentPriceDouble = child.child("rentPricePerDay").getValue(Double::class.java) ?: 0.0
                            "$${rentPriceDouble.toInt()}/day"
                        } else {
                            val priceDouble = child.child("price").getValue(Double::class.java) ?: 0.0
                            "$${priceDouble.toInt()}"
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
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing product: ${e.message}")
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
                Log.d(TAG, "🔄 Rent data changed for followed slider")
                
                allPostsMap.keys.filter { it.startsWith("rent_") }.toList().forEach { allPostsMap.remove(it) }
                
                for (child in snapshot.children) {
                    try {
                        val listingId = child.key ?: ""
                        val userId = child.child("sellerId").getValue(String::class.java) 
                            ?: child.child("ownerId").getValue(String::class.java)
                            ?: child.child("userId").getValue(String::class.java) ?: ""
                        val imageUrl = child.child("imageUrl").getValue(String::class.java) ?: ""
                        val itemName = child.child("title").getValue(String::class.java) ?: ""
                        val rentPriceDouble = child.child("rentPricePerDay").getValue(Double::class.java) 
                            ?: child.child("rentPrice").getValue(Double::class.java) 
                            ?: child.child("price").getValue(Double::class.java) ?: 0.0
                        val price = "$${rentPriceDouble.toInt()}/day"
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
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing rent item: ${e.message}")
                    }
                }
                
                rentLoaded = true
                processAndEmitItems()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Rent listener cancelled: ${error.message}")
            }
        }
        
        // Attach all listeners
        usersRef.child(currentUserId).child("following").addValueEventListener(followingListener)
        usersRef.child(currentUserId).child("blocked").addValueEventListener(blockedListener)
        productsRef.addValueEventListener(productsListener)
        rentRef.addValueEventListener(rentListener)
        
        Log.d(TAG, "Firebase listeners attached for followed-users slider")
        
        awaitClose {
            Log.d(TAG, "Removing Firebase listeners for followed-users slider")
            usersRef.child(currentUserId).child("following").removeEventListener(followingListener)
            usersRef.child(currentUserId).child("blocked").removeEventListener(blockedListener)
            productsRef.removeEventListener(productsListener)
            rentRef.removeEventListener(rentListener)
        }
    }
}

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
 * 1. Fetches all listings (posts) from Firebase in real-time
 * 2. Groups listings by userId (so each user gets one slider)
 * 3. Each slider shows user's profile picture as background
 * 4. Multiple listing cards (images, names, prices) shown inside each slider
 * 5. Automatically updates when any user posts a new listing
 */
class SliderRepoImpl : SliderRepo {
    
    private val database = FirebaseDatabase.getInstance()
    private val listingsRef = database.getReference("Products")  // Read from Products node where listings are stored
    private val usersRef = database.getReference("Users")
    
    companion object {
        private const val TAG = "SliderRepoImpl"
        private const val MAX_LISTINGS_PER_USER = 3  // Show max 3 listing cards per slider
        private const val MAX_USERS_IN_SLIDER = 10   // Show max 10 users in slider
    }
    
    /**
     * Returns a Flow that emits slider items grouped by user in real-time.
     * 
     * HOW IT WORKS:
     * - Listens to Firebase "posts" (or "listings") collection
     * - When ANY listing is added/updated/deleted, this triggers
     * - Groups all listings by userId
     * - For each user, fetches their profile picture from users collection
     * - Creates one SliderItemModel per user with their listings
     * - Sorts users by their latest listing timestamp (most recent first)
     * - Returns top 10 active users
     */
    override fun getSliderItems(): Flow<List<SliderItemModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Firebase data changed. Total listings: ${snapshot.childrenCount}")
                
                // Step 1: Parse all listings from Firebase
                val allListings = mutableListOf<Triple<String, String, ListingItem>>()
                // Triple = (userId, username, ListingItem)
                
                for (child in snapshot.children) {
                    try {
                        val listingId = child.key ?: ""
                        // Map ProductModel fields to our expected fields
                        val userId = child.child("sellerId").getValue(String::class.java) ?: ""
                        val username = child.child("sellerName").getValue(String::class.java) ?: ""
                        val imageUrl = child.child("imageUrl").getValue(String::class.java) ?: ""
                        val itemName = child.child("title").getValue(String::class.java) ?: ""
                        val priceDouble = child.child("price").getValue(Double::class.java) ?: 0.0
                        val price = "₹${priceDouble.toInt()}"  // Format price
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                        val status = child.child("status").getValue(String::class.java) ?: "Available"
                        
                        Log.d(TAG, "Processing listing: id=$listingId, user=$username, userId=$userId, status=$status")
                        
                        // Only include available listings with valid data
                        if (listingId.isNotEmpty() && userId.isNotEmpty() && imageUrl.isNotEmpty() && status == "Available") {
                            val listing = ListingItem(
                                listingId = listingId,
                                imageUrl = imageUrl,
                                itemName = itemName,
                                price = price,
                                timestamp = timestamp
                            )
                            allListings.add(Triple(userId, username, listing))
                            Log.d(TAG, "✓ Added listing from user: $username ($userId)")
                        } else {
                            Log.w(TAG, "✗ Skipped listing: missing data or unavailable (status=$status)")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing listing: ${e.message}", e)
                    }
                }
                
                // Step 2: Group listings by userId
                val listingsByUser = allListings.groupBy { it.first }
                Log.d(TAG, "Grouped listings into ${listingsByUser.size} users")
                
                // Step 3: Create SliderItemModel for each user with proper user data from Users node
                val sliderItems = mutableListOf<SliderItemModel>()
                
                for ((userId, userListings) in listingsByUser) {
                    try {
                        // Get listings (sorted by timestamp, take latest 3)
                        val listings = userListings
                            .map { it.third }
                            .sortedByDescending { it.timestamp }
                            .take(MAX_LISTINGS_PER_USER)
                        
                        // Get latest timestamp for sorting users
                        val lastUpdated = listings.maxOfOrNull { it.timestamp } ?: 0L
                        
                        // Fetch user data from Users node synchronously
                        usersRef.child(userId).get().addOnSuccessListener { userSnapshot ->
                            if (userSnapshot.exists()) {
                                val username = userSnapshot.child("fullName").getValue(String::class.java) 
                                    ?: userSnapshot.child("username").getValue(String::class.java) 
                                    ?: "User"
                                val profilePictureUrl = userSnapshot.child("profilePicture").getValue(String::class.java) 
                                    ?: userSnapshot.child("profilePic").getValue(String::class.java)
                                    ?: ""
                                
                                Log.d(TAG, "Fetched user data: username=$username, profile=${profilePictureUrl.take(50)}")
                                
                                // Create slider item with proper user data
                                if (listings.isNotEmpty()) {
                                    val sliderItem = SliderItemModel(
                                        userId = userId,
                                        username = username,
                                        profilePictureUrl = profilePictureUrl,
                                        listings = listings,
                                        totalListings = userListings.size,
                                        lastUpdated = lastUpdated
                                    )
                                    
                                    // Add to list and re-emit sorted items
                                    sliderItems.add(sliderItem)
                                    
                                    // Sort and emit immediately so UI updates as each user loads
                                    val sortedItems = sliderItems
                                        .sortedByDescending { it.lastUpdated }
                                        .take(MAX_USERS_IN_SLIDER)
                                    
                                    trySend(sortedItems)
                                }
                            } else {
                                Log.w(TAG, "User $userId not found in Users node")
                            }
                        }.addOnFailureListener { e ->
                            Log.e(TAG, "Error fetching user $userId: ${e.message}", e)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating slider for user $userId: ${e.message}", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase listener cancelled: ${error.message}")
                close(error.toException())
            }
        }
        
        // Attach the real-time listener
        listingsRef.addValueEventListener(listener)
        Log.d(TAG, "Firebase listener attached for real-time updates")
        
        // Clean up listener when Flow is cancelled
        awaitClose {
            Log.d(TAG, "Removing Firebase listener")
            listingsRef.removeEventListener(listener)
        }
    }
}

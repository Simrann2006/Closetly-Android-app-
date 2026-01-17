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
    private val listingsRef = database.getReference("posts")  // or "listings" if you have separate node
    private val usersRef = database.getReference("users")
    
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
                        val userId = child.child("userId").getValue(String::class.java) ?: ""
                        val username = child.child("username").getValue(String::class.java) ?: ""
                        val imageUrl = child.child("imageUrl").getValue(String::class.java) ?: ""
                        val itemName = child.child("itemName").getValue(String::class.java) ?: ""
                        val price = child.child("price").getValue(String::class.java) ?: ""
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                        val isActive = child.child("isActive").getValue(Boolean::class.java) ?: true
                        
                        // Only include active listings with valid data
                        if (listingId.isNotEmpty() && userId.isNotEmpty() && imageUrl.isNotEmpty() && isActive) {
                            val listing = ListingItem(
                                listingId = listingId,
                                imageUrl = imageUrl,
                                itemName = itemName,
                                price = price,
                                timestamp = timestamp
                            )
                            allListings.add(Triple(userId, username, listing))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing listing: ${e.message}", e)
                    }
                }
                
                // Step 2: Group listings by userId
                val listingsByUser = allListings.groupBy { it.first }
                Log.d(TAG, "Grouped listings into ${listingsByUser.size} users")
                
                // Step 3: Create SliderItemModel for each user
                val sliderItems = mutableListOf<SliderItemModel>()
                
                for ((userId, userListings) in listingsByUser) {
                    try {
                        // Get username (same for all listings of this user)
                        val username = userListings.firstOrNull()?.second ?: ""
                        
                        // Get listings (sorted by timestamp, take latest 3)
                        val listings = userListings
                            .map { it.third }
                            .sortedByDescending { it.timestamp }
                            .take(MAX_LISTINGS_PER_USER)
                        
                        // Get latest timestamp for sorting users
                        val lastUpdated = listings.maxOfOrNull { it.timestamp } ?: 0L
                        
                        // Fetch user's profile picture (from users collection or from listing data)
                        // For now, we'll use a placeholder - you can fetch from users node
                        var profilePictureUrl = ""
                        
                        // Try to get profile pic from users collection
                        usersRef.child(userId).child("profilePicture")
                            .get()
                            .addOnSuccessListener { profileSnapshot ->
                                profilePictureUrl = profileSnapshot.getValue(String::class.java) ?: ""
                            }
                        
                        // If not found, try to get from first listing's profilePictureUrl field
                        if (profilePictureUrl.isEmpty()) {
                            snapshot.children.find { 
                                it.child("userId").getValue(String::class.java) == userId 
                            }?.let { listingSnapshot ->
                                profilePictureUrl = listingSnapshot.child("profilePictureUrl")
                                    .getValue(String::class.java) ?: ""
                            }
                        }
                        
                        // Create slider item
                        if (listings.isNotEmpty()) {
                            sliderItems.add(
                                SliderItemModel(
                                    userId = userId,
                                    username = username,
                                    profilePictureUrl = profilePictureUrl,
                                    listings = listings,
                                    totalListings = userListings.size,
                                    lastUpdated = lastUpdated
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating slider for user $userId: ${e.message}", e)
                    }
                }
                
                // Step 4: Sort by latest activity and limit
                val sortedItems = sliderItems
                    .sortedByDescending { it.lastUpdated }
                    .take(MAX_USERS_IN_SLIDER)
                
                Log.d(TAG, "Emitting ${sortedItems.size} slider items (grouped by user)")
                
                // Emit the data - this will trigger UI update
                trySend(sortedItems)
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

// ====================================================================
// QUICK REFERENCE: Adding New Posts to Firebase Slider
// ====================================================================

// Method 1: From your Android app (Add this to PostCreationActivity or similar)
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import android.util.Log

class SliderHelper {
    
    companion object {
        /**
         * Add a post to Firebase that will automatically appear in the slider
         * @param username: User's display name
         * @param profilePicUrl: URL of user's profile picture (from Firebase Storage)
         * @param postImageUrl: URL of the post image (from Firebase Storage)
         * @param caption: Post caption/description
         * @param itemName: Optional - name of item shown in post
         * @param price: Optional - price of item
         */
        fun addPostToSlider(
            username: String,
            profilePicUrl: String,
            postImageUrl: String,
            caption: String,
            itemName: String = "",
            price: String = "",
            onSuccess: () -> Unit = {},
            onFailure: (Exception) -> Unit = {}
        ) {
            val database = FirebaseDatabase.getInstance()
            val currentUser = FirebaseAuth.getInstance().currentUser
            
            if (currentUser == null) {
                onFailure(Exception("User not authenticated"))
                return
            }
            
            val postId = database.reference.child("posts").push().key ?: run {
                onFailure(Exception("Failed to generate post ID"))
                return
            }
            
            val postData = hashMapOf(
                "postId" to postId,
                "userId" to currentUser.uid,
                "username" to username,
                "profilePictureUrl" to profilePicUrl,
                "imageUrl" to postImageUrl,
                "caption" to caption,
                "itemName" to itemName,
                "price" to price,
                "timestamp" to System.currentTimeMillis(),
                "likesCount" to 0,
                "commentsCount" to 0,
                "isActive" to true // Set to false to remove from slider
            )
            
            database.reference
                .child("posts")
                .child(postId)
                .setValue(postData)
                .addOnSuccessListener {
                    Log.d("SliderHelper", "Post added to slider successfully: $postId")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("SliderHelper", "Failed to add post to slider", e)
                    onFailure(e)
                }
        }
        
        /**
         * Upload image to Firebase Storage and get download URL
         */
        fun uploadImageAndAddPost(
            imageUri: android.net.Uri,
            username: String,
            profilePicUrl: String,
            caption: String,
            itemName: String = "",
            price: String = "",
            onSuccess: () -> Unit = {},
            onFailure: (Exception) -> Unit = {}
        ) {
            val storage = FirebaseStorage.getInstance()
            val currentUser = FirebaseAuth.getInstance().currentUser
            
            if (currentUser == null) {
                onFailure(Exception("User not authenticated"))
                return
            }
            
            val postId = FirebaseDatabase.getInstance().reference.child("posts").push().key ?: run {
                onFailure(Exception("Failed to generate post ID"))
                return
            }
            
            val imageRef = storage.reference.child("posts/${currentUser.uid}/$postId.jpg")
            
            imageRef.putFile(imageUri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                }
                .addOnSuccessListener { downloadUri ->
                    addPostToSlider(
                        username = username,
                        profilePicUrl = profilePicUrl,
                        postImageUrl = downloadUri.toString(),
                        caption = caption,
                        itemName = itemName,
                        price = price,
                        onSuccess = onSuccess,
                        onFailure = onFailure
                    )
                }
                .addOnFailureListener { e ->
                    Log.e("SliderHelper", "Failed to upload image", e)
                    onFailure(e)
                }
        }
        
        /**
         * Remove a post from slider (sets isActive to false)
         */
        fun removeFromSlider(postId: String, onComplete: (Boolean) -> Unit = {}) {
            FirebaseDatabase.getInstance()
                .reference
                .child("posts")
                .child(postId)
                .child("isActive")
                .setValue(false)
                .addOnSuccessListener {
                    Log.d("SliderHelper", "Post removed from slider: $postId")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e("SliderHelper", "Failed to remove post from slider", e)
                    onComplete(false)
                }
        }
        
        /**
         * Update post engagement (likes/comments)
         */
        fun updatePostEngagement(
            postId: String,
            likesCount: Int? = null,
            commentsCount: Int? = null
        ) {
            val updates = hashMapOf<String, Any>()
            likesCount?.let { updates["likesCount"] = it }
            commentsCount?.let { updates["commentsCount"] = it }
            
            if (updates.isNotEmpty()) {
                FirebaseDatabase.getInstance()
                    .reference
                    .child("posts")
                    .child(postId)
                    .updateChildren(updates)
                    .addOnSuccessListener {
                        Log.d("SliderHelper", "Post engagement updated: $postId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("SliderHelper", "Failed to update engagement", e)
                    }
            }
        }
    }
}

// ====================================================================
// USAGE EXAMPLES
// ====================================================================

// Example 1: Add post with existing image URLs
fun exampleAddPostWithUrls() {
    SliderHelper.addPostToSlider(
        username = "kendall",
        profilePicUrl = "https://example.com/profile.jpg",
        postImageUrl = "https://example.com/post.jpg",
        caption = "Summer vibes! 🌞",
        itemName = "Denim Jacket",
        price = "Rs.899",
        onSuccess = {
            Toast.makeText(this, "Post added to slider!", Toast.LENGTH_SHORT).show()
        },
        onFailure = { e ->
            Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    )
}

// Example 2: Upload image and add to slider
fun exampleUploadAndAddPost(imageUri: Uri) {
    SliderHelper.uploadImageAndAddPost(
        imageUri = imageUri,
        username = "simran02",
        profilePicUrl = "https://example.com/profile_simran.jpg",
        caption = "New collection drop! ✨",
        itemName = "White Dress",
        price = "Rs.799",
        onSuccess = {
            Toast.makeText(this, "Post uploaded and added!", Toast.LENGTH_SHORT).show()
        },
        onFailure = { e ->
            Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    )
}

// Example 3: Remove post from slider
fun exampleRemovePost() {
    SliderHelper.removeFromSlider("post_001") { success ->
        if (success) {
            Toast.makeText(this, "Removed from slider", Toast.LENGTH_SHORT).show()
        }
    }
}

// Example 4: Update engagement
fun exampleUpdateEngagement() {
    SliderHelper.updatePostEngagement(
        postId = "post_001",
        likesCount = 250,
        commentsCount = 35
    )
}

// ====================================================================
// INTEGRATION WITH EXISTING CODE
// ====================================================================

// In your PostCreationActivity, after uploading a post:
class PostCreationActivity : AppCompatActivity() {
    
    private fun onPostCreated(
        postId: String,
        imageUrl: String,
        caption: String,
        itemName: String,
        price: String
    ) {
        // Get current user data
        val currentUser = FirebaseAuth.getInstance().currentUser
        val username = // Get from your user profile
        val profilePicUrl = // Get from your user profile
        
        // Add to slider automatically
        SliderHelper.addPostToSlider(
            username = username,
            profilePicUrl = profilePicUrl,
            postImageUrl = imageUrl,
            caption = caption,
            itemName = itemName,
            price = price,
            onSuccess = {
                Log.d("Post", "Post added to home slider")
                // Continue with normal flow
            },
            onFailure = { e ->
                Log.e("Post", "Failed to add to slider", e)
                // Post still exists, just not in slider
            }
        )
    }
}

// ====================================================================
// FIREBASE CONSOLE - MANUAL TESTING
// ====================================================================

/*
Go to: https://console.firebase.google.com/
Select: Your Closetly project
Navigate: Realtime Database

Add this JSON manually for testing:

{
  "posts": {
    "test_post_001": {
      "postId": "test_post_001",
      "userId": "test_user_123",
      "username": "test_user",
      "profilePictureUrl": "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
      "imageUrl": "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800",
      "caption": "Test post - This should appear in slider immediately!",
      "itemName": "Test Item",
      "price": "Rs.999",
      "timestamp": 1705500000000,
      "likesCount": 0,
      "commentsCount": 0,
      "isActive": true
    }
  }
}

After adding, check your app's HomeScreen - it should appear in slider within 1-2 seconds!
*/

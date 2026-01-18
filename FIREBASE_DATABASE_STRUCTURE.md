# Firebase Realtime Database Structure for Instagram-Style Home Screen

This document describes the complete Firebase Realtime Database structure required for the Instagram-style home screen with auto-slider functionality.

## Overview

The home screen fetches data from multiple Firebase nodes:
- **Posts**: Social media posts from users
- **Products**: Product listings (thrift/rent items)
- **Users**: User profile information
- **Comments**: Post/product comments
- **Likes**: Post engagement data

All data updates in **real-time** - when any user posts a new item, it appears instantly on all connected devices.

---

## 1. Users Node

Stores user profile information displayed throughout the app.

```
Users/
  {userId}/
    userId: "user123"
    username: "johndoe"
    fullName: "John Doe"
    email: "john@example.com"
    phoneNumber: "+1234567890"
    profilePicture: "https://firebasestorage.googleapis.com/..."
    bio: "Fashion enthusiast"
    fcmToken: "device_token_here"
    
    followers/
      {followerId}: true
      
    following/
      {followingId}: true
      
    saved/
      {postId}: true
```

**Key Fields:**
- `userId` (String): Unique user identifier
- `username` (String): Display name shown on posts
- `profilePicture` (String): URL to user's profile image
- `followers`, `following`: User relationships
- `saved`: Bookmarked posts/products

---

## 2. Posts Node

Social media posts (images with captions, similar to Instagram).

```
Posts/
  {postId}/
    postId: "post_12345"
    userId: "user123"
    username: "johndoe"
    userProfilePic: "https://firebasestorage.googleapis.com/..."
    imageUrl: "https://firebasestorage.googleapis.com/post_image.jpg"
    caption: "Check out my new outfit! #fashion"
    timestamp: 1705621234567
    postType: "post"
    likesCount: 0
    commentsCount: 0
    
    images: [
      "https://firebasestorage.googleapis.com/image1.jpg",
      "https://firebasestorage.googleapis.com/image2.jpg"
    ]
    
    likes/
      {userId}: true
```

**Key Fields:**
- `postId` (String): Unique post identifier
- `userId` (String): Author's user ID
- `username` (String): Author's display name
- `userProfilePic` (String): Author's profile picture URL
- `imageUrl` (String): Main post image
- `caption` (String): Post text/description
- `timestamp` (Long): Unix timestamp in milliseconds
- `postType` (String): "post" or "product"
- `likes/{userId}`: Map of users who liked this post

---

## 3. Products Node

Product listings for thrift/rent marketplace.

```
Products/
  {productId}/
    id: "prod_67890"
    title: "Vintage Denim Jacket"
    description: "Barely worn, great condition"
    price: 1500.0
    imageUrl: "https://firebasestorage.googleapis.com/product_image.jpg"
    size: "M"
    brand: "Levi's"
    condition: "Like New"
    listingType: "THRIFT"
    status: "Available"
    
    sellerId: "user123"
    sellerName: "johndoe"
    sellerProfilePic: "https://firebasestorage.googleapis.com/..."
    
    timestamp: 1705621234567
    rentPricePerDay: null
```

**Key Fields:**
- `id` (String): Unique product identifier
- `title` (String): Product name
- `description` (String): Product details
- `price` (Double): Product price in currency
- `imageUrl` (String): Product image URL
- `sellerId` (String): Seller's user ID
- `sellerName` (String): Seller's username
- `sellerProfilePic` (String): Seller's profile picture
- `timestamp` (Long): When product was listed
- `status` (String): "Available", "Sold", or "Rented"

---

## 4. Comments Node

Comments on posts and products.

```
Comments/
  {commentId}/
    commentId: "comment_123"
    postId: "post_12345"
    userId: "user456"
    username: "janedoe"
    userProfilePic: "https://..."
    text: "Love this!"
    timestamp: 1705621234567
```

---

## 5. Netflix-Style Auto-Slider Data Source

The auto-slider at the top of the home screen is **automatically generated** from the **Products node**.

### How It Works:

1. **Fetches all products** from `Products/` node
2. **Groups by user** (sellerId) - each user gets one slider card
3. **Background image**: User's profile picture (`sellerProfilePic`)
4. **Foreground cards**: Up to 3 latest products from that user
5. **Real-time updates**: When a user posts a new product, their slider card updates instantly
6. **Auto-scroll**: Automatically cycles through users every 3 seconds

### Example Slider Data Flow:

```
Products/
  prod1/
    sellerId: "user123"
    sellerName: "johndoe"
    sellerProfilePic: "https://profile1.jpg"
    imageUrl: "https://item1.jpg"
    title: "Blue Jeans"
    price: 800.0
    timestamp: 1705621234567
    
  prod2/
    sellerId: "user123"
    sellerName: "johndoe"
    sellerProfilePic: "https://profile1.jpg"
    imageUrl: "https://item2.jpg"
    title: "White Shirt"
    price: 500.0
    timestamp: 1705621244567
    
  prod3/
    sellerId: "user456"
    sellerName: "janedoe"
    sellerProfilePic: "https://profile2.jpg"
    imageUrl: "https://item3.jpg"
    title: "Red Dress"
    price: 1200.0
    timestamp: 1705621254567
```

**Results in slider:**

**Slider Card 1** (user123 - johndoe):
- Background: profile1.jpg
- Username: "johndoe"
- Listings count: "2 listings"
- Product cards: Blue Jeans (₹800), White Shirt (₹500)

**Slider Card 2** (user456 - janedoe):
- Background: profile2.jpg
- Username: "janedoe"
- Listings count: "1 listing"
- Product cards: Red Dress (₹1200)

---

## Real-Time Updates

### How Real-Time Works:

1. **Firebase ValueEventListener**: Attached to `Posts/` and `Products/` nodes
2. **Instant propagation**: When data changes in Firebase, all connected devices receive updates within milliseconds
3. **Automatic UI refresh**: StateFlow/LiveData automatically triggers recomposition

### Example Scenarios:

#### Scenario 1: User posts a new product
```
User A posts "Leather Jacket" (₹2000) → 
Firebase adds to Products/ → 
All devices receive update → 
Home feed shows new product instantly → 
Slider adds/updates User A's card
```

#### Scenario 2: User deletes a product
```
User B deletes "Old Shoes" → 
Firebase removes from Products/ → 
All devices receive update → 
Product disappears from home feed → 
Slider removes product card (or entire slider if last product)
```

#### Scenario 3: User likes a post
```
User C likes a post → 
Firebase updates Posts/{postId}/likes/{userId} → 
All devices see like count increase → 
UI shows red heart icon
```

---

## Code Architecture

### Data Flow:

```
Firebase Realtime Database
    ↓
SliderRepoImpl (listens to Products/)
HomePostRepoImpl (listens to Posts/ + Products/)
    ↓
SliderViewModel (manages slider state)
HomeViewModel (manages posts state)
    ↓
HomeScreen (Composable UI)
    ↓
User sees Instagram-style feed with auto-slider
```

### Key Components:

1. **SliderRepoImpl**: Fetches products, groups by user, creates slider cards
2. **HomePostRepoImpl**: Fetches posts and products, combines into unified feed
3. **SliderViewModel**: Provides `StateFlow<List<SliderItemModel>>`
4. **HomeViewModel**: Provides `StateFlow<List<PostUI>>`
5. **HomeScreen**: Observes StateFlows, renders UI automatically

---

## Setup Instructions

### 1. Enable Firebase Realtime Database

1. Go to Firebase Console → Realtime Database
2. Create database in test mode (or production with rules)
3. Note your database URL

### 2. Set Firebase Rules

```json
{
  "rules": {
    "Users": {
      ".read": "auth != null",
      ".write": "auth != null",
      "$userId": {
        ".write": "$userId === auth.uid"
      }
    },
    "Posts": {
      ".read": true,
      ".write": "auth != null"
    },
    "Products": {
      ".read": true,
      ".write": "auth != null"
    },
    "Comments": {
      ".read": true,
      ".write": "auth != null"
    }
  }
}
```

### 3. Sample Data Structure

Add sample data to test the home screen:

```json
{
  "Users": {
    "user123": {
      "userId": "user123",
      "username": "fashionista_sarah",
      "fullName": "Sarah Johnson",
      "profilePicture": "https://picsum.photos/200?random=1",
      "bio": "Fashion lover | Thrift enthusiast"
    },
    "user456": {
      "userId": "user456",
      "username": "style_guru_mike",
      "fullName": "Mike Chen",
      "profilePicture": "https://picsum.photos/200?random=2",
      "bio": "Vintage collector"
    }
  },
  "Products": {
    "prod1": {
      "id": "prod1",
      "title": "Vintage Leather Jacket",
      "description": "Classic brown leather jacket in excellent condition",
      "price": 2500.0,
      "imageUrl": "https://picsum.photos/400/600?random=10",
      "sellerId": "user123",
      "sellerName": "fashionista_sarah",
      "sellerProfilePic": "https://picsum.photos/200?random=1",
      "status": "Available",
      "timestamp": 1705621234567
    },
    "prod2": {
      "id": "prod2",
      "title": "Blue Denim Jeans",
      "description": "Comfortable fit, barely worn",
      "price": 800.0,
      "imageUrl": "https://picsum.photos/400/600?random=11",
      "sellerId": "user123",
      "sellerName": "fashionista_sarah",
      "sellerProfilePic": "https://picsum.photos/200?random=1",
      "status": "Available",
      "timestamp": 1705621244567
    },
    "prod3": {
      "id": "prod3",
      "title": "Floral Summer Dress",
      "description": "Perfect for summer parties",
      "price": 1200.0,
      "imageUrl": "https://picsum.photos/400/600?random=12",
      "sellerId": "user456",
      "sellerName": "style_guru_mike",
      "sellerProfilePic": "https://picsum.photos/200?random=2",
      "status": "Available",
      "timestamp": 1705621254567
    }
  },
  "Posts": {
    "post1": {
      "postId": "post1",
      "userId": "user123",
      "username": "fashionista_sarah",
      "userProfilePic": "https://picsum.photos/200?random=1",
      "imageUrl": "https://picsum.photos/600/800?random=20",
      "caption": "Today's outfit inspiration! ✨ #OOTD",
      "timestamp": 1705621264567,
      "postType": "post"
    }
  }
}
```

---

## Testing Real-Time Updates

### Manual Test Steps:

1. **Launch app** → Home screen loads with slider and posts
2. **Open Firebase Console** → Add a new product manually
3. **Watch app** → New product appears in feed within 1-2 seconds
4. **Check slider** → User's slider card updates with new product
5. **Delete product** → Product disappears from feed instantly

### Automated Test:

```kotlin
// In Firebase Console or admin script
val newProduct = mapOf(
    "id" to "test_prod_${System.currentTimeMillis()}",
    "title" to "Test Product",
    "description" to "Testing real-time updates",
    "price" to 999.0,
    "imageUrl" to "https://picsum.photos/400/600",
    "sellerId" to "user123",
    "sellerName" to "fashionista_sarah",
    "sellerProfilePic" to "https://picsum.photos/200",
    "status" to "Available",
    "timestamp" to System.currentTimeMillis()
)

FirebaseDatabase.getInstance()
    .getReference("Products")
    .push()
    .setValue(newProduct)
    
// App should instantly show this product
```

---

## Performance Optimization

### Current Implementation:

- ✅ **Real-time listeners**: Efficient ValueEventListener
- ✅ **Coroutines Flow**: Non-blocking reactive streams
- ✅ **StateFlow**: Efficient state management
- ✅ **Lazy loading**: Only loads visible data
- ✅ **Image caching**: Coil library caches images

### Limits:

- Slider shows max **10 users**
- Each slider shows max **3 products per user**
- Products must have `status = "Available"`

---

## Troubleshooting

### Issue: Slider not showing

**Check:**
1. Products node exists in Firebase
2. Products have `status: "Available"`
3. Products have valid `sellerId`, `sellerName`, `sellerProfilePic`
4. Firebase rules allow read access

### Issue: Posts not updating

**Check:**
1. Posts node exists
2. Posts have valid `userId`, `username`, `userProfilePic`
3. Network connection active
4. No Firebase errors in Logcat

### Issue: Profile pictures not loading

**Check:**
1. URLs are valid and accessible
2. Firebase Storage rules allow public read
3. Internet permission in AndroidManifest.xml

---

## Summary

✅ **Instagram-style home feed**: Combines posts and products
✅ **Netflix-style auto-slider**: Shows users with their products
✅ **Real-time updates**: Instant synchronization across all devices
✅ **No UI changes**: Fits seamlessly into existing design
✅ **Profile navigation**: Tap slider to view user profile
✅ **Firebase-powered**: 100% data from Firebase Realtime Database

The system is fully operational and will update automatically whenever:
- A user posts a new product
- A user creates a new post
- A product is sold/deleted
- A user updates their profile picture

# Quick Start: Adding Sample Data to Firebase

## 🔥 Firebase Console Setup

### Step 1: Access Firebase Console

1. Go to: https://console.firebase.google.com
2. Select your project: **Closetly-Android-app**
3. Click **Realtime Database** in left sidebar
4. Click on your database URL

---

## 📝 Sample Data to Copy-Paste

### Option 1: Complete Sample Data (Recommended for Testing)

Copy this JSON and paste it in Firebase Console:

```json
{
  "Users": {
    "user_sarah_001": {
      "userId": "user_sarah_001",
      "username": "fashionista_sarah",
      "fullName": "Sarah Johnson",
      "email": "sarah@example.com",
      "profilePicture": "https://i.pravatar.cc/200?img=1",
      "bio": "Fashion lover | Thrift enthusiast 👗✨"
    },
    "user_mike_002": {
      "userId": "user_mike_002",
      "username": "vintage_mike",
      "fullName": "Mike Chen",
      "email": "mike@example.com",
      "profilePicture": "https://i.pravatar.cc/200?img=12",
      "bio": "Vintage collector | Sustainable fashion advocate 🌿"
    },
    "user_emma_003": {
      "userId": "user_emma_003",
      "username": "style_emma",
      "fullName": "Emma Davis",
      "email": "emma@example.com",
      "profilePicture": "https://i.pravatar.cc/200?img=5",
      "bio": "Minimalist wardrobe | Quality over quantity 🖤"
    }
  },
  "Products": {
    "prod_001": {
      "id": "prod_001",
      "title": "Vintage Leather Jacket",
      "description": "Classic brown leather jacket in excellent condition. Perfect for fall and winter.",
      "price": 2500.0,
      "imageUrl": "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600",
      "size": "M",
      "brand": "Levi's",
      "condition": "Like New",
      "listingType": "THRIFT",
      "status": "Available",
      "sellerId": "user_sarah_001",
      "sellerName": "fashionista_sarah",
      "sellerProfilePic": "https://i.pravatar.cc/200?img=1",
      "timestamp": 1705621234567
    },
    "prod_002": {
      "id": "prod_002",
      "title": "Blue Denim Jeans",
      "description": "Comfortable high-waist jeans, barely worn. True to size.",
      "price": 800.0,
      "imageUrl": "https://images.unsplash.com/photo-1542272604-787c3835535d?w=600",
      "size": "S",
      "brand": "H&M",
      "condition": "Excellent",
      "listingType": "THRIFT",
      "status": "Available",
      "sellerId": "user_sarah_001",
      "sellerName": "fashionista_sarah",
      "sellerProfilePic": "https://i.pravatar.cc/200?img=1",
      "timestamp": 1705621244567
    },
    "prod_003": {
      "id": "prod_003",
      "title": "Floral Summer Dress",
      "description": "Beautiful floral print dress, perfect for summer events.",
      "price": 1200.0,
      "imageUrl": "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=600",
      "size": "M",
      "brand": "Zara",
      "condition": "Good",
      "listingType": "THRIFT",
      "status": "Available",
      "sellerId": "user_mike_002",
      "sellerName": "vintage_mike",
      "sellerProfilePic": "https://i.pravatar.cc/200?img=12",
      "timestamp": 1705621254567
    },
    "prod_004": {
      "id": "prod_004",
      "title": "Black Leather Boots",
      "description": "Stylish ankle boots with minimal wear. Great quality.",
      "price": 1800.0,
      "imageUrl": "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=600",
      "size": "7",
      "brand": "Steve Madden",
      "condition": "Like New",
      "listingType": "THRIFT",
      "status": "Available",
      "sellerId": "user_mike_002",
      "sellerName": "vintage_mike",
      "sellerProfilePic": "https://i.pravatar.cc/200?img=12",
      "timestamp": 1705621264567
    },
    "prod_005": {
      "id": "prod_005",
      "title": "White Cotton T-Shirt",
      "description": "Basic white tee, never worn. Perfect wardrobe staple.",
      "price": 300.0,
      "imageUrl": "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600",
      "size": "L",
      "brand": "Uniqlo",
      "condition": "New",
      "listingType": "THRIFT",
      "status": "Available",
      "sellerId": "user_emma_003",
      "sellerName": "style_emma",
      "sellerProfilePic": "https://i.pravatar.cc/200?img=5",
      "timestamp": 1705621274567
    },
    "prod_006": {
      "id": "prod_006",
      "title": "Wool Winter Coat",
      "description": "Elegant grey wool coat. Keeps you warm and stylish.",
      "price": 3500.0,
      "imageUrl": "https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=600",
      "size": "M",
      "brand": "Massimo Dutti",
      "condition": "Excellent",
      "listingType": "THRIFT",
      "status": "Available",
      "sellerId": "user_emma_003",
      "sellerName": "style_emma",
      "sellerProfilePic": "https://i.pravatar.cc/200?img=5",
      "timestamp": 1705621284567
    }
  },
  "Posts": {
    "post_001": {
      "postId": "post_001",
      "userId": "user_sarah_001",
      "username": "fashionista_sarah",
      "userProfilePic": "https://i.pravatar.cc/200?img=1",
      "imageUrl": "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=800",
      "caption": "Today's outfit inspiration! Loving this vintage vibe ✨ #OOTD #Vintage",
      "timestamp": 1705621294567,
      "postType": "post"
    },
    "post_002": {
      "postId": "post_002",
      "userId": "user_mike_002",
      "username": "vintage_mike",
      "userProfilePic": "https://i.pravatar.cc/200?img=12",
      "imageUrl": "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=800",
      "caption": "Just added new pieces to my collection! Check out my listings 🔥 #ThriftShop",
      "timestamp": 1705621304567,
      "postType": "post"
    },
    "post_003": {
      "postId": "post_003",
      "userId": "user_emma_003",
      "username": "style_emma",
      "userProfilePic": "https://i.pravatar.cc/200?img=5",
      "imageUrl": "https://images.unsplash.com/photo-1445205170230-053b83016050?w=800",
      "caption": "Minimalist style = timeless elegance 🖤 #MinimalistFashion #Sustainable",
      "timestamp": 1705621314567,
      "postType": "post"
    }
  }
}
```

---

## 🚀 How to Add This Data

### Method 1: Import JSON (Easiest)

1. In Firebase Console, click the **⋮** (three dots menu)
2. Select **Import JSON**
3. Paste the JSON above
4. Click **Import**
5. Done! ✅

### Method 2: Manual Entry

1. Click **+** next to your database name
2. Name: `Users`
3. Click **+** under Users
4. Enter user data field by field
5. Repeat for Products and Posts

---

## 🧪 Testing Real-Time Updates

### Test 1: Add a New Product While App is Running

1. **Keep your app open** on Home Screen
2. Go to Firebase Console
3. Add this new product:

```json
{
  "id": "prod_test_realtime",
  "title": "🆕 REAL-TIME TEST - Red Hoodie",
  "description": "This should appear instantly in your app!",
  "price": 999.0,
  "imageUrl": "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=600",
  "size": "M",
  "status": "Available",
  "sellerId": "user_sarah_001",
  "sellerName": "fashionista_sarah",
  "sellerProfilePic": "https://i.pravatar.cc/200?img=1",
  "timestamp": 1705621324567
}
```

4. **Watch your app** - product should appear within 1-2 seconds!

### Test 2: Update Slider

1. Add another product for **user_mike_002**
2. Slider should update to show the new product card
3. No app restart needed!

---

## 🔍 Expected Results in App

After adding the sample data, you should see:

### Auto-Slider (Top of Home Screen):
1. **Slide 1**: Sarah (2 products - Leather Jacket, Denim Jeans)
2. **Slide 2**: Mike (2 products - Floral Dress, Leather Boots)
3. **Slide 3**: Emma (2 products - White T-Shirt, Wool Coat)

### Post Feed (Below Slider):
1. Emma's minimalist style post
2. Mike's thrift shop post
3. Sarah's outfit inspiration post
4. Sarah's Wool Coat product (₹3500)
5. Emma's White T-Shirt product (₹300)
6. Mike's Leather Boots product (₹1800)
7. Mike's Floral Dress product (₹1200)
8. Sarah's Denim Jeans product (₹800)
9. Sarah's Leather Jacket product (₹2500)

---

## 📊 Firebase Rules (Important!)

Make sure your Firebase rules allow reading data:

```json
{
  "rules": {
    "Users": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "Posts": {
      ".read": true,
      ".write": "auth != null"
    },
    "Products": {
      ".read": true,
      ".write": "auth != null"
    }
  }
}
```

To update rules:
1. Firebase Console → Realtime Database
2. Click **Rules** tab
3. Paste rules above
4. Click **Publish**

---

## 🆘 Troubleshooting

### "Permission Denied" Error

**Problem**: Firebase rules are too restrictive

**Solution**:
```json
{
  "rules": {
    ".read": true,
    ".write": "auth != null"
  }
}
```

⚠️ **Note**: This allows public read access. For production, use more specific rules.

---

### Images Not Loading

**Problem**: Image URLs are blocked or invalid

**Solution**: Use these reliable image sources:
- ✅ Unsplash: `https://images.unsplash.com/photo-...`
- ✅ Pravatar: `https://i.pravatar.cc/200?img=1`
- ✅ Picsum: `https://picsum.photos/400/600?random=1`

---

### Slider Empty

**Problem**: No products with `status: "Available"`

**Solution**: Check all products have:
```json
{
  "status": "Available",
  "sellerId": "valid_user_id",
  "sellerName": "username",
  "sellerProfilePic": "valid_url",
  "imageUrl": "valid_url"
}
```

---

## 📱 Alternative: Use Your Own Images

### Upload to Firebase Storage:

1. Firebase Console → Storage
2. Click **Upload file**
3. Upload your images
4. Copy the download URL
5. Use in Firebase Database

### Example:
```json
{
  "imageUrl": "https://firebasestorage.googleapis.com/v0/b/your-project.appspot.com/o/images%2Fjacket.jpg?alt=media&token=..."
}
```

---

## ✅ Verification Checklist

After adding data, verify:

- [ ] At least 3 users exist in `Users/` node
- [ ] At least 5 products exist in `Products/` node
- [ ] All products have `status: "Available"`
- [ ] All products have valid `sellerId` matching a user
- [ ] All image URLs are accessible (test in browser)
- [ ] Firebase rules allow reading data
- [ ] App shows slider with multiple users
- [ ] App shows products in feed with prices
- [ ] Real-time updates work when adding new data

---

## 🎉 Success!

If you see the slider auto-scrolling with user profiles and products in the feed, **congratulations!** Your Instagram-style home screen is working perfectly!

**Next**: Try adding a new product while the app is running and watch it appear instantly! 🚀

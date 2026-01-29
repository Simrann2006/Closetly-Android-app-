package com.example.closetly.view

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.R
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.repository.ProductRepoImpl
import com.example.closetly.repository.PostRepoImpl
import com.example.closetly.viewmodel.ProductViewModel
import com.example.closetly.viewmodel.PostViewModel
import com.example.closetly.model.ProductModel
import com.example.closetly.model.PostModel
import com.example.closetly.ui.theme.Background_Dark
import com.example.closetly.ui.theme.Background_Light
import com.example.closetly.ui.theme.Pink40
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.White
import com.example.closetly.ui.theme.Black
import com.example.closetly.ui.theme.DarkGrey
import com.example.closetly.ui.theme.Grey
import com.example.closetly.ui.theme.Light_grey
import com.example.closetly.utils.ThemeManager
import com.example.closetly.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val userRepo = remember { UserRepoImpl(context) }
    val userViewModel = remember { UserViewModel(userRepo) }
    val productRepo = remember { ProductRepoImpl() }
    val productViewModel = remember { ProductViewModel(productRepo) }
    val postRepo = remember { PostRepoImpl(context) }
    val postViewModel = remember { PostViewModel(postRepo) }
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profilePicture by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var userListings by remember { mutableStateOf<List<ProductModel>>(emptyList()) }
    var userPosts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var followersCount by remember { mutableStateOf<Int?>(null) }
    var followingCount by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.let { user ->
            userViewModel.getUserById(user.uid) { success, _, userData ->
                if (success && userData != null) {
                    name = userData.fullName
                    username = userData.username
                    bio = userData.bio
                    profilePicture = userData.profilePicture
                }
                isLoading = false
            }
            productViewModel.getUserProducts(user.uid) { listings ->
                userListings = listings
            }
            postViewModel.getUserPosts(user.uid) { posts ->
                userPosts = posts
            }
            
            userViewModel.getFollowersCount(user.uid) { count ->
                followersCount = count
            }
            
            userViewModel.getFollowingCount(user.uid) { count ->
                followingCount = count
            }
        } ?: run {
            isLoading = false
        }
    }

    val editProfileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentUser?.let { user ->
                userViewModel.getUserById(user.uid) { success, _, userData ->
                    if (success && userData != null) {
                        name = userData.fullName
                        username = userData.username
                        bio = userData.bio
                        profilePicture = userData.profilePicture
                    }
                }
                productViewModel.getUserProducts(user.uid) { listings ->
                    userListings = listings
                }
                postViewModel.getUserPosts(user.uid) { posts ->
                    userPosts = posts
                }
            }
        }
    }

    val selectedTab = remember { mutableStateOf("Posts") }
    val listState = rememberLazyListState()
    
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (ThemeManager.isDarkMode) Background_Dark else Background_Light),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Pink40)
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(if (ThemeManager.isDarkMode) Background_Dark else Background_Light)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(if (ThemeManager.isDarkMode) DarkGrey else Light_grey)
                ) {
                    if (profilePicture.isNotEmpty()) {
                        AsyncImage(
                            model = profilePicture,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_person_24),
                                contentDescription = null,
                                tint = Grey,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (ThemeManager.isDarkMode) White else Black
                )
                
                Text(
                    text = "@$username",
                    fontSize = 14.sp,
                    color = Grey
                )
                
                if (bio.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = bio,
                        fontSize = 14.sp,
                        color = if (ThemeManager.isDarkMode) White.copy(alpha = 0.9f) else Black,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat(
                        count = "${userPosts.size}",
                        label = "Posts",
                        onClick = {}
                    )
                    ProfileStat(
                        count = followersCount?.toString() ?: "0",
                        label = "Followers",
                        onClick = {
                            currentUser?.let { user ->
                                val intent = Intent(context, FollowersFollowingActivity::class.java).apply {
                                    putExtra("userId", user.uid)
                                    putExtra("username", username)
                                    putExtra("type", "followers")
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                    ProfileStat(
                        count = followingCount?.toString() ?: "0",
                        label = "Following",
                        onClick = {
                            currentUser?.let { user ->
                                val intent = Intent(context, FollowersFollowingActivity::class.java).apply {
                                    putExtra("userId", user.uid)
                                    putExtra("username", username)
                                    putExtra("type", "following")
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        editProfileLauncher.launch(
                            Intent(context, EditProfileActivity::class.java).apply {
                                putExtra("name", name)
                                putExtra("username", username)
                                putExtra("bio", bio)
                                putExtra("imageUri", profilePicture)
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brown
                    )
                ) {
                    Text(
                        "Edit Profile",
                        color = White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (ThemeManager.isDarkMode) Background_Dark else White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileTab(
                    text = "Posts",
                    selected = selectedTab.value == "Posts",
                    onClick = { selectedTab.value = "Posts" }
                )

                ProfileTab(
                    text = "Listings",
                    selected = selectedTab.value == "Listings",
                    onClick = { selectedTab.value = "Listings" }
                )
            }
        }

        val maxRows = maxOf(
            if (userPosts.isEmpty()) 1 else userPosts.chunked(3).size,
            if (userListings.isEmpty()) 1 else userListings.chunked(3).size
        )
        
        items(maxRows) { rowIndex ->
            when (selectedTab.value) {
                "Posts" -> {
                    if (userPosts.isEmpty() && rowIndex == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No posts yet",
                                fontSize = 16.sp,
                                color = Grey
                            )
                        }
                    } else if (rowIndex < userPosts.chunked(3).size) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 1.dp, vertical = 1.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            userPosts.chunked(3)[rowIndex].forEach { post ->
                                val index = userPosts.indexOf(post)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clickable {
                                            val intent = Intent(context, PostFeedActivity::class.java)
                                            intent.putExtra("USER_ID", currentUser?.uid)
                                            intent.putExtra("INITIAL_INDEX", index)
                                            context.startActivity(intent)
                                        }
                                ) {
                                    AsyncImage(
                                        model = post.imageUrl,
                                        contentDescription = post.caption,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            repeat(3 - userPosts.chunked(3)[rowIndex].size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 1.dp, vertical = 1.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            repeat(3) {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            }
                        }
                    }
                }
                "Listings" -> {
                    if (userListings.isEmpty() && rowIndex == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No listings yet",
                                fontSize = 16.sp,
                                color = Grey
                            )
                        }
                    } else if (rowIndex < userListings.chunked(3).size) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 1.dp, vertical = 1.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            userListings.chunked(3)[rowIndex].forEach { listing ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clickable {
                                            val intent = Intent(context, ListingViewerActivity::class.java).apply {
                                                putExtra("productId", listing.id)
                                            }
                                            context.startActivity(intent)
                                        }
                                ) {
                                    AsyncImage(
                                        model = listing.imageUrl,
                                        contentDescription = listing.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            repeat(3 - userListings.chunked(3)[rowIndex].size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 1.dp, vertical = 1.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            repeat(3) {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            onClick = onClick,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        )
    ) {
        Text(
            text = text,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp,
            color = if (ThemeManager.isDarkMode) White else Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(40.dp)
                .background(if (selected) Pink40 else Color.Transparent)
        )
    }
}

@Composable
fun ProfileStat(count: String, label: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            onClick = onClick,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        )
    ) {
        Text(
            text = count,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = if (com.example.closetly.utils.ThemeManager.isDarkMode) White else Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Grey
        )
    }
}
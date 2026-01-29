package com.example.closetly.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.R
import com.example.closetly.model.PostModel
import com.example.closetly.model.ProductModel
import com.example.closetly.repository.ChatRepoImpl
import com.example.closetly.repository.PostRepoImpl
import com.example.closetly.repository.ProductRepoImpl
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.utils.ThemeManager
import com.example.closetly.viewmodel.ChatViewModel
import com.example.closetly.viewmodel.PostViewModel
import com.example.closetly.viewmodel.ProductViewModel
import com.example.closetly.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class UserProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userId = intent.getStringExtra("userId") ?: ""
        val username = intent.getStringExtra("username") ?: ""

        setContent {
            UserProfielBody(userId = userId, initialUsername = username)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfielBody(userId: String, initialUsername: String) {

    val context = LocalContext.current
    val userRepo = remember { UserRepoImpl(context) }
    val userViewModel = remember { UserViewModel(userRepo) }
    val postRepo = remember { PostRepoImpl(context) }
    val postViewModel = remember { PostViewModel(postRepo) }
    val productRepo = remember { ProductRepoImpl() }
    val productViewModel = remember { ProductViewModel(productRepo) }
    val chatRepo = remember { ChatRepoImpl() }
    val chatViewModel = remember { ChatViewModel(chatRepo) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf(initialUsername) }
    var bio by remember { mutableStateOf("") }
    var profilePicture by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var existingChatId by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Posts", "Listings")
    var userPosts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var userListings by remember { mutableStateOf<List<ProductModel>>(emptyList()) }
    val listState = rememberLazyListState()

    var isFollowing by remember { mutableStateOf(false) }
    var followersCount by remember { mutableStateOf<Int?>(null) }
    var followingCount by remember { mutableStateOf<Int?>(null) }
    var theyFollowUs by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showUnblockDialog by remember { mutableStateOf(false) }
    var isBlocked by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            try {
                userViewModel.getUserById(userId) { success, _, userData ->
                    if (success && userData != null) {
                        name = userData.fullName
                        username = userData.username
                        bio = userData.bio
                        profilePicture = userData.profilePicture
                    }
                    isLoading = false
                }

                postViewModel.getUserPosts(userId) { posts ->
                    userPosts = posts
                }

                productViewModel.getUserProducts(userId) { products ->
                    userListings = products
                }

                if (currentUserId.isNotEmpty() && currentUserId != userId) {
                    userViewModel.isFollowing(currentUserId, userId) { following ->
                        isFollowing = following
                    }

                    userViewModel.isFollowing(userId, currentUserId) { theyFollow ->
                        theyFollowUs = theyFollow
                    }
                    
                    userViewModel.isUserBlocked(currentUserId, userId) { blocked ->
                        isBlocked = blocked
                    }
                }

                userViewModel.getFollowersCount(userId) { count ->
                    followersCount = count
                }

                userViewModel.getFollowingCount(userId) { count ->
                    followingCount = count
                }

                if (currentUserId.isNotEmpty() && currentUserId != userId) {
                    chatViewModel.getOrCreateChat(currentUserId, userId) { success, _, chatId ->
                        if (success && chatId != null) {
                            existingChatId = chatId
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = username,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (ThemeManager.isDarkMode) White else Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = if (ThemeManager.isDarkMode) White else Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_more_vert_24),
                            contentDescription = null,
                            tint = if (ThemeManager.isDarkMode) White else Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (ThemeManager.isDarkMode) Background_Dark else White,
                    titleContentColor = if (ThemeManager.isDarkMode) White else Black
                )
            )
        },
        containerColor = if (ThemeManager.isDarkMode) Background_Dark else White
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = if (ThemeManager.isDarkMode) White else Black)
            }
        } else {
            LazyColumn(
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
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
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(if (ThemeManager.isDarkMode) Surface_Dark else Light_grey)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (profilePicture.isNotEmpty()) {
                                        val intent = Intent(context, FullScreenImageActivity::class.java).apply {
                                            putExtra("IMAGE_URL", profilePicture)
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profilePicture.isNotEmpty()) {
                                AsyncImage(
                                    model = profilePicture,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_person_24),
                                    contentDescription = null,
                                    tint = Grey,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = name,
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ThemeManager.isDarkMode) White else Black
                            )
                        )

                        Text(
                            text = "@$username",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Grey
                            )
                        )

                        if (bio.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = bio,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = if (ThemeManager.isDarkMode) White else Black
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            UserProfileStat(
                                count = if (isBlocked) "0" else "${userPosts.size}",
                                label = "Posts",
                                onClick = {}
                            )
                            UserProfileStat(
                                count = if (isBlocked) "0" else (followersCount?.toString() ?: "0"),
                                label = "Followers",
                                onClick = {
                                    if (!isBlocked) {
                                        val intent = Intent(context, FollowersFollowingActivity::class.java).apply {
                                            putExtra("userId", userId)
                                            putExtra("username", username)
                                            putExtra("type", "followers")
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            )
                            UserProfileStat(
                                count = if (isBlocked) "0" else (followingCount?.toString() ?: "0"),
                                label = "Following",
                                onClick = {
                                    if (!isBlocked) {
                                        val intent = Intent(context, FollowersFollowingActivity::class.java).apply {
                                            putExtra("userId", userId)
                                            putExtra("username", username)
                                            putExtra("type", "following")
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isBlocked) {
                                        showUnblockDialog = true
                                    } else if (currentUserId.isNotEmpty() && currentUserId != userId) {
                                        userViewModel.toggleFollow(currentUserId, userId) { success, message ->
                                            if (success) {
                                                // Toggle the follow state immediately for UI responsiveness
                                                isFollowing = !isFollowing
                                                
                                                // Refresh the actual count from Firebase to ensure accuracy
                                                userViewModel.getFollowersCount(userId) { count ->
                                                    followersCount = count
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isBlocked) Brown else if (isFollowing) (if (ThemeManager.isDarkMode) Surface_Dark else Light_grey) else Brown
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                val buttonText = if (isBlocked) {
                                    "Unblock"
                                } else if (isFollowing) {
                                    "Following"
                                } else if (theyFollowUs) {
                                    "Follow Back"
                                } else {
                                    "Follow"
                                }

                                Text(
                                    buttonText,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBlocked) White else if (isFollowing) (if (ThemeManager.isDarkMode) White else Black) else White
                                    )
                                )
                            }

                            if (!isBlocked) {
                                Button(
                                    onClick = {
                                        val intent = Intent(context, ChatActivity::class.java).apply {
                                            putExtra("chatId", existingChatId)
                                            putExtra("otherUserId", userId)
                                            putExtra("otherUserName", username)
                                            putExtra("otherUserImage", profilePicture)
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (ThemeManager.isDarkMode) Surface_Dark else Light_grey
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "Message",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ThemeManager.isDarkMode) White else Black
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (!isBlocked) {
                    stickyHeader {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (ThemeManager.isDarkMode) Background_Dark else White)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            UserProfileTab(
                                text = "Posts",
                                selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 }
                            )
                            UserProfileTab(
                                text = "Listings",
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 }
                            )
                        }
                    }

                    val maxRows = maxOf(
                        if (userPosts.isEmpty()) 1 else userPosts.chunked(3).size,
                        if (userListings.isEmpty()) 1 else userListings.chunked(3).size
                    )

                    items(maxRows) { rowIndex ->
                        when (selectedTabIndex) {
                        0 -> {
                            if (userPosts.isEmpty() && rowIndex == 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No posts yet",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            color = if (ThemeManager.isDarkMode) Grey else Grey
                                        )
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
                                        val postIndex = userPosts.indexOf(post)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clickable {
                                                    val intent = Intent(context, PostFeedActivity::class.java).apply {
                                                        putExtra("USER_ID", userId)
                                                        putExtra("INITIAL_INDEX", postIndex)
                                                    }
                                                    context.startActivity(intent)
                                                }
                                        ) {
                                            AsyncImage(
                                                model = post.imageUrl,
                                                contentDescription = null,
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
                        1 -> {
                            if (userListings.isEmpty() && rowIndex == 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No listings yet",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            color = if (ThemeManager.isDarkMode) Grey else Grey
                                        )
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
                                                contentDescription = null,
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
        }
    }
    
    if (showMenu) {
        ModalBottomSheet(
            onDismissRequest = { showMenu = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = if (ThemeManager.isDarkMode) Surface_Dark else Color(0xFFD3D3D3)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp)
            ) {
                Text(
                    text = "Block",
                    fontSize = 16.sp,
                    color = if (ThemeManager.isDarkMode) White else Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showMenu = false
                            showBlockDialog = true
                        }
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                )
            }
        }
    }
    
    if (showBlockDialog) {
        ModalBottomSheet(
            onDismissRequest = { showBlockDialog = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = if (ThemeManager.isDarkMode) Surface_Dark else Color(0xFFD3D3D3)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (ThemeManager.isDarkMode) DarkGrey else Light_grey),
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePicture.isNotEmpty()) {
                        AsyncImage(
                            model = profilePicture,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.baseline_person_24),
                            contentDescription = null,
                            tint = Grey,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "If you block this user, they won't be able to message you or see your profile or posts anymore.",
                    fontSize = 14.sp,
                    color = if (ThemeManager.isDarkMode) White else Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "You can unblock them at anytime.",
                    fontSize = 14.sp,
                    color = if (ThemeManager.isDarkMode) White else Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        showBlockDialog = false
                        userViewModel.blockUser(currentUserId, userId) { success, message ->
                            if (success) {
                                isBlocked = true
                                isFollowing = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brown
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Block",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                }
            }
        }
    }
    
    if (showUnblockDialog) {
        ModalBottomSheet(
            onDismissRequest = { showUnblockDialog = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = if (ThemeManager.isDarkMode) Surface_Dark else Color(0xFFD3D3D3)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (ThemeManager.isDarkMode) DarkGrey else Light_grey),
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePicture.isNotEmpty()) {
                        AsyncImage(
                            model = profilePicture,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.baseline_person_24),
                            contentDescription = null,
                            tint = Grey,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Unblock this user?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (ThemeManager.isDarkMode) White else Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "They'll be able to contact you again.",
                    fontSize = 14.sp,
                    color = if (ThemeManager.isDarkMode) White else Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        showUnblockDialog = false
                        userViewModel.unblockUser(currentUserId, userId) { success, message ->
                            if (success) {
                                isBlocked = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brown
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Unblock",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                }
            }
        }
    }
}

@Composable
fun UserProfileStat(count: String, label: String, onClick: () -> Unit = {}) {
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
            color = if (ThemeManager.isDarkMode) White else Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Grey
        )
    }
}

@Composable
fun UserProfileTab(text: String, selected: Boolean, onClick: () -> Unit) {
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
            fontSize = 15.sp,
            color = if (selected) (if (ThemeManager.isDarkMode) White else Black) else Grey
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(40.dp)
                .background(if (selected) Brown else Color.Transparent)
        )
    }
}

@Preview
@Composable
fun PreviewUserProfile(){
    UserProfielBody(
        userId = "",
        initialUsername = "username"
    )
}
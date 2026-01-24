package com.example.closetly.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.R
import com.example.closetly.model.UserModel
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class FollowersFollowingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val userId = intent.getStringExtra("userId") ?: ""
        val username = intent.getStringExtra("username") ?: ""
        val initialTab = intent.getStringExtra("type") ?: "followers"
        
        setContent {
            FollowersFollowingScreen(userId = userId, username = username, initialTab = initialTab)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersFollowingScreen(userId: String, username: String, initialTab: String) {
    val context = LocalContext.current
    val userRepo = remember { UserRepoImpl(context) }
    val userViewModel = remember { UserViewModel(userRepo) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    var selectedTab by remember { mutableStateOf(if (initialTab == "followers") 0 else 1) }
    var followersList by remember { mutableStateOf<List<UserModel>>(emptyList()) }
    var followingList by remember { mutableStateOf<List<UserModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var followingStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var followerStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    
    LaunchedEffect(userId) {
        userViewModel.getFollowersList(userId) { list ->
            followersList = list
            isLoading = false
        }
        userViewModel.getFollowingList(userId) { list ->
            followingList = list
        }
    }
    
    LaunchedEffect(followersList, followingList) {
        if (currentUserId.isNotEmpty()) {
            val allUsers = (followersList + followingList).distinctBy { it.userId }
            allUsers.forEach { user ->
                if (user.userId != currentUserId) {
                    userViewModel.isFollowing(currentUserId, user.userId) { isFollowing ->
                        followingStates = followingStates + (user.userId to isFollowing)
                    }
                }
            }
        }
    }
    
    LaunchedEffect(followersList, followingList) {
        if (currentUserId.isNotEmpty()) {
            val allUsers = (followersList + followingList).distinctBy { it.userId }
            allUsers.forEach { user ->
                if (user.userId != currentUserId) {
                    userViewModel.isFollowing(user.userId, currentUserId) { theyFollowUs ->
                        followerStates = followerStates + (user.userId to theyFollowUs)
                    }
                }
            }
        }
    }
    
    val currentUsers = if (selectedTab == 0) followersList else followingList
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = username,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Black
                )
            )
        },
        containerColor = White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FollowTab(
                    text = "Followers",
                    count = followersList.size,
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                FollowTab(
                    text = "Following",
                    count = followingList.size,
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
            
            Divider(color = Light_grey, thickness = 1.dp)
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Brown)
                }
            } else if (currentUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTab == 0) "No followers yet" else "No following yet",
                        fontSize = 16.sp,
                        color = Grey
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(currentUsers) { user ->
                        UserItem(
                            user = user,
                            isCurrentUser = user.userId == currentUserId,
                            isFollowing = followingStates[user.userId] ?: false,
                            theyFollowUs = followerStates[user.userId] ?: false,
                            onUserClick = {
                                if (user.userId == currentUserId) {
                                } else {
                                    val intent = Intent(context, UserProfileActivity::class.java).apply {
                                        putExtra("userId", user.userId)
                                        putExtra("username", user.username)
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            onFollowClick = {
                                if (currentUserId.isNotEmpty() && user.userId != currentUserId) {
                                    userViewModel.toggleFollow(currentUserId, user.userId) { success, _ ->
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: UserModel,
    isCurrentUser: Boolean,
    isFollowing: Boolean,
    theyFollowUs: Boolean,
    onUserClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Light_grey),
            contentAlignment = Alignment.Center
        ) {
            if (user.profilePicture.isNotEmpty()) {
                AsyncImage(
                    model = user.profilePicture,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.baseline_person_24),
                    contentDescription = null,
                    tint = Grey,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user.username,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Black
            )
            Text(
                text = user.fullName,
                fontSize = 13.sp,
                color = Grey
            )
        }
        
        if (!isCurrentUser) {
            val buttonText = if (isFollowing) {
                "Following"
            } else if (theyFollowUs) {
                "Follow Back"
            } else {
                "Follow"
            }
            
            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) Light_grey else Brown
                ),
                modifier = Modifier
                    .height(32.dp)
                    .widthIn(min = 100.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isFollowing) Black else White
                )
            }
        }
    }
}

@Composable
fun FollowTab(text: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 15.sp,
                color = if (selected) Black else Grey
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = count.toString(),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp,
                color = Grey
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(60.dp)
                .background(if (selected) Brown else Color.Transparent)
        )
    }
}

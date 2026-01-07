package com.example.closetly

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class UserProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val userId = intent.getStringExtra("userId") ?: ""
        val username = intent.getStringExtra("username") ?: ""
        
        setContent {
            UserProfileScreen(userId = userId, initialUsername = username)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(userId: String, initialUsername: String) {
    val context = LocalContext.current
    val userRepo = remember { UserRepoImpl() }
    val userViewModel = remember { UserViewModel(userRepo) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf(initialUsername) }
    var bio by remember { mutableStateOf("") }
    var profilePicture by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    val tabs = listOf("Posts", "Listings")

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId) { success, _, userData ->
                if (success && userData != null) {
                    name = userData.fullName
                    username = userData.username
                    bio = userData.bio
                    profilePicture = userData.profilePicture
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = username,
                        fontSize = 18.sp,
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
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_menu_24),
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Black)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Light_grey),
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

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "0",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black
                        )
                        Text(
                            "Posts",
                            fontSize = 14.sp,
                            color = Grey
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "0",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black
                        )
                        Text(
                            "Followers",
                            fontSize = 14.sp,
                            color = Grey
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "0",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black
                        )
                        Text(
                            "Following",
                            fontSize = 14.sp,
                            color = Grey
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Black
                    )
                    if (bio.isNotEmpty()) {
                        Text(
                            bio,
                            fontSize = 14.sp,
                            color = Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {},
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Follow",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                    Button(
                        onClick = {
                            val intent = Intent(context, ChatActivity::class.java).apply {
                                putExtra("chatId", "")
                                putExtra("otherUserId", userId)
                                putExtra("otherUserName", username)
                                putExtra("otherUserImage", profilePicture)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Grey
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Message",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = White,
                    contentColor = Black
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = Black
                                )
                            }
                        )
                    }
                }

                when (selectedTabIndex) {
                    0 -> UserPostsGrid()
                    1 -> UserListingsGrid()
                }
            }
        }
    }
}

@Composable
fun UserPostsGrid() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No posts yet",
            fontSize = 16.sp,
            color = Grey
        )
    }
}

@Composable
fun UserListingsGrid() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No listings yet",
            fontSize = 16.sp,
            color = Grey
        )
    }
}

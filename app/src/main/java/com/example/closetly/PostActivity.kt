package com.example.closetly

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.model.PostModel
import com.example.closetly.model.ProductModel
import com.example.closetly.repository.ChatRepoImpl
import com.example.closetly.repository.PostRepoImpl
import com.example.closetly.repository.ProductRepoImpl
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.viewmodel.ChatViewModel
import com.example.closetly.viewmodel.PostViewModel
import com.example.closetly.viewmodel.ProductViewModel
import com.example.closetly.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth


class PostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val userId = intent.getStringExtra("userId") ?: ""
        val username = intent.getStringExtra("username") ?: ""
        
        setContent {
            PostBody(userId = userId, initialUsername = username)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostBody(userId: String, initialUsername: String) {

    val context = LocalContext.current
    val userRepo = remember { UserRepoImpl() }
    val userViewModel = remember { UserViewModel(userRepo) }
    val postRepo = remember { PostRepoImpl() }
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
                
                // Fetch user's posts
                postViewModel.getUserPosts(userId) { posts ->
                    userPosts = posts
                }
                
                // Fetch user's listings
                productViewModel.getUserProducts(userId) { products ->
                    userListings = products
                }
                
                // Only create chat if both users are valid
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
                            painter = painterResource(R.drawable.baseline_more_vert_24),
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
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),

                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,

            ){
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

                Column (
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text("${userPosts.size}",style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    ))
                    Text("Posts", style = TextStyle(
                        fontSize = 14.sp,
                        color = Grey
                    ))
                }
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text("0",style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    ))
                    Text("Followers", style = TextStyle(
                        fontSize = 14.sp,
                        color = Grey
                    ))
                }
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text("0",style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    ))
                    Text("Following", style = TextStyle(
                        fontSize = 14.sp,
                        color = Grey
                    ))
                }
            }
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ){
                Text(name, style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Black
                ))
                if (bio.isNotEmpty()) {
                    Text(bio, style = TextStyle(
                        fontSize = 14.sp,
                        color = Black
                    ))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),

                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){
                Button(
                    onClick = {
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF8B6F6F),
                    ),
                    shape = RoundedCornerShape(8.dp),

                    ) {
                    Text("Follow",style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    ))
                }
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
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Grey,
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Message",style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    ))
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
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = Black
                                )
                            )
                        }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> PostsGrid(context, userPosts, userId)
                1 -> ListingsGrid(context, userListings)
            }
        }
        }
    }
}

@Composable
fun PostsGrid(context: Context, posts: List<PostModel>, userId: String) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No posts yet",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Grey
                )
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize()
        ) {
            items(posts) { post ->
                val postIndex = posts.indexOf(post)
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(1.dp)
                        .background(Light_grey)
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
        }
    }
}

@Composable
fun ListingsGrid(context: Context, listings: List<ProductModel>) {
    if (listings.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No listings yet",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Grey
                )
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize()
        ) {
            items(listings) { listing ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(1.dp)
                        .background(Light_grey)
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
        }
    }
}

@Preview
@Composable
fun PostBodyPreview(){
    PostBody(
        userId = "",
        initialUsername = "username"
    )
}
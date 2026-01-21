package com.example.closetly

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.model.PostModel
import com.example.closetly.repository.HomePostRepoImpl
import com.example.closetly.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SavedPostsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SavedPostsBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPostsBody() {
    val context = LocalContext.current
    val homePostRepo = remember { HomePostRepoImpl() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    var savedPosts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load saved posts in real-time
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            homePostRepo.getSavedPosts(currentUserId).collectLatest { posts ->
                savedPosts = posts
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Saved Posts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Brown)
            }
        } else if (savedPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_bookmark_border_24),
                        contentDescription = null,
                        tint = Grey,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No saved posts yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Grey
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Save posts to view them here",
                        fontSize = 14.sp,
                        color = Grey.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(
                    items = savedPosts,
                    key = { it.postId }
                ) { post ->
                    SavedPostCard(post = post)
                }
            }
        }
    }
}

@Composable
fun SavedPostCard(post: PostModel) {
    val context = LocalContext.current
    var showPostDetail by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable {
                showPostDetail = true
            }
    ) {
        AsyncImage(
            model = post.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
    
    if (showPostDetail) {
        SavedPostDetailDialog(
            post = post,
            onDismiss = { showPostDetail = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPostDetailDialog(post: PostModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val homePostRepo = remember { HomePostRepoImpl() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()
    
    var isLiked by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(true) }
    var likesCount by remember { mutableStateOf(0) }
    var commentsCount by remember { mutableStateOf(0) }
    
    LaunchedEffect(post.postId) {
        homePostRepo.isPostLiked(post.postId, currentUserId).collectLatest { liked ->
            isLiked = liked
        }
    }
    
    LaunchedEffect(post.postId) {
        homePostRepo.getPostLikesCount(post.postId).collectLatest { count ->
            likesCount = count
        }
    }
    
    LaunchedEffect(post.postId) {
        homePostRepo.getPostCommentsCount(post.postId).collectLatest { count ->
            commentsCount = count
        }
    }
    
    LaunchedEffect(post.postId) {
        homePostRepo.isPostSaved(post.postId, currentUserId).collectLatest { saved ->
            isSaved = saved
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(White),
            color = White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White)
            ) {
                // Top bar
                TopAppBar(
                    title = { Text("Post") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
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
                
                // Post content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // User info header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clickable {
                                onDismiss()
                                val intent = Intent(context, UserProfileActivity::class.java).apply {
                                    putExtra("userId", post.userId)
                                    putExtra("username", post.username)
                                }
                                context.startActivity(intent)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Light_grey)
                        ) {
                            if (post.userProfilePic.isNotEmpty()) {
                                AsyncImage(
                                    model = post.userProfilePic,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_person_24),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    tint = Grey
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = post.username,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Black
                        )
                    }
                    
                    // Post image
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                    
                    // Action buttons row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Like button
                            Row(
                                modifier = Modifier.clickable {
                                    coroutineScope.launch {
                                        homePostRepo.toggleLike(post.postId, currentUserId)
                                    }
                                },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (isLiked) R.drawable.baseline_favorite_24 
                                        else R.drawable.baseline_favorite_border_24
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp),
                                    tint = if (isLiked) Color.Red else Black
                                )
                                if (likesCount > 0) {
                                    Text(
                                        "$likesCount",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Black
                                    )
                                }
                            }
                            
                            // Comment button
                            Row(
                                modifier = Modifier.clickable {
                                    onDismiss()
                                    val intent = Intent(context, CommentActivity::class.java).apply {
                                        putExtra("POST_ID", post.postId)
                                        putExtra("POST_USER_ID", post.userId)
                                        putExtra("USER_NAME", post.username)
                                    }
                                    context.startActivity(intent)
                                },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.comment),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = Black
                                )
                                if (commentsCount > 0) {
                                    Text(
                                        "$commentsCount",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Black
                                    )
                                }
                            }
                        }
                        
                        // Save button
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    homePostRepo.toggleSave(post.postId, currentUserId)
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (isSaved) R.drawable.baseline_bookmark_24 
                                    else R.drawable.baseline_bookmark_border_24
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                                tint = Black
                            )
                        }
                    }
                    
                    // Caption
                    val caption = post.resolveCaption()
                    if (caption.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                        ) {
                            Text(
                                text = post.username,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = caption,
                                fontSize = 15.sp,
                                color = Black
                            )
                        }
                    }
                }
            }
        }
    }
}

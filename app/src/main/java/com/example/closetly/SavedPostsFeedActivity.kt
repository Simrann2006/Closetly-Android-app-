package com.example.closetly

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.closetly.model.PostModel
import com.example.closetly.repository.HomePostRepoImpl
import com.example.closetly.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SavedPostsFeedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialIndex = intent.getIntExtra("INITIAL_INDEX", 0)

        setContent {
            SavedPostsFeedBody(initialIndex = initialIndex)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPostsFeedBody(initialIndex: Int) {
    val context = LocalContext.current
    val homePostRepo = remember { HomePostRepoImpl(context) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    var savedPosts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            homePostRepo.getSavedPosts(currentUserId).collectLatest { posts ->
                savedPosts = posts
                isLoading = false
            }
        }
    }
    
    LaunchedEffect(savedPosts, initialIndex) {
        if (savedPosts.isNotEmpty() && initialIndex in savedPosts.indices) {
            listState.scrollToItem(initialIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Saved Posts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            painterResource(R.drawable.baseline_arrow_back_ios_24),
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
                Text(
                    "No saved posts",
                    fontSize = 16.sp,
                    color = Grey
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(savedPosts) { post ->
                    SavedPostItem(post = post)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Grey.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
fun SavedPostItem(post: PostModel) {
    val context = LocalContext.current
    val homePostRepo = remember { HomePostRepoImpl(context) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()
    
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(0) }
    var commentsCount by remember { mutableStateOf(0) }
    var isSaved by remember { mutableStateOf(true) }
    
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
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
                        painterResource(R.drawable.baseline_person_24),
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

        AsyncImage(
            model = post.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

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

                Row(
                    modifier = Modifier.clickable {
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
                        painterResource(R.drawable.comment),
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

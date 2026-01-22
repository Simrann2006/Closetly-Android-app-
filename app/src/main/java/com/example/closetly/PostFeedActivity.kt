package com.example.closetly

import android.os.Bundle
import android.widget.Toast
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
import com.example.closetly.repository.PostRepoImpl
import com.example.closetly.repository.HomePostRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PostFeedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userId = intent.getStringExtra("USER_ID") ?: ""
        val initialIndex = intent.getIntExtra("INITIAL_INDEX", 0)

        setContent {
            PostFeedBody(userId = userId, initialIndex = initialIndex)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFeedBody(userId: String, initialIndex: Int) {
    val context = LocalContext.current
    val postRepo = remember { PostRepoImpl(context) }
    val postViewModel = remember { PostViewModel(postRepo) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var posts by remember { mutableStateOf<List<PostModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            postViewModel.getUserPosts(userId) { fetchedPosts ->
                posts = fetchedPosts
                isLoading = false
            }
        }
    }
    
    LaunchedEffect(posts, initialIndex) {
        if (posts.isNotEmpty() && initialIndex in posts.indices) {
            listState.scrollToItem(initialIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Posts",
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
        } else if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No posts yet",
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
                items(posts) { post ->
                    PostItem(
                        post = post,
                        isOwner = post.userId == currentUserId,
                        onDelete = {
                            postViewModel.deletePost(post.postId) { success, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                if (success) {
                                    postViewModel.getUserPosts(userId) { fetchedPosts ->
                                        posts = fetchedPosts
                                        if (posts.isEmpty()) {
                                            (context as? ComponentActivity)?.finish()
                                        }
                                    }
                                }
                            }
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        color = Grey.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
fun PostItem(post: PostModel, isOwner: Boolean, onDelete: () -> Unit) {
    val context = LocalContext.current
    val homePostRepo = remember { HomePostRepoImpl(context) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()
    
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(0) }
    var commentsCount by remember { mutableStateOf(0) }
    var isSaved by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var currentCaption by remember { mutableStateOf(post.caption) }
    
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

            Spacer(modifier = Modifier.weight(1f))

            if (isOwner) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_more_vert_24),
                            contentDescription = null,
                            tint = Black
                        )
                    }

                    DropdownMenu(
                        modifier = Modifier
                            .background(White),
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Post", color = Black) },
                            onClick = {
                                showMenu = false
                                showEditDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_edit_24),
                                    contentDescription = null,
                                    tint = Black
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Black,
                                leadingIconColor = Black
                            )
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Post", color = Black) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_delete_24),
                                    contentDescription = null,
                                    tint = Black
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Black,
                                leadingIconColor = Black
                            )
                        )
                    }
                }
            }
        }

        AsyncImage(
            model = post.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentScale = ContentScale.Crop
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                homePostRepo.toggleLike(post.postId, currentUserId)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(if (isLiked) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24),
                            contentDescription = null,
                            tint = if (isLiked) Color.Red else Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "$likesCount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Black
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        val intent = android.content.Intent(context, CommentActivity::class.java).apply {
                            putExtra("POST_ID", post.postId)
                            putExtra("POST_USER_ID", post.userId)
                            putExtra("USER_NAME", post.username)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        painterResource(R.drawable.comment),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "$commentsCount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Black
                    )
                }
            }

            IconButton(
                onClick = {
                    coroutineScope.launch {
                        homePostRepo.toggleSave(post.postId, currentUserId)
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(if (isSaved) R.drawable.baseline_bookmark_24 else R.drawable.baseline_bookmark_border_24),
                    contentDescription = null,
                    tint = Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (currentCaption.isNotEmpty()) {
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
                    text = currentCaption,
                    fontSize = 15.sp,
                    color = Black
                )
            }
        }

        Text(
            text = getPostTimeAgo(post.timestamp),
            fontSize = 12.sp,
            color = Grey,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
        )
    }
    
    if (showEditDialog) {
        EditPostDialog(
            post = post,
            onDismiss = { showEditDialog = false },
            onSave = { updatedCaption ->
                currentCaption = updatedCaption
                showEditDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to delete this post?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun getPostTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

@Composable
fun EditPostDialog(
    post: PostModel,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val context = LocalContext.current
    val postRepo = remember { PostRepoImpl(context) }
    val postViewModel = remember { PostViewModel(postRepo) }
    
    var caption by remember { mutableStateOf(post.caption) }
    var isSaving by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        title = {
            Text(
                text = "Edit Post",
                fontWeight = FontWeight.Bold,
                color = Black
            )
        },
        text = {
            Column {
                TextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = {
                        Text(
                            "Add a caption...",
                            color = Grey
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        disabledContainerColor = White,
                        focusedIndicatorColor = Grey.copy(alpha = 0.3f),
                        unfocusedIndicatorColor = Grey.copy(alpha = 0.3f),
                        cursorColor = Black
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = Black
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    isSaving = true
                    val updatedPost = post.copy(caption = caption)
                    postViewModel.updatePost(updatedPost) { success, message ->
                        isSaving = false
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        if (success) {
                            onSave(caption)
                        }
                    }
                },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Brown,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save", color = Brown)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cancel", color = Grey)
            }
        }
    )
}
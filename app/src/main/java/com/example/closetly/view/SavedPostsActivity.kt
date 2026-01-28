package com.example.closetly.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.R
import com.example.closetly.model.PostModel
import com.example.closetly.repository.HomePostRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.utils.ThemeManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest

class SavedPostsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        ThemeManager.initialize(this)
        
        setContent {
            ClosetlyTheme(darkTheme = ThemeManager.isDarkMode) {
                SavedPostsBody()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPostsBody() {
    val context = LocalContext.current
    val homePostRepo = remember { HomePostRepoImpl(context) }
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
                        tint = if (ThemeManager.isDarkMode) Grey else Grey,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No saved posts yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (ThemeManager.isDarkMode) Grey else Grey
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Save posts to view them here",
                        fontSize = 14.sp,
                        color = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.7f) else Grey.copy(alpha = 0.7f)
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
                    val postIndex = savedPosts.indexOf(post)
                    SavedPostCard(
                        post = post,
                        onClick = {
                            val intent = Intent(context, SavedPostsFeedActivity::class.java).apply {
                                putExtra("INITIAL_INDEX", postIndex)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SavedPostCard(post: PostModel, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = post.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}


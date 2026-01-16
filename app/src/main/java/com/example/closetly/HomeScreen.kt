package com.example.closetly

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.closetly.ui.theme.White
import com.example.closetly.utils.getTimeAgo
import com.example.closetly.viewmodel.HomeViewModel
import com.example.closetly.viewmodel.SliderViewModel
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPostClick: (String, String) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = viewModel(),
    sliderViewModel: SliderViewModel = viewModel()
) {
    val context = LocalContext.current
    val postsUI by viewModel.postsUI.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Firebase slider data
    val sliderItems by sliderViewModel.sliderItems.collectAsState()
    val sliderCount = if (sliderItems.isEmpty()) 4 else sliderItems.size
    
    // Fallback to hardcoded images if Firebase has no data
    val fallbackImages = listOf(
        "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800",
        "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=800",
        "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800",
        "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=800",
    )

    val pagerState = rememberPagerState()

    LaunchedEffect(pagerState, sliderCount) {
        while (true) {
            delay(3000)
            if (sliderCount > 0) {
                val nextPage = (pagerState.currentPage + 1) % sliderCount
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                HorizontalPager(
                    count = sliderCount,
                    state = pagerState,
                ) { indexOfImages ->
                    val sliderItem = sliderItems.getOrNull(indexOfImages)
                    val imageUrl = sliderItem?.imageUrl ?: fallbackImages.getOrElse(indexOfImages) { fallbackImages[0] }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .clickable {
                                try {
                                    // Navigate to user's profile showing the specific post
                                    if (sliderItem != null && sliderItem.userId.isNotEmpty()) {
                                        val intent = Intent(context, PostActivity::class.java).apply {
                                            putExtra("userId", sliderItem.userId)
                                            putExtra("username", sliderItem.username)
                                            putExtra("highlightPostId", sliderItem.postId) // Highlight this specific post
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        // Fallback to old behavior for backward compatibility
                                        val intent = Intent(context, PostActivity::class.java).apply {
                                            when (indexOfImages) {
                                                0 -> putExtra("userId", "user_kendall")
                                                1 -> putExtra("userId", "user_emily")
                                                2 -> putExtra("userId", "user_sophia")
                                                3 -> putExtra("userId", "user_olivia")
                                            }
                                        }
                                        context.startActivity(intent)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = sliderItem?.username ?: "Slider image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxSize()
                        ) {
                            // Display username from Firebase post or fallback
                            val displayUsername = sliderItem?.username ?: when (indexOfImages) {
                                0 -> "kendall"
                                1 -> "emily"
                                2 -> "sophia"
                                3 -> "olivia"
                                else -> "explore"
                            }

                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 16.dp)
                            ) {
                                Text(
                                    text = displayUsername,
                                    style = TextStyle(
                                        fontSize = 64.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                
                                // Show "posted a new post" indicator
                                if (sliderItem != null) {
                                    Text(
                                        text = "posted a new post",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.White.copy(alpha = 0.9f)
                                        ),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            
                            // Show caption and engagement stats if available
                            if (sliderItem != null) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(start = 16.dp, bottom = 80.dp)
                                ) {
                                    if (sliderItem.caption.isNotEmpty()) {
                                        Text(
                                            text = sliderItem.caption,
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = Color.White
                                            ),
                                            maxLines = 2,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    
                                    // Show likes and comments count
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            text = "❤️ ${sliderItem.likesCount}",
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = "💬 ${sliderItem.commentsCount}",
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Show product cards (keeping existing functionality)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when (indexOfImages) {
                                0 -> {
                                    ProductCard("https://images.unsplash.com/photo-1549298916-b41d501d3772?w=400", "Half Jeans", "Rs.299")
                                    ProductCard("https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=400", "Blue Hoodie", "Rs.799")
                                    ProductCard("https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400", "Bebe Tee", "Rs.499")
                                }
                                1 -> {
                                    ProductCard("https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400", "White Tee", "Rs.399")
                                    ProductCard("https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400", "Denim Jacket", "Rs.899")
                                    ProductCard("https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=400", "Shorts", "Rs.349")
                                }
                                2 -> {
                                    ProductCard("https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=400", "Hoodie", "Rs.699")
                                    ProductCard("https://images.unsplash.com/photo-1542272604-787c3835535d?w=400", "Jeans", "Rs.599")
                                    ProductCard("https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=400", "Dress", "Rs.799")
                                }
                                3 -> {
                                    ProductCard("https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400", "Jacket", "Rs.999")
                                    ProductCard("https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400", "T-Shirt", "Rs.299")
                                    ProductCard("https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=400", "Pants", "Rs.649")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            HorizontalPagerIndicator(
                pagerState = pagerState,
                pageCount = sliderCount,
                activeColor = Color.White,
                inactiveColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
            )

            Spacer(Modifier.height(16.dp))
        }

        if (isLoading && postsUI.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        error?.let { errorMessage ->
            item {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        items(
            items = postsUI,
            key = { it.post.postId }
        ) { postUI ->
            PostCard(
                postUI = postUI,
                onLikeClick = { viewModel.toggleLike(postUI.post.postId) },
                onSaveClick = { viewModel.toggleSave(postUI.post.postId) },
                onFollowClick = { viewModel.toggleFollow(postUI.post.userId) },
                onCommentClick = {
                    val intent = Intent(context, CommentActivity::class.java).apply {
                        putExtra("POST_ID", postUI.post.postId)
                        putExtra("POST_USER_ID", postUI.post.userId)
                        putExtra("USER_NAME", postUI.post.username)
                    }
                    context.startActivity(intent)
                }
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun PostCard(
    postUI: com.example.closetly.viewmodel.PostUI,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onFollowClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        contentColor = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = postUI.post.userProfilePic.ifEmpty { "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200" },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.LightGray, CircleShape)
                        .clickable {
                            if (postUI.post.userId.isNotEmpty()) {
                                try {
                                    val intent = Intent(context, PostActivity::class.java).apply {
                                        putExtra("userId", postUI.post.userId)
                                        putExtra("username", postUI.post.username)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                )
                Spacer(modifier = Modifier.width(13.dp))

                Text(
                    postUI.post.username,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable {
                        if (postUI.post.userId.isNotEmpty()) {
                            try {
                                val intent = Intent(context, PostActivity::class.java).apply {
                                    putExtra("userId", postUI.post.userId)
                                    putExtra("username", postUI.post.username)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.weight(1f))

                androidx.compose.material3.Button(
                    onClick = onFollowClick,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (postUI.isFollowing) Color.LightGray else colorResource(R.color.purple_200),
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        if (postUI.isFollowing) "Following" else "Follow",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (postUI.isFollowing) Color.DarkGray else Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = postUI.post.imageUrl.ifEmpty { "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800" },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Like button with count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onLikeClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (postUI.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (postUI.isLiked) Color.Red else Color.Black,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Text(
                            "${postUI.likesCount}",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.clickable { onCommentClick() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.chat),
                            contentDescription = "Comment",
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            "${postUI.commentsCount}",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onSaveClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (postUI.isSaved)
                            Icons.Default.Bookmark
                        else
                            Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            ) {
                Text(
                    postUI.post.caption,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                    )
                )
            }

            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)
            ) {
                Text(
                    getTimeAgo(postUI.post.timestamp),
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
fun ProductCard(imageUrl: String, title: String, price: String) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Box {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                Text(
                    title,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                )
                Text(
                    price,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 9.sp,
                        color = White
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewHome() {
    HomeScreen()
}

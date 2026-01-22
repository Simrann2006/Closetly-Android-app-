package com.example.closetly

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.Surface
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
import com.example.closetly.model.SliderItemModel
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
    
    // Real-time Firebase slider data
    val sliderItems by sliderViewModel.sliderItems.collectAsState()
    val sliderLoading by sliderViewModel.isLoading.collectAsState()
    
    // Dynamic slider count based on Firebase data
    val sliderCount = if (sliderItems.isEmpty()) 0 else sliderItems.size
    
    val pagerState = rememberPagerState()

    // Auto-scroll effect - Netflix-style smooth animation
    LaunchedEffect(pagerState, sliderCount) {
        if (sliderCount > 0) {
            while (true) {
                delay(3000) // 3 seconds per slide
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
        // Show single loading indicator when both are loading initially
        if ((sliderLoading && sliderItems.isEmpty()) && (isLoading && postsUI.isEmpty())) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 200.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            item {
                // Netflix-style auto-slider with real-time Firebase data
                if (sliderLoading && sliderItems.isEmpty()) {
                    // Loading state for slider only
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Real-time slider from Firebase
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    ) {
                        HorizontalPager(
                            count = sliderCount,
                            state = pagerState,
                        ) { pageIndex ->
                            val sliderItem = sliderItems.getOrNull(pageIndex)
                            
                            if (sliderItem != null) {
                                SliderItemCard(
                                    sliderItem = sliderItem,
                                    onItemClick = {
                                        // Navigate to user profile when clicking anywhere on slider
                                        try {
                                            val intent = Intent(context, UserProfileActivity::class.java).apply {
                                                putExtra("userId", sliderItem.userId)
                                                putExtra("username", sliderItem.username)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },
                                    onUsernameClick = {
                                        // Navigate to user profile when clicking username
                                        try {
                                            val intent = Intent(context, UserProfileActivity::class.java).apply {
                                                putExtra("userId", sliderItem.userId)
                                                putExtra("username", sliderItem.username)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Slider indicators
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        pageCount = sliderCount,
                        activeColor = Color.White,
                        inactiveColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                    )

                    Spacer(Modifier.height(16.dp))
                }
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
}

@Composable
fun SliderItemCard(
    sliderItem: SliderItemModel,
    onItemClick: () -> Unit,
    onUsernameClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clickable { onItemClick() }
    ) {
        // Show user profile background with listings
        // Background image = USER'S PROFILE PICTURE or default placeholder
        if (sliderItem.profilePictureUrl.isNotEmpty()) {
            AsyncImage(
                model = sliderItem.profilePictureUrl,
                contentDescription = "${sliderItem.username}'s profile",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
        } else {
            // Default placeholder background for users without profile picture
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color(0xFFE8E8E8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_person_24),
                    contentDescription = "Default profile",
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(120.dp)
                )
            }
        }

        // Overlay content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))  // Slight overlay for text visibility
        ) {
                // Username overlay at top-left
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .clickable { onUsernameClick() }
                ) {
                    Text(
                        text = sliderItem.username,
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    
                    Text(
                        text = "${sliderItem.totalListings} listings",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // LISTING CARDS at bottom (small boxes with listing image, name, price)
                if (sliderItem.listings.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Show up to 3 listing cards
                        sliderItem.listings.take(3).forEach { listing ->
                            ListingCard(
                                imageUrl = listing.imageUrl,
                                itemName = listing.itemName,
                                price = listing.price
                            )
                        }
                    }
                }
            }
    }
}

/**
 * Small card component for displaying individual listings inside the slider
 * Shows: listing image, item name, price
 */
@Composable
fun ListingCard(imageUrl: String, itemName: String, price: String) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Box {
            // Listing image
            AsyncImage(
                model = imageUrl,
                contentDescription = itemName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Item info overlay at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                if (itemName.isNotEmpty()) {
                    Text(
                        itemName,
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1
                    )
                }
                if (price.isNotEmpty()) {
                    Text(
                        price,
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    )
                }
            }
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
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8E8E8))
                        .border(1.5.dp, Color.LightGray, CircleShape)
                        .clickable {
                            if (postUI.post.userId.isNotEmpty()) {
                                try {
                                    val intent = Intent(context, UserProfileActivity::class.java).apply {
                                        putExtra("userId", postUI.post.userId)
                                        putExtra("username", postUI.post.username)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (postUI.post.userProfilePic.isNotEmpty()) {
                        AsyncImage(
                            model = postUI.post.userProfilePic,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.baseline_person_24),
                            contentDescription = "Default profile",
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    postUI.post.username,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.clickable {
                        if (postUI.post.userId.isNotEmpty()) {
                            try {
                                val intent = Intent(context, UserProfileActivity::class.java).apply {
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
                    modifier = Modifier.height(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B6F6F),
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        if (postUI.isFollowing) "Following" else "Follow",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }
            }

            // Post Image - Fixed square size
            AsyncImage(
                model = postUI.post.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable {
                        val intent = Intent(context, UserProfileActivity::class.java).apply {
                            putExtra("userId", postUI.post.userId)
                            putExtra("username", postUI.post.username)
                        }
                        context.startActivity(intent)
                    }
            )

            // Action buttons row (Like, Comment, Save) - Instagram style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Like and Comment
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Like button with count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onLikeClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (postUI.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (postUI.isLiked) Color.Red else Color.Black,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        if (postUI.likesCount > 0) {
                            Text(
                                "${postUI.likesCount}",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                            )
                        }
                    }

                    // Comment button with count
                    Row(
                        modifier = Modifier.clickable { onCommentClick() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.comment),
                                contentDescription = "Comment",
                                tint = Color.Black,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        if (postUI.commentsCount > 0) {
                            Text(
                                "${postUI.commentsCount}",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }

                // Right side: Save button
                IconButton(
                    onClick = onSaveClick,
                    modifier = Modifier.size(36.dp)
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

            // Caption and content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                // Show title for products
                if (postUI.post.postType == "product" && postUI.post.title.isNotEmpty()) {
                    Text(
                        postUI.post.title,
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                
                // Show caption/description with username
                if (postUI.post.resolveCaption().isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            postUI.post.username,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        )
                        Text(
                            " ${postUI.post.resolveCaption()}",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black
                            )
                        )
                    }
                }
                
                // Show price for products
                if (postUI.post.postType == "product" && postUI.post.price > 0.0) {
                    Text(
                        postUI.post.formatPrice(),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                // Timestamp - subtle and clean
                Text(
                    getTimeAgo(postUI.post.timestamp),
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
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

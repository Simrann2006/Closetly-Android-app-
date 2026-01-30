package com.example.closetly.view

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.example.closetly.R
import com.example.closetly.model.SliderItemModel
import com.example.closetly.ui.theme.Background_Dark
import com.example.closetly.ui.theme.Black
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.DarkGrey
import com.example.closetly.ui.theme.Grey
import com.example.closetly.ui.theme.Light_brown
import com.example.closetly.ui.theme.Light_grey
import com.example.closetly.ui.theme.Red
import com.example.closetly.ui.theme.Surface_Dark
import com.example.closetly.ui.theme.White
import com.example.closetly.utils.ThemeManager
import com.example.closetly.utils.getTimeAgo
import com.example.closetly.viewmodel.HomeViewModel
import com.example.closetly.viewmodel.PostUI
import com.example.closetly.viewmodel.SliderViewModel
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.delay
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    sliderViewModel: SliderViewModel = viewModel()
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = remember { HomeViewModel(context) }
    val postsUI by viewModel.postsUI.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val error by viewModel.error.collectAsState()
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val shuffledPosts = remember(postsUI, refreshTrigger) {
        postsUI.shuffled()
    }

    val sliderItems by sliderViewModel.sliderItems.collectAsState()
    val sliderLoading by sliderViewModel.isLoading.collectAsState()
    
    val sliderCount = sliderItems.size

    val pagerState = rememberPagerState(
        pageCount = { sliderCount },
        initialPage = 0
    )
    
    val listState = rememberLazyListState()
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { 
            viewModel.refreshPosts()
            sliderViewModel.refresh()
            refreshTrigger++ // Trigger reshuffle
        }
    )
    
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            
            lastVisibleItemIndex >= totalItemsCount - 3 && totalItemsCount > 0
        }
    }
    
    LaunchedEffect(Unit) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                if (!isLoadingMore && !isLoading) {
                    viewModel.loadMorePosts()
                }
            }
    }

    LaunchedEffect(pagerState, sliderCount) {
        if (sliderCount > 1) {
            while (true) {
                delay(4000) // 4 second delay between slides
                val nextPage = (pagerState.currentPage + 1) % sliderCount
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 600,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    )
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (ThemeManager.isDarkMode) Background_Dark else Light_grey)
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if ((sliderLoading && sliderItems.isEmpty()) && (isLoading && postsUI.isEmpty())) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(600.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Brown)
                    }
                }
            } else {
                item {
                    if (sliderLoading && sliderItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Brown)
                        }
                    } else if (sliderItems.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { pageIndex ->
                                val sliderItem = sliderItems.getOrNull(pageIndex)

                                if (sliderItem != null) {
                                    SliderItemCard(
                                        sliderItem = sliderItem,
                                        onItemClick = {
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

                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(sliderCount) { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (pagerState.currentPage == index) White
                                                else Grey.copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }
                        }
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
                            CircularProgressIndicator(color = Brown)
                        }
                    }
                }

                error?.let { errorMessage ->
                    item {
                        Text(
                            text = errorMessage,
                            color = Red,
                            modifier = Modifier.padding(16.dp),
                            fontFamily = FontFamily(Font(R.font.poppins_regular))
                        )
                    }
                }


                items(
                    items = shuffledPosts,
                    key = { it.post.postId },
                    contentType = { "post" }
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
                    Spacer(Modifier.height(2.dp))
                }
                
                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Brown,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
        
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = if (ThemeManager.isDarkMode) Background_Dark else White,
            contentColor = if (ThemeManager.isDarkMode) White else Black
        )
    }
}

@Composable
fun SliderItemCard(
    sliderItem: SliderItemModel,
    onItemClick: () -> Unit,
    onUsernameClick: () -> Unit
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clickable { onItemClick() }
    ) {
        if (sliderItem.profilePictureUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(sliderItem.profilePictureUrl)
                    .memoryCachePolicy(CachePolicy.READ_ONLY)
                    .crossfade(true)
                    .build(),
                contentDescription = "${sliderItem.username}'s profile",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Light_brown.copy(alpha = 0.3f),
                                Brown.copy(alpha = 0.4f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_person_24),
                    contentDescription = "Default profile",
                    tint = White.copy(alpha = 0.5f),
                    modifier = Modifier.size(120.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Black.copy(alpha = 0.7f)
                        ),
                        startY = 200f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
                    .clickable { onUsernameClick() }
            ) {
                Text(
                    text = sliderItem.username,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_add_shopping_cart_24),
                        contentDescription = null,
                        tint = White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${sliderItem.totalListings} listings",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = White.copy(alpha = 0.9f)
                        )
                    )
                }
            }

            if (sliderItem.listings.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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

@Composable
fun ListingCard(imageUrl: String, itemName: String, price: String) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(145.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = 6.dp,
        backgroundColor = if (ThemeManager.isDarkMode) Background_Dark else White
    ) {
        Box {
            AsyncImage(
                model = imageUrl,
                contentDescription = itemName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Black.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                if (itemName.isNotEmpty()) {
                    Text(
                        itemName,
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = White
                        ),
                        maxLines = 1
                    )
                }
                val cleanPrice = price
                    .replace("$", "")
                    .replace("Rs. ", "Rs.")
                    .replace("Rs.", "Rs.")
                    .trim()
                
                val displayPrice = when {
                    cleanPrice.isBlank() || cleanPrice == "0" || cleanPrice == "Rs.0" || cleanPrice == "Rs.0/day" -> "Rs.100/day"
                    cleanPrice.startsWith("Rs.") -> cleanPrice
                    cleanPrice.matches(Regex("[0-9]+\\.?[0-9]*/day")) -> "Rs.$cleanPrice"
                    cleanPrice.matches(Regex("[0-9]+\\.?[0-9]*")) -> "Rs.$cleanPrice"
                    else -> "Rs.$cleanPrice"
                }
                Text(
                    displayPrice,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                )
            }
        }
    }
}

@Composable
fun PostCard(
    postUI: PostUI,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onFollowClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 1.dp),
        shape = RoundedCornerShape(0.dp),
        elevation = 0.dp,
        backgroundColor = if (ThemeManager.isDarkMode) Background_Dark else White
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (ThemeManager.isDarkMode)
                                DarkGrey
                            else
                                Light_grey
                        )
                        .border(
                            2.dp,
                            if (ThemeManager.isDarkMode)
                                Grey.copy(alpha = 0.3f)
                            else
                                Light_brown.copy(alpha = 0.3f),
                            CircleShape
                        )
                        .combinedClickable(
                            onClick = {
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
                            onLongClick = {
                                if (postUI.post.userProfilePic.isNotEmpty()) {
                                    try {
                                        val intent = Intent(context, FullScreenImageActivity::class.java).apply {
                                            putExtra("IMAGE_URL", postUI.post.userProfilePic)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (postUI.post.userProfilePic.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(postUI.post.userProfilePic)
                                .memoryCachePolicy(CachePolicy.READ_ONLY)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.baseline_person_24),
                            contentDescription = "Default profile",
                            tint = if (ThemeManager.isDarkMode) Grey else DarkGrey,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    postUI.post.username,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (ThemeManager.isDarkMode) White else Black
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

                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (postUI.post.userId != currentUserId) {
                    Button(
                        onClick = onFollowClick,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (postUI.isFollowing) {
                                if (ThemeManager.isDarkMode) DarkGrey else Light_grey
                            } else {
                                Brown
                            }
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
                    ) {
                        Text(
                            if (postUI.isFollowing) "Following" else "Follow",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (postUI.isFollowing) {
                                    if (ThemeManager.isDarkMode) White else Black
                                } else {
                                    White
                                }
                            )
                        )
                    }
                }
            }

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onLikeClick,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = if (postUI.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (postUI.isLiked) Red else (if (ThemeManager.isDarkMode) White else Black),
                                modifier = Modifier.size(27.dp)
                            )
                        }
                        if (postUI.likesCount > 0) {
                            Text(
                                "${postUI.likesCount}",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (ThemeManager.isDarkMode) White else Black
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.clickable { onCommentClick() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(38.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.comment),
                                contentDescription = "Comment",
                                tint = if (ThemeManager.isDarkMode) White else Black,
                                modifier = Modifier.size(27.dp)
                            )
                        }
                        if (postUI.commentsCount > 0) {
                            Text(
                                "${postUI.commentsCount}",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (ThemeManager.isDarkMode) White else Black
                                )
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onSaveClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = if (postUI.isSaved)
                            Icons.Default.Bookmark
                        else
                            Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (postUI.isSaved) Brown else (if (ThemeManager.isDarkMode) White else Black),
                        modifier = Modifier.size(27.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                if (postUI.post.postType == "product" && postUI.post.title.isNotEmpty()) {
                    Text(
                        postUI.post.title,
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ThemeManager.isDarkMode) White else Black
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                if (postUI.post.resolveCaption().isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            postUI.post.username,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (ThemeManager.isDarkMode) White else Black
                            )
                        )
                        Text(
                            " ${postUI.post.resolveCaption()}",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (ThemeManager.isDarkMode) White.copy(alpha = 0.9f) else Black
                            )
                        )
                    }
                }

                if (postUI.post.postType == "product" && postUI.post.price > 0.0) {
                    Text(
                        postUI.post.formatPrice(),
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brown
                        ),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Text(
                    getTimeAgo(postUI.post.timestamp),
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Grey
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
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

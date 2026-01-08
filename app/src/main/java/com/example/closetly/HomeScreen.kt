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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import com.example.closetly.ui.theme.White
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onPostClick: (String, String) -> Unit = { _, _ -> }) {

    val context = LocalContext.current

    var isPost1Liked by remember { mutableStateOf(false) }
    var post1LikeCount by remember { mutableStateOf(1143) }

    var isPost2Liked by remember { mutableStateOf(false) }
    var post2LikeCount by remember { mutableStateOf(509) }

    var isPost1Saved by remember { mutableStateOf(false) }
    var isPost2Saved by remember { mutableStateOf(false) }

    var isPost1Following by remember { mutableStateOf(false) }
    var isPost2Following by remember { mutableStateOf(false) }

    val images = listOf(
        "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800",
        "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=800",
        "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=800",
        "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=800",
    )

    val pagerState = rememberPagerState()

    LaunchedEffect(pagerState) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % images.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    val scrollState = rememberScrollState()

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
                    count = images.size,
                    state = pagerState,
                ) { indexOfImages ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .clickable {
                                val intent = Intent(context, PostActivity::class.java).apply {
                                    when (indexOfImages) {
                                        0 -> {
                                            putExtra("USER_NAME", "Kendall")
                                            putExtra("USER_HANDLE", "k2")
                                            putExtra(
                                                "USER_BIO",
                                                "Sustainable style for every soul."
                                            )
                                            putExtra("POSTS_COUNT", 3)
                                            putExtra("FOLLOWERS_COUNT", 1456)
                                            putExtra("FOLLOWING_COUNT", 11)
                                        }

                                        1 -> {
                                            putExtra("USER_NAME", "Emily")
                                            putExtra("USER_HANDLE", "emily_style")
                                            putExtra("USER_BIO", "Eco-friendly, wallet-friendly.")
                                            putExtra("POSTS_COUNT", 8)
                                            putExtra("FOLLOWERS_COUNT", 2340)
                                            putExtra("FOLLOWING_COUNT", 156)
                                        }

                                        2 -> {
                                            putExtra("USER_NAME", "Sophia")
                                            putExtra("USER_HANDLE", "sophia_fashion")
                                            putExtra("USER_BIO", "Vintage vibes & modern style")
                                            putExtra("POSTS_COUNT", 15)
                                            putExtra("FOLLOWERS_COUNT", 3890)
                                            putExtra("FOLLOWING_COUNT", 234)
                                        }

                                        3 -> {
                                            putExtra("USER_NAME", "Olivia")
                                            putExtra("USER_HANDLE", "liv_closet")
                                            putExtra("USER_BIO", "Minimalist wardrobe enthusiast")
                                            putExtra("POSTS_COUNT", 12)
                                            putExtra("FOLLOWERS_COUNT", 1987)
                                            putExtra("FOLLOWING_COUNT", 89)
                                        }
                                    }
                                }
                                context.startActivity(intent)
                            }
                    ) {
                        AsyncImage(
                            model = images[indexOfImages],
                            contentDescription = null,
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
                            val nameText = when (indexOfImages) {
                                0 -> "kendall"
                                1 -> "emily"
                                2 -> "sophia"
                                3 -> "olivia"
                                else -> "kendall"
                            }

                            Text(
                                text = nameText,
                                style = TextStyle(
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 16.dp)
                            )
                        }

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
                pageCount = 4,
                activeColor = Color.White,
                inactiveColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
            )

            Spacer(Modifier.height(16.dp))

            Card(
                contentColor = Color.Transparent
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.LightGray, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(13.dp))

                        Text(
                            "Emily",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
<<<<<<< Updated upstream
                        Spacer(modifier = Modifier.weight(1f))
=======
                        Spacer(modifier = Modifier.width(130.dp))
>>>>>>> Stashed changes

                        androidx.compose.material3.Button(
                            onClick = {
                                isPost1Following = !isPost1Following
                            },
                            modifier = Modifier
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPost1Following) Color.LightGray else colorResource(R.color.purple_200),
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                if (isPost1Following) "Following" else "Follow",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPost1Following) Color.DarkGray else Color.White
                                )
                            )
                        }
<<<<<<< Updated upstream
=======
                        Spacer(modifier = Modifier.width(20.dp))

                        Icon(
                            painter = painterResource(R.drawable.edit),
                            null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    selectedPostImage = R.drawable.lu
                                    showBottomSheet = true
                                }
                        )
>>>>>>> Stashed changes
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        isPost1Liked = !isPost1Liked
                                        post1LikeCount = if (isPost1Liked) post1LikeCount + 1 else post1LikeCount - 1
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPost1Liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isPost1Liked) Color.Red else Color.Black,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "$post1LikeCount", style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.clickable {
                                    val intent = Intent(context, CommentActivity::class.java).apply {
                                        putExtra("POST_ID", "post_1")
                                    }
                                    context.startActivity(intent)
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.chat),
                                    contentDescription = "Comment",
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "59", style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                isPost1Saved = !isPost1Saved
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isPost1Saved)
                                    Icons.Default.Bookmark
                                else
                                    Icons.Default.BookmarkBorder,
                                contentDescription = "Save",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                    ) {
                        Text(
                            "Eco-friendly, wallet-friendly.", style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        )
                    }
                    Row(
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 8.dp)
                    ) {
                        Text(
                            "22 hours ago", style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                contentColor = Color.Transparent
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.LightGray, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(13.dp))

                        Text(
                            "kendall",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
<<<<<<< Updated upstream
                        Spacer(modifier = Modifier.weight(1f))
=======
                        Spacer(modifier = Modifier.width(130.dp))
>>>>>>> Stashed changes

                        androidx.compose.material3.Button(
                            onClick = {
                                isPost2Following = !isPost2Following
                            },
                            modifier = Modifier
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPost2Following) Color.LightGray else colorResource(R.color.purple_200),
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                if (isPost2Following) "Following" else "Follow",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPost2Following) Color.DarkGray else Color.White
                                )
                            )
                        }
<<<<<<< Updated upstream
=======
                        Spacer(modifier = Modifier.width(20.dp))

                        Icon(
                            painter = painterResource(R.drawable.edit),
                            null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    selectedPostImage = R.drawable.jacket
                                    showBottomSheet = true
                                }
                        )
>>>>>>> Stashed changes
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        isPost2Liked = !isPost2Liked
                                        post2LikeCount = if (isPost2Liked) post2LikeCount + 1 else post2LikeCount - 1
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPost2Liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (isPost2Liked) Color.Red else Color.Black,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "$post2LikeCount", style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.clickable {
                                    val intent = Intent(context, CommentActivity::class.java).apply {
                                        putExtra("POST_ID", "post_2")
                                    }
                                    context.startActivity(intent)
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.chat),
                                    contentDescription = "Comment",
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "32", style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                isPost2Saved = !isPost2Saved
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isPost2Saved)
                                    Icons.Default.Bookmark
                                else
                                    Icons.Default.BookmarkBorder,
                                contentDescription = "Save",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                    ) {
                        Text(
                            "Style that tells a story", style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 8.dp)
                    ) {
                        Text(
                            "5 days ago", style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray
                            )
                        )
                    }
                }
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
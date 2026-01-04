package com.example.closetly

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.closetly.ui.theme.White
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onPostClick: (String, String) -> Unit = { _, _ -> }) {

    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedPostImage by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val images = listOf(
        R.drawable.image1,
        R.drawable.image2,
        R.drawable.image3,
        R.drawable.image4,
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
                                            putExtra("USER_BIO", "Sustainable style for every soul.")
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
                        Image(
                            painter = painterResource(id = images[indexOfImages]),
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
                                    ProductCard(R.drawable.image4, "Half Jeans", "Rs.299")
                                    ProductCard(R.drawable.image2, "Blue Hoodie", "Rs.799")
                                    ProductCard(R.drawable.image3, "Bebe Tee", "Rs.499")
                                }
                                1 -> {
                                    ProductCard(R.drawable.image3, "White Tee", "Rs.399")
                                    ProductCard(R.drawable.image1, "Denim Jacket", "Rs.899")
                                    ProductCard(R.drawable.image4, "Shorts", "Rs.349")
                                }
                                2 -> {
                                    ProductCard(R.drawable.image2, "Hoodie", "Rs.699")
                                    ProductCard(R.drawable.image4, "Jeans", "Rs.599")
                                    ProductCard(R.drawable.image1, "Dress", "Rs.799")
                                }
                                3 -> {
                                    ProductCard(R.drawable.image1, "Jacket", "Rs.999")
                                    ProductCard(R.drawable.image3, "T-Shirt", "Rs.299")
                                    ProductCard(R.drawable.image2, "Pants", "Rs.649")
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
                        Image(
                            painter = painterResource(R.drawable.image4),
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
                        Spacer(modifier = Modifier.width(160.dp))

                        androidx.compose.material3.Button(
                            onClick = {
                            },
                            modifier = Modifier
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.purple_200),
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                "Follow", style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))

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
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.lu),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.heart),
                            null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "1143", style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(20.dp))

                        Row(
                            modifier = Modifier.clickable {
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.chat),
                                null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                "59", style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))
                        Icon(
                            painter = painterResource(R.drawable.share),
                            null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "34", style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp)
                    ) {
                        Text(
                            "Eco-friendly, wallet-friendly.", style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        )
                    }
                    Row(
                        modifier = Modifier.padding(start = 10.dp)
                    ) {
                        Text(
                            "22 hours ago", style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
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
                        Image(
                            painter = painterResource(R.drawable.image1),
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
                        Spacer(modifier = Modifier.width(150.dp))

                        androidx.compose.material3.Button(
                            onClick = {
                            },
                            modifier = Modifier
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.purple_200),
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                "Follow", style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))

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
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.jacket),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.heart),
                            null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "509", style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(20.dp))

                        Row(
                            modifier = Modifier.clickable {
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.chat),
                                null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                "32", style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))
                        Icon(
                            painter = painterResource(R.drawable.share),
                            null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            "3", style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp)
                    ) {
                        Text(
                            "Style that tells a story", style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))

                    Row(
                        modifier = Modifier.padding(start = 10.dp)
                    ) {
                        Text(
                            "5 days ago", style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet Popup
    if (showBottomSheet && selectedPostImage != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF2D2D2D)
        ) {
            BottomSheetContent(
                imageRes = selectedPostImage!!,
                onDismiss = { showBottomSheet = false }
            )
        }
    }
}

@Composable
fun BottomSheetContent(imageRes: Int, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Post Image
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF404040))
                .clickable {
                    // Handle save action
                    onDismiss()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.edit),
                    contentDescription = "Save",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Save",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About this account
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Handle about this account action
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF404040)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.edit),
                    contentDescription = "Account",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "About this account",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProductCard(imageRes: Int, title: String, price: String) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Box {
            Image(
                painter = painterResource(imageRes),
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
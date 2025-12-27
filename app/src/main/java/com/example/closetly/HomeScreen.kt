package com.example.closetly

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

@Composable
fun HomeScreen() {

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
//            .padding(16.dp)
            .background(White)
//            .verticalScroll(scrollState)
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

                        // Profile name and View Profile button overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxSize()
                        ) {
                            // View Profile button at top right corner
                            androidx.compose.material3.Button(
                                onClick = { },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 16.dp, end = 16.dp)
                                    .height(36.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD4C4B0)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "View Profile",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Black
                                    )
                                )
                            }

                            // Different name for each slider - left corner
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
                                    // Slide 1 Cards
                                    ProductCard(R.drawable.image4, "Half Jeans", "Rs.299")
                                    ProductCard(R.drawable.image2, "Blue Hoodie", "Rs.799")
                                    ProductCard(R.drawable.image3, "Bebe Tee", "Rs.499")
                                }
                                1 -> {
                                    // Slide 2 Cards
                                    ProductCard(R.drawable.image3, "White Tee", "Rs.399")
                                    ProductCard(R.drawable.image1, "Denim Jacket", "Rs.899")
                                    ProductCard(R.drawable.image4, "Shorts", "Rs.349")
                                }
                                2 -> {
                                    // Slide 3 Cards
                                    ProductCard(R.drawable.image2, "Hoodie", "Rs.699")
                                    ProductCard(R.drawable.image4, "Jeans", "Rs.599")
                                    ProductCard(R.drawable.image1, "Dress", "Rs.799")
                                }
                                3 -> {
                                    // Slide 4 Cards
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
                            modifier = Modifier.size(30.dp)
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
                            painter = painterResource(R.drawable.img),
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
                            modifier = Modifier.size(30.dp)
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
                            painter = painterResource(R.drawable.img),
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
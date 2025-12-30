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
            HorizontalPager(
                count = images.size,
                state = pagerState,
            ) { indexOfImages ->

                Image(
                    painter = painterResource(id = images[indexOfImages]),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(10.dp))

            HorizontalPagerIndicator(
                pagerState = pagerState,
                pageCount = 4,
                activeColor = Color.White,
                inactiveColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
            )
            Card(
                modifier = Modifier
                    .width(100.dp)
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
//                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box {
                    Image(
                        painter = painterResource(R.drawable.image4),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(),
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
                            "Half Jeans",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        )
                        Text(
                            "Rs 245",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 9.sp,
                                color = White
                            )
                        )

                    }
                }
            }
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
                            .fillMaxWidth(),

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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Eco-friendly, wallet-friendly.", style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        )
                    }
                    Row {
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
                            .fillMaxWidth(),

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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "“Style that tells a story”", style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))

                    Row {
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

@Preview
@Composable
fun PreviewHome() {
    HomeScreen()

}
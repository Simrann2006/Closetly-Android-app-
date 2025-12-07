package com.example.closetly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.closetly.ui.theme.Black
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.White

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DashboardBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBody(){
    data class NavItem(val label : String, val image : Int)

    var selectedIndex by remember { mutableIntStateOf(2) }

    val listItem = listOf(
        NavItem(label = "Home", image = R.drawable.home),
        NavItem(label = "Market", image = R.drawable.marketplace),
        NavItem(label = "Closet", image = R.drawable.closet),
        NavItem(label = "Calender", image = R.drawable.calender),
        NavItem(label = "Profile", image = R.drawable.profile)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Scaffold (
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.height(54.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        titleContentColor = Black,
                        actionIconContentColor = Black,
                        containerColor = White,
                        navigationIconContentColor = Black
                    ),
                    title = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Closetly")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_add_24),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(28.dp),
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Image(painter = painterResource(R.drawable.notification),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(22.dp)
                            )
                        }
                        IconButton(onClick = {}) {
                            Image(painter = painterResource(R.drawable.chat),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(22.dp)
                            )
                        }
                    }
                )
            },
            bottomBar = { }
        ){ padding ->
            Box (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(color = Brown)
            ){
                when(selectedIndex){
                    0 -> HomeScreen()
                    1 -> MarketplaceScreen()
                    2 -> ClosetScreen()
                    3 -> CalendarScreen()
                    4 -> ProfileScreen()
                    else -> HomeScreen()
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .zIndex(10f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .align(Alignment.BottomCenter)
                    .shadow(
                        elevation = 12.dp,
                        spotColor = Color.Black.copy(alpha = 0.3f),
                        clip = false
                    )
                    .drawBehind {
                        val path = Path().apply {
                            moveTo(0f, size.height)

                            lineTo(0f, size.height * 0.3f)

                            quadraticBezierTo(
                                0f, 0f,
                                size.width * 0.1f, 0f
                            )

                            lineTo(size.width * 0.35f, 0f)

                            quadraticBezierTo(
                                size.width * 0.40f, 0f,
                                size.width * 0.42f, size.height * 0.25f
                            )


                            quadraticBezierTo(
                                size.width * 0.45f, size.height * 0.45f,
                                size.width * 0.50f, size.height * 0.50f
                            )

                            quadraticBezierTo(
                                size.width * 0.55f, size.height * 0.45f,
                                size.width * 0.58f, size.height * 0.25f
                            )

                            quadraticBezierTo(
                                size.width * 0.60f, 0f,
                                size.width * 0.65f, 0f
                            )

                            lineTo(size.width * 0.9f, 0f)

                            quadraticBezierTo(
                                size.width, 0f,
                                size.width, size.height * 0.3f
                            )

                            lineTo(size.width, size.height)

                            close()
                        }

                        drawPath(
                            path = path,
                            color = Brown
                        )
                    }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedIndex = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = if (selectedIndex == 0) Modifier.offset(y = (-8).dp) else Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(listItem[0].image),
                            contentDescription = listItem[0].label,
                            colorFilter = ColorFilter.tint(White),
                            modifier = Modifier.size(if (selectedIndex == 0) 28.dp else 26.dp)
                        )

                        AnimatedVisibility(visible = selectedIndex == 0) {
                            Text(
                                text = listItem[0].label,
                                modifier = Modifier.padding(top = 4.dp),
                                color = White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedIndex = 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = if (selectedIndex == 1) Modifier.offset(y = (-8).dp) else Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(listItem[1].image),
                            contentDescription = listItem[1].label,
                            colorFilter = ColorFilter.tint(White),
                            modifier = Modifier.size(if (selectedIndex == 1) 28.dp else 26.dp)
                        )

                        AnimatedVisibility(visible = selectedIndex == 1) {
                            Text(
                                text = listItem[1].label,
                                modifier = Modifier.padding(top = 4.dp),
                                color = White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .offset(y = (-30).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(65.dp)
                            .shadow(10.dp, CircleShape)
                            .background(White, CircleShape)
                            .border(4.dp, Brown, CircleShape)
                            .clickable { selectedIndex = 2 },
                        contentAlignment = Center
                    ) {
                        Image(
                            painter = painterResource(listItem[2].image),
                            contentDescription = listItem[2].label,
                            colorFilter = ColorFilter.tint(Brown),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedIndex = 3 },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = if (selectedIndex == 3) Modifier.offset(y = (-8).dp) else Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(listItem[3].image),
                            contentDescription = listItem[3].label,
                            colorFilter = ColorFilter.tint(White),
                            modifier = Modifier.size(if (selectedIndex == 3) 28.dp else 26.dp)
                        )

                        AnimatedVisibility(visible = selectedIndex == 3) {
                            Text(
                                text = listItem[3].label,
                                modifier = Modifier.padding(top = 4.dp),
                                color = White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedIndex = 4 },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = if (selectedIndex == 4) Modifier.offset(y = (-8).dp) else Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(listItem[4].image),
                            contentDescription = listItem[4].label,
                            colorFilter = ColorFilter.tint( White),
                            modifier = Modifier.size(if (selectedIndex == 4) 28.dp else 26.dp)
                        )

                        AnimatedVisibility(visible = selectedIndex == 4) {
                            Text(
                                text = listItem[4].label,
                                modifier = Modifier.padding(top = 4.dp),
                                color = White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewDashboard() {
    DashboardBody()
}
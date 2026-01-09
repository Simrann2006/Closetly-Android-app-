package com.example.closetly

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.closetly.ui.theme.Black
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.Grey
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
fun DashboardBody() {
    data class NavItem(val label: String, val image: Int)

    var selectedIndex by remember { mutableIntStateOf(0) }
    var unreadNotifications by remember { mutableStateOf(3) } // example unread notifications

    val listItem = listOf(
        NavItem(label = "Home", image = R.drawable.home),
        NavItem(label = "Market", image = R.drawable.marketplace),
        NavItem(label = "Closet", image = R.drawable.closet),
        NavItem(label = "Calender", image = R.drawable.calendar),
        NavItem(label = "Profile", image = R.drawable.profile)
    )

    val context = LocalContext.current

    Scaffold(
        topBar = {
            if (selectedIndex == 4) {
                CenterAlignedTopAppBar(
                    modifier = Modifier.height(65.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        titleContentColor = Black,
                        actionIconContentColor = Black,
                        containerColor = White,
                        navigationIconContentColor = Black
                    ),
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_add_24),
                                contentDescription = "Add",
                                modifier = Modifier.size(22.dp),
                                tint = Black
                            )
                        }
                    },
                    title = {
                        Text(
                            "Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Cursive
                        )
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_menu_24),
                                contentDescription = "Settings",
                                modifier = Modifier.size(22.dp),
                                tint = Black
                            )
                        }
                    }
                )
            } else {
                TopAppBar(
                    modifier = Modifier.height(65.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        titleContentColor = Black,
                        actionIconContentColor = Black,
                        containerColor = White,
                        navigationIconContentColor = Black
                    ),
                    title = {
                        Text(
                            when (selectedIndex) {
                                0 -> "Closetly"
                                1 -> "Marketplace"
                                2 -> "My Closet"
                                3 -> "Calendar"
                                else -> "Closetly"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            fontFamily = FontFamily.Cursive
                        )
                    },
                    actions = {
                        when (selectedIndex) {
                            0 -> {
                                // 🔔 Notification Bell with Red Dot
                                IconButton(onClick = {
                                    context.startActivity(Intent(context, NotificationActivity::class.java))
                                    unreadNotifications = 0 // optional: clear unread on click
                                }) {
                                    Box {
                                        Image(
                                            painter = painterResource(R.drawable.notification),
                                            contentDescription = "Notifications",
                                            modifier = Modifier.size(22.dp)
                                        )
                                        if (unreadNotifications > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(Color.Red, shape = RoundedCornerShape(4.dp))
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = 6.dp, y = (-2).dp)
                                            )
                                        }
                                    }
                                }
                                IconButton(onClick = {}) {
                                    Image(
                                        painter = painterResource(R.drawable.chat),
                                        contentDescription = "Messages",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            1 -> {
                                IconButton(onClick = {}) {
                                    Icon(
                                        painter = painterResource(R.drawable.chat),
                                        contentDescription = "Filter",
                                        modifier = Modifier.size(22.dp),
                                        tint = Black
                                    )
                                }
                            }
                            2 -> {
                                IconButton(onClick = {}) {
                                    Icon(
                                        painter = painterResource(R.drawable.chat),
                                        contentDescription = "Add Clothes",
                                        modifier = Modifier.size(22.dp),
                                        tint = Black
                                    )
                                }
                            }
                            3 -> {
                                IconButton(onClick = {}) {
                                    Icon(
                                        painter = painterResource(R.drawable.chat),
                                        contentDescription = "Today",
                                        modifier = Modifier.size(22.dp),
                                        tint = Black
                                    )
                                }
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = White,
                modifier = Modifier
                    .height(115.dp)
                    .border(
                        width = 1.dp,
                        color = Grey.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(0.dp)
                    )
            ) {
                listItem.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    NavigationBarItem(
                        icon = {
                            Column(
                                modifier = if (isSelected) Modifier.offset(y = (-8).dp) else Modifier,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(item.image),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                AnimatedVisibility(visible = isSelected) {
                                    Text(
                                        text = item.label,
                                        modifier = Modifier.padding(top = 4.dp),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        },
                        label = null,
                        onClick = {
                            selectedIndex = index
                        },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = Brown,
                            selectedTextColor = Brown,
                            unselectedIconColor = Black,
                            unselectedTextColor = Black
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedIndex) {
                0 -> HomeScreen()
                1 -> MarketplaceScreen()
                2 -> ClosetScreen()
                3 -> CalendarScreen()
                4 -> ProfileScreen()
                else -> HomeScreen()
            }
        }
    }
}

@Composable
@Preview
fun PreviewDashboard() {
    DashboardBody()
}

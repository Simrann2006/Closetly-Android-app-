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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.closetly.ui.theme.Black
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.Grey
import com.example.closetly.ui.theme.White
import com.google.firebase.database.*

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

    val listItem = listOf(
        NavItem(label = "Home", image = R.drawable.home),
        NavItem(label = "Market", image = R.drawable.marketplace),
        NavItem(label = "Closet", image = R.drawable.closet),
        NavItem(label = "Calendar", image = R.drawable.calendar),
        NavItem(label = "Profile", image = R.drawable.profile)
    )

    val context = LocalContext.current

    // Firebase unread notifications count
    var unreadCount by remember { mutableStateOf(0) }
    val database = FirebaseDatabase.getInstance().getReference("notifications")
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                for (child in snapshot.children) {
                    val read = child.child("read").getValue(Boolean::class.java) ?: false
                    if (!read) count++
                }
                unreadCount = count
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.height(65.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = Black,
                    actionIconContentColor = Black,
                    containerColor = White,
                    navigationIconContentColor = Black
                ),
                title = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Closetly", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                actions = {
                    // Notification icon with badge
                    Box {
                        IconButton(onClick = {
                            context.startActivity(Intent(context, NotificationActivity::class.java))
                        }) {
                            Image(
                                painter = painterResource(R.drawable.notification),
                                contentDescription = "Notifications",
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .background(Color.Red, shape = RoundedCornerShape(6.dp))
                            )
                        }
                    }

                    IconButton(onClick = {
                        // Chat functionality if you add ChatActivity
                        // context.startActivity(Intent(context, ChatActivity::class.java))
                    }) {
                        Image(
                            painter = painterResource(R.drawable.chat),
                            contentDescription = "Chat",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )
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

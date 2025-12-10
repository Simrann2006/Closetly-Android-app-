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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class NavItem(val label: String, val image: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBody(){

    var selectedIndex by remember { mutableIntStateOf(2) }

    val listItem = listOf(
        NavItem(label = "Home", image = R.drawable.home),
        NavItem(label = "Market", image = R.drawable.marketplace),
        NavItem(label = "Closet", image = R.drawable.closet),
        NavItem(label = "Calender", image = R.drawable.calendar),
        NavItem(label = "Profile", image = R.drawable.profile)
    )

    Scaffold (
        topBar = {
            TopAppBar(
                modifier = Modifier.height(60.dp),
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
                        Text("Closetly")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Image(
                            painter = painterResource(R.drawable.notification),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = {}) {
                        Image(
                            painter = painterResource(R.drawable.chat),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            CustomBottomNavigation(
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it },
                listItem = listItem
            )
        }
    ){ padding ->
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
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
}

@Composable
fun CustomBottomNavigation(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    listItem: List<NavItem>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
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
                        quadraticBezierTo(0f, 0f, size.width * 0.1f, 0f)
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
                    drawPath(path = path, color = Brown)
                }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listItem.take(2).forEachIndexed { index, item ->
                NavItemButton(
                    item = item,
                    isSelected = selectedIndex == index,
                    onClick = { onItemSelected(index) }
                )
            }

            Box(
                modifier = Modifier
                    .offset(y = (-30).dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(65.dp)
                        .shadow(10.dp, CircleShape)
                        .background(White, CircleShape)
                        .border(4.dp, Brown, CircleShape)
                        .clickable { onItemSelected(2) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(listItem[2].image),
                        contentDescription = listItem[2].label,
                        colorFilter = ColorFilter.tint(Brown),
                        modifier = Modifier.size(45.dp)
                    )
                }
            }

            listItem.takeLast(2).forEachIndexed { index, item ->
                val actualIndex = index + 3
                NavItemButton(
                    item = item,
                    isSelected = selectedIndex == actualIndex,
                    onClick = { onItemSelected(actualIndex) }
                )
            }
        }
    }
}

@Composable
fun RowScope.NavItemButton(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = if (isSelected) Modifier.offset(y = (-8).dp) else Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(item.image),
                contentDescription = item.label,
                colorFilter = ColorFilter.tint(White),
                modifier = Modifier.size(if (isSelected) 28.dp else 26.dp)
            )

            AnimatedVisibility(visible = isSelected) {
                Text(
                    text = item.label,
                    modifier = Modifier.padding(top = 4.dp),
                    color = White,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
@Preview
fun PreviewDashboard() {
    DashboardBody()
}
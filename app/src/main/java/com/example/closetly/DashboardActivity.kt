package com.example.closetly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.closetly.ui.theme.Black
import com.example.closetly.ui.theme.Light_brown
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

    var selectedIndex by remember { mutableIntStateOf(0) }

    val listItem = listOf(
        NavItem(label = "Home", image = R.drawable.home),
        NavItem(label = "Market", image = R.drawable.marketplace),
        NavItem(label = "Closet", image = R.drawable.closet),
        NavItem(label = "Calender", image = R.drawable.calender),
        NavItem(label = "Profile", image = R.drawable.profile)
    )

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
        bottomBar = {
            Box(
                modifier = Modifier
                    .height(56.dp)
                    .background(White, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listItem.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(11f)
                                .clickable {
                                    selectedIndex = index
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                if (selectedIndex == index) Modifier.offset(y = (-15).dp)
                                else Modifier,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) Light_brown else Color.Transparent,
                                            CircleShape
                                        )
                                        .size(36.dp),
                                    contentAlignment = Center
                                ) {
                                    Image(
                                        painter = painterResource(item.image),
                                        contentDescription = item.label,
                                        colorFilter = ColorFilter.tint(Black),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                AnimatedVisibility(visible = isSelected) {
                                    Text(
                                        text = item.label,
                                        modifier = Modifier.padding(top = 4.dp),
                                        color = Black,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ){ padding ->
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
@Preview
fun PreviewDashboard() {
    DashboardBody()
}
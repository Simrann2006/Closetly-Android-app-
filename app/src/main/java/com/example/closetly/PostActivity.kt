package com.example.closetly

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class PostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PostBody()
        }
    }
}

@Composable
fun PostBody(){

    val context = LocalContext.current
    val activity = context as Activity
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Posts", "Listings")

    Scaffold { padding ->
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth(),

                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,

                ){
                IconButton(
                    onClick = {
                        val intent = Intent(context, DashboardActivity::class.java)
                        context.startActivity(intent)
                    }
                ){
                    Icon(
                        painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                        null,
                    )
                }
                Text("Kendall_02", style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ))
                Icon(
                    painter = painterResource(R.drawable.edit),
                    null,
                    modifier = Modifier.size(30.dp)
                )
            }
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),

                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Image(
                    painter = painterResource(R.drawable.image1),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.LightGray, CircleShape)
                )

                Column (
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text("3",style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ))
                    Text("Posts", style = TextStyle(
                        fontSize = 14.sp
                    ))
                }
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text("1,456",style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ))
                    Text("Followers", style = TextStyle(
                        fontSize = 14.sp
                    ))
                }
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text("11",style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ))
                    Text("Following", style = TextStyle(
                        fontSize = 14.sp
                    ))
                }
            }
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ){
                Text("kendall", style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ))
                Text("Sustainable style for every soul.",style = TextStyle(
                    fontSize = 14.sp
                ))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),

                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){
                Button(
                    onClick = {
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.purple_200),
                    ),
                    shape = RoundedCornerShape(8.dp),

                    ) {
                    Text("Follow",style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ))
                }
                Button(
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.purple_200),
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Message",style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> PostsGrid(context)
                1 -> ListingsGrid(context)
            }
        }
    }
}

@Composable
fun PostsGrid(context: android.content.Context) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 2.dp)
    ) {
        items(2) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                for (colIndex in 0 until 3) {
                    val index = rowIndex * 3 + colIndex
                    if (index < 3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(1.dp)
                                .background(Color.LightGray)
                                .clickable {
                                }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ListingsGrid(context: android.content.Context) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 2.dp)
    ) {
        items(3) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                for (colIndex in 0 until 3) {
                    val index = rowIndex * 3 + colIndex
                    if (index < 4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(1.dp)
                                .background(Color.LightGray)
                                .clickable {
                                }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PostBodyPreview(){
    PostBody()
}
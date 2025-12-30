package com.example.closetly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.model.NotificationModel
import com.example.closetly.model.NotificationType
import com.example.closetly.ui.theme.ClosetlyTheme
import com.google.firebase.database.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon




class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClosetlyTheme {
                NotificationScreen(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onBackClick: () -> Unit) {

    val database = FirebaseDatabase.getInstance().getReference("notifications")
    var notifications by remember { mutableStateOf(listOf<NotificationModel>()) }

    // Fetch notifications from Firebase
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<NotificationModel>()
                for (child in snapshot.children) {
                    val userName = child.child("userName").getValue(String::class.java) ?: ""
                    val userProfileImage = child.child("userProfileImage").getValue(String::class.java) ?: ""
                    val typeStr = child.child("type").getValue(String::class.java) ?: "FOLLOW"
                    val type = NotificationType.valueOf(typeStr)
                    val message = child.child("message").getValue(String::class.java) ?: ""
                    val time = child.child("time").getValue(String::class.java) ?: ""

                    val notification = NotificationModel(
                        userName = userName,
                        userProfileImage = userProfileImage,
                        type = type,
                        message = message,
                        time = time
                    )
                    list.add(notification)
                }
                notifications = list.reversed() // latest first
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        topBar = {TopAppBar(
            title = {
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Notifications",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Image(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItem(notification)
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        ) {
            if (notification.userProfileImage.toString().matches(Regex("\\d+"))) {
                Image(
                    painter = painterResource(id = notification.userProfileImage.toString().toInt()),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = notification.userProfileImage.toString(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${notification.userName} ${notification.message}",
                fontSize = 16.sp
            )
            Text(
                text = notification.time,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        when (notification.type) {
            NotificationType.FOLLOW -> {
                Button(onClick = { /* Follow back */ }) {
                    Text("Follow")
                }
            }
            NotificationType.LIKE -> {
                Icon(
                    painter = painterResource(R.drawable.heart),
                    contentDescription = "Liked",
                    modifier = Modifier.size(24.dp)
                )
            }


        }
    }
}

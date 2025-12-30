package com.example.closetly

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

class NotificationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClosetlyTheme {
                NotificationScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(onBackClick: () -> Unit) {

    // Firebase reference
    val database = FirebaseDatabase.getInstance().getReference("notifications")
    var notifications by remember { mutableStateOf(listOf<NotificationModel>()) }

    // Fetch notifications from Firebase
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<NotificationModel>()
                for (child in snapshot.children) {
                    val notification = child.getValue(NotificationModel::class.java)
                    notification?.let { list.add(it) }
                }
                notifications = list.reversed() // show latest first
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Notifications") } },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {}
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Background image
            Image(
                painter = painterResource(R.drawable.registrationbg), // your background drawable
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

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
        // Profile image
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        ) {
            if (notification.userProfileImage is Int) {
                Image(
                    painter = painterResource(id = notification.userProfileImage),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (notification.userProfileImage is String) {
                AsyncImage(
                    model = notification.userProfileImage,
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
                Button(onClick = { /* Follow back logic */ }) {
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

            else -> {}
        }
    }
}

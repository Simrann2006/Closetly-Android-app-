package com.example.closetly

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.model.NotificationModel
import com.example.closetly.model.NotificationType
import com.example.closetly.ui.theme.Black
import com.example.closetly.ui.theme.ClosetlyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClosetlyTheme {
                NotificationScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {
    val database = FirebaseDatabase.getInstance().getReference("notifications")
    var notifications by remember { mutableStateOf(listOf<NotificationModel>()) }

    // Fetch notifications
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<NotificationModel>()
                for (child in snapshot.children) {
                    val userName = child.child("userName").getValue(String::class.java) ?: ""
                    val userProfileImage = child.child("userProfileImage").getValue(Any::class.java) ?: ""
                    val typeStr = child.child("type").getValue(String::class.java) ?: "FOLLOW"
                    val type = NotificationType.valueOf(typeStr)
                    val message = child.child("message").getValue(String::class.java) ?: ""
                    val time = child.child("time").getValue(String::class.java) ?: ""
                    val isRead = child.child("isRead").getValue(Boolean::class.java) ?: false
                    val senderId = child.child("senderId").getValue(String::class.java) ?: ""

                    list.add(NotificationModel(userName, userProfileImage, type, message, time, isRead, senderId))
                }
                notifications = list.reversed()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            "Notifications",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Cursive,
                            color = Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (notifications.isEmpty()) {
                NotificationEmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationItem(notification)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.nf),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 310.dp)
        ) {
            Text(
                "No notifications",
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "We’ll let you know when there’s something new to update you",
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationModel) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().reference
    var isFollowing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val snapshot = database.child("users")
            .child(currentUserId)
            .child("following")
            .child(notification.senderId)
            .get().await()
        isFollowing = snapshot.exists() && snapshot.getValue(Boolean::class.java) == true
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (!notification.isRead) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
            .clickable {
                notification.isRead = true
                database.child("notifications").child(notification.time)
                    .child("isRead").setValue(true)
            },
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
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = notification.time,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        when (notification.type) {
            NotificationType.FOLLOW -> {
                Button(
                    onClick = {
                        if (!isFollowing) {
                            database.child("users").child(currentUserId)
                                .child("following").child(notification.senderId).setValue(true)
                            database.child("users").child(notification.senderId)
                                .child("followers").child(currentUserId).setValue(true)
                            database.child("notifications").child(notification.time)
                                .child("isRead").setValue(true)
                            isFollowing = true
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) { Text(if (isFollowing) "Following" else "Follow") }
            }
            NotificationType.LIKE -> {
                Icon(
                    painter = painterResource(R.drawable.heart),
                    contentDescription = "Liked",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            NotificationType.MENTION -> {
                Icon(
                    painter = painterResource(R.drawable.mention),
                    contentDescription = "Mentioned",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            NotificationType.POST -> {
                Icon(
                    painter = painterResource(R.drawable.baseline_add_24),
                    contentDescription = "New Post",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

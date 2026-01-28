package com.example.closetly.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.model.NotificationModel
import com.example.closetly.model.NotificationType
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.closetly.R
import com.example.closetly.repository.PostRepoImpl
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.utils.ThemeManager
import com.example.closetly.utils.getTimeAgoShort
import com.example.closetly.viewmodel.PostViewModel
import com.example.closetly.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            ClosetlyTheme(darkTheme = ThemeManager.isDarkMode) {
                NotificationScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val database = FirebaseDatabase.getInstance().getReference("Notifications").child(currentUserId)
    var notifications by remember { mutableStateOf(listOf<Pair<String, NotificationModel>>()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var notificationToDelete by remember { mutableStateOf<Pair<String, NotificationModel>?>(null) }

    LaunchedEffect(Unit) {
        database.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Pair<String, NotificationModel>>()
                for (child in snapshot.children) {
                    val notificationId = child.key ?: continue
                    val userName = child.child("userName").getValue(String::class.java) ?: ""
                    val userProfileImage = child.child("userProfileImage").getValue(String::class.java) ?: ""
                    val typeStr = child.child("type").getValue(String::class.java) ?: "FOLLOW"
                    val type = try {
                        NotificationType.valueOf(typeStr)
                    } catch (e: Exception) {
                        NotificationType.FOLLOW
                    }
                    val message = child.child("message").getValue(String::class.java) ?: ""
                    val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                    val time = getTimeAgoShort(timestamp)
                    val isRead = child.child("isRead").getValue(Boolean::class.java) ?: false
                    val senderId = child.child("senderId").getValue(String::class.java) ?: ""
                    val postId = child.child("postId").getValue(String::class.java) ?: ""
                    val postImage = child.child("postImage").getValue(String::class.java) ?: ""
                    val commentText = child.child("commentText").getValue(String::class.java) ?: ""

                    val notificationModel = NotificationModel(userName, userProfileImage, type, message, time, isRead, senderId, postId, postImage, commentText, timestamp)
                    list.add(Pair(notificationId, notificationModel))
                }
                notifications = list.reversed()
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    }

    LaunchedEffect(Unit) {
        database.get().addOnSuccessListener { snapshot ->
            for (child in snapshot.children) {
                val isRead = child.child("isRead").getValue(Boolean::class.java) ?: false
                if (!isRead) {
                    child.ref.child("isRead").setValue(true)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ThemeManager.isDarkMode) OnBackground_Dark else Brown
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = "Back",
                            tint = if (ThemeManager.isDarkMode) OnBackground_Dark else Brown
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (ThemeManager.isDarkMode) Background_Dark else Background_Light
                )
            )
        },
        containerColor = if (ThemeManager.isDarkMode) Background_Dark else Background_Light
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Brown)
                    }
                }
                notifications.isEmpty() -> {
                    NotificationEmptyState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(notifications) { notificationPair ->
                            NotificationItem(
                                notificationId = notificationPair.first,
                                notification = notificationPair.second,
                                onLongPress = {
                                    notificationToDelete = notificationPair
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && notificationToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
            title = {
                Text(
                    "Delete Notification",
                    fontWeight = FontWeight.Bold,
                    color = if (ThemeManager.isDarkMode) OnSurface_Dark else Brown
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete this notification?",
                    color = if (ThemeManager.isDarkMode) OnSurface_Dark.copy(alpha = 0.7f) else OnSurface_Light.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        notificationToDelete?.let { (id, _) ->
                            database.child(id).removeValue()
                        }
                        showDeleteDialog = false
                        notificationToDelete = null
                    }
                ) {
                    Text("Delete", color = if (ThemeManager.isDarkMode) Error_Dark else Error_Light, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        notificationToDelete = null
                    }
                ) {
                    Text("Cancel", color = if (ThemeManager.isDarkMode) OnSurface_Dark.copy(alpha = 0.7f) else Grey)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
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
                    color = if (ThemeManager.isDarkMode) OnBackground_Dark.copy(alpha = 0.7f) else Grey,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "We'll let you know when there's something new to update you",
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (ThemeManager.isDarkMode) OnSurface_Dark.copy(alpha = 0.6f) else Grey,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationItem(
    notificationId: String,
    notification: NotificationModel,
    onLongPress: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val userRepo = remember { UserRepoImpl(context) }
    val userViewModel = remember { UserViewModel(userRepo) }
    var isFollowing by remember { mutableStateOf(false) }

    LaunchedEffect(notification.senderId) {
        userViewModel.isFollowing(currentUserId, notification.senderId) { following ->
            isFollowing = following
        }
    }

    if (notification.type == NotificationType.POST) {
        SalePostNotificationCard(notification, context, onLongPress, haptic)
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (ThemeManager.isDarkMode) Background_Dark else Background_Light)
                .combinedClickable(
                    onClick = {
                        when (notification.type) {
                            NotificationType.FOLLOW -> {
                                val intent = Intent(context, UserProfileActivity::class.java).apply {
                                    putExtra("userId", notification.senderId)
                                    putExtra("username", notification.userName)
                                }
                                context.startActivity(intent)
                            }
                            NotificationType.LIKE, NotificationType.COMMENT -> {
                                if (notification.postId.isNotEmpty()) {
                                    val postRepo = PostRepoImpl(context)
                                    val postViewModel = PostViewModel(postRepo)

                                    postViewModel.getUserPosts(currentUserId) { posts ->
                                        val postIndex = posts.indexOfFirst { it.postId == notification.postId }
                                        if (postIndex != -1) {
                                            val intent = Intent(context, PostFeedActivity::class.java).apply {
                                                putExtra("USER_ID", currentUserId)
                                                putExtra("INITIAL_INDEX", postIndex)
                                            }
                                            context.startActivity(intent)
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (ThemeManager.isDarkMode) Surface_Dark else Light_grey1)
            ) {
                if (notification.userProfileImage.toString().isNotEmpty()) {
                    AsyncImage(
                        model = notification.userProfileImage,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.baseline_person_24),
                        contentDescription = null,
                        tint = if (ThemeManager.isDarkMode) OnSurface_Dark else Grey,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = if (ThemeManager.isDarkMode) OnBackground_Dark else Brown)) {
                            append(notification.userName)
                        }
                        withStyle(style = SpanStyle(color = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light)) {
                            append(" ")
                            append(notification.message)
                        }
                    },
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notification.time,
                    fontSize = 12.sp,
                    color = if (ThemeManager.isDarkMode) OnSurface_Dark.copy(alpha = 0.6f) else Grey
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            when (notification.type) {
                NotificationType.FOLLOW -> {
                    if (notification.senderId != currentUserId) {
                        Button(
                            onClick = {
                                userViewModel.toggleFollow(currentUserId, notification.senderId) { success, _ -> }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) 
                                    (if (ThemeManager.isDarkMode) Surface_Dark else Light_grey1) 
                                else 
                                    Brown
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (isFollowing) "Following" else "Follow",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isFollowing) 
                                    (if (ThemeManager.isDarkMode) OnSurface_Dark else Brown) 
                                else 
                                    White
                            )
                        }
                    }
                }
                NotificationType.LIKE, NotificationType.COMMENT -> {
                    if (notification.postImage.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            AsyncImage(
                                model = notification.postImage,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                else -> {}
            }
        }

        Divider(
            color = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1.copy(alpha = 0.5f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(start = 76.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SalePostNotificationCard(
    notification: NotificationModel,
    context: Context,
    onLongPress: () -> Unit,
    haptic: HapticFeedback
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .combinedClickable(
                onClick = {
                    if (notification.postId.isNotEmpty()) {
                        val intent = Intent(context, PostFeedActivity::class.java).apply {
                            putExtra("USER_ID", notification.senderId)
                            putExtra("INITIAL_INDEX", 0)
                        }
                        context.startActivity(intent)
                    }
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ThemeManager.isDarkMode) Surface_Dark else White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (ThemeManager.isDarkMode) Surface_Dark else Light_grey1)
                ) {
                    if (notification.userProfileImage.toString().isNotEmpty()) {
                        AsyncImage(
                            model = notification.userProfileImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.baseline_person_24),
                            contentDescription = null,
                            tint = if (ThemeManager.isDarkMode) OnSurface_Dark else Grey,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_local_offer_24),
                            contentDescription = null,
                            tint = Brown,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SALE ALERT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brown,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = if (ThemeManager.isDarkMode) OnSurface_Dark else Brown)) {
                                append(notification.userName)
                            }
                            withStyle(style = SpanStyle(color = if (ThemeManager.isDarkMode) OnSurface_Dark.copy(alpha = 0.8f) else OnSurface_Light.copy(alpha = 0.8f))) {
                                append(" posted a sale item")
                            }
                        },
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (notification.postImage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (ThemeManager.isDarkMode) Surface_Dark else Light_grey1)
                ) {
                    AsyncImage(
                        model = notification.postImage,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.Red, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SALE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = White,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_shopping_bag_24),
                        contentDescription = null,
                        tint = Brown,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Grab this opportunity • ${notification.time}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Brown
                    )
                }
            }
        }
    }
}
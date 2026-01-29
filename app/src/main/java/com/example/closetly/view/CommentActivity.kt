package com.example.closetly.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.R
import com.example.closetly.model.CommentModel
import com.example.closetly.ui.theme.Background_Dark
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.Surface_Dark
import com.example.closetly.utils.ThemeManager
import com.example.closetly.utils.getTimeAgoShort
import com.example.closetly.viewmodel.CommentViewModel
import kotlinx.coroutines.launch

class CommentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val postId = intent.getStringExtra("POST_ID") ?: "post_1"

        setContent {
            CommentScreen(
                postId = postId,
                onBackClick = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(
    postId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { CommentViewModel(context) }
    val comments by viewModel.comments.collectAsState()
    val filteredComments = comments.filter { it.userName != "User" && it.userId.isNotEmpty() }
    val commentCount = filteredComments.size
    val isLoading by viewModel.isLoading.collectAsState()
    val commentText by viewModel.commentText.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val listState = rememberLazyListState()

    var selectedCommentForDelete by remember { mutableStateOf<CommentModel?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(postId) {
        viewModel.loadComments(postId)
    }

    if (selectedCommentForDelete != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedCommentForDelete = null },
            sheetState = sheetState,
            containerColor = if (ThemeManager.isDarkMode) Surface_Dark else Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                selectedCommentForDelete?.let { comment ->
                                    viewModel.deleteComment(comment.id, postId)
                                    selectedCommentForDelete = null
                                    sheetState.hide()
                                }
                            }
                        }
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Delete Comment",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Red
                        )
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Comments",
                            fontWeight = FontWeight.Bold,
                            color = if (ThemeManager.isDarkMode) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (commentCount > 0) {
                            Text(
                                text = commentCount.toString(),
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            "Back",
                            tint = if (ThemeManager.isDarkMode) Color.White else Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (ThemeManager.isDarkMode) Background_Dark else Color.White
                )
            )
        },
        bottomBar = {
            CommentInputSection(
                commentText = commentText,
                currentUserProfileImage = currentUserProfile.second,
                onCommentChange = { viewModel.updateCommentText(it) },
                onSendClick = {
                    viewModel.postComment(postId = postId)
                },
                isDarkMode = ThemeManager.isDarkMode
            )
        },
        containerColor = if (ThemeManager.isDarkMode) Background_Dark else Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(if (ThemeManager.isDarkMode) Background_Dark else Color.White)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = if (ThemeManager.isDarkMode) Color.White else Brown
                )
            } else if (comments.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No comments yet",
                        style = TextStyle(
                            fontSize = 17.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Be the first to share your thoughts!",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp, top = 8.dp)
                ) {
                    items(
                        items = filteredComments,
                        key = { it.id }
                    ) { comment ->
                        CommentItem(
                            comment = comment,
                            currentUserId = viewModel.getCurrentUserId(),
                            isCurrentUser = viewModel.isCurrentUserComment(comment.userId),
                            onCommentLongPress = {
                                if (viewModel.isCurrentUserComment(comment.userId)) {
                                    selectedCommentForDelete = comment
                                }
                            },
                            onLikeClick = {
                                viewModel.likeComment(comment.id, postId)
                            },
                            isDarkMode = ThemeManager.isDarkMode
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: CommentModel,
    currentUserId: String,
    isCurrentUser: Boolean,
    onCommentLongPress: () -> Unit,
    onLikeClick: () -> Unit,
    isDarkMode: Boolean = false
) {
    val isLiked = comment.isLikedBy(currentUserId)

    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        if (isCurrentUser) {
                            onCommentLongPress()
                        }
                    },
                    onTap = {
                        if (isCurrentUser) {
                            onCommentLongPress()
                        }
                    }
                )
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Profile Picture with placeholder support
        if (comment.userProfileImage.isNotEmpty()) {
            AsyncImage(
                model = comment.userProfileImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.dp, if (isDarkMode) Color.Gray else Color.LightGray, CircleShape)
                    .let { baseModifier ->
                        if (comment.userName != "User" && comment.userId.isNotEmpty()) {
                            baseModifier.clickable {
                                val intent = android.content.Intent(context, UserProfileActivity::class.java).apply {
                                    putExtra("userId", comment.userId)
                                    putExtra("username", comment.userName)
                                }
                                context.startActivity(intent)
                            }
                        } else baseModifier
                    }
            )
        } else {
            // Default placeholder profile image
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) Color.DarkGray else Color.LightGray)
                    .border(1.dp, if (isDarkMode) Color.Gray else Color.LightGray, CircleShape)
                    .let { baseModifier ->
                        if (comment.userName != "User" && comment.userId.isNotEmpty()) {
                            baseModifier.clickable {
                                val intent = android.content.Intent(context, UserProfileActivity::class.java).apply {
                                    putExtra("userId", comment.userId)
                                    putExtra("username", comment.userName)
                                }
                                context.startActivity(intent)
                            }
                        } else baseModifier
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_person_24),
                    contentDescription = "Default profile",
                    tint = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Comment Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Username and Timestamp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    comment.userName,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkMode) Color.White else Color.Black
                    ),
                    modifier = if (comment.userName != "User" && comment.userId.isNotEmpty()) Modifier.clickable {
                        val intent = android.content.Intent(context, UserProfileActivity::class.java).apply {
                            putExtra("userId", comment.userId)
                            putExtra("username", comment.userName)
                        }
                        context.startActivity(intent)
                    } else Modifier
                )
                Text(
                    "•",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                )
                Text(
                    getTimeAgoShort(comment.timestamp),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                )
            }

            // Comment Text
            Text(
                comment.commentText,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = if (isDarkMode) Color.White else Color.Black,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.padding(top = 3.dp, bottom = 2.dp)
            )
        }

        // Like Button with count
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onLikeClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Like count below like button
            if (comment.likesCount > 0) {
                Text(
                    "${comment.likesCount}",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
fun CommentInputSection(
    commentText: String,
    currentUserProfileImage: String,
    onCommentChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isDarkMode: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        color = if (isDarkMode) Surface_Dark else Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Profile image with placeholder support
            if (currentUserProfileImage.isNotEmpty()) {
                AsyncImage(
                    model = currentUserProfileImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, if (isDarkMode) Color.Gray else Color.LightGray, CircleShape)
                )
            } else {
                // Default placeholder profile image
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color.DarkGray else Color.LightGray)
                        .border(1.dp, if (isDarkMode) Color.Gray else Color.LightGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_person_24),
                        contentDescription = "Default profile",
                        tint = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Add a comment...",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    )
                },
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = if (isDarkMode) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDarkMode) Color.Gray else Color.LightGray,
                    unfocusedBorderColor = if (isDarkMode) Color.Gray.copy(alpha = 0.6f) else Color.LightGray.copy(alpha = 0.6f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = if (isDarkMode) Color.White else Color.Black
                ),
                maxLines = 4,
                singleLine = false
            )

            IconButton(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onSendClick()
                    }
                },
                enabled = commentText.isNotBlank(),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (commentText.isNotBlank())
                        Brown
                    else
                        Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDarkMode: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Delete Comment",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black
                )
            )
        },
        text = {
            Text(
                "Are you sure you want to delete this comment? This action cannot be undone.",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color.Black
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = if (isDarkMode) Color.White else Color.Black
                )
            }
        },
        containerColor = if (isDarkMode) Surface_Dark else Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
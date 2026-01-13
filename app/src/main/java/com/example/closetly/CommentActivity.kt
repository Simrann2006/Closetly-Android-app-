package com.example.closetly

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.closetly.model.CommentModel
import com.example.closetly.viewmodel.CommentViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    onBackClick: () -> Unit,
    viewModel: CommentViewModel = viewModel()
) {
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val commentText by viewModel.commentText.collectAsState()
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
            containerColor = Color.White,
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
                    Text(
                        "Comments",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            CommentInputSection(
                commentText = commentText,
                onCommentChange = { viewModel.updateCommentText(it) },
                onSendClick = {
                    viewModel.postComment(postId = postId)
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (comments.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No comments yet",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Be the first to comment!",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.LightGray
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
                        items = comments,
                        key = { it.id }
                    ) { comment ->
                        CommentItem(
                            comment = comment,
                            isCurrentUser = viewModel.isCurrentUserComment(comment.userId),
                            onCommentLongPress = {
                                if (viewModel.isCurrentUserComment(comment.userId)) {
                                    selectedCommentForDelete = comment
                                }
                            },
                            onLikeClick = {
                                viewModel.likeComment(comment.id, postId)
                            }
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
    isCurrentUser: Boolean,
    onCommentLongPress: () -> Unit,
    onLikeClick: () -> Unit
) {
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = comment.userProfileImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, Color.LightGray, CircleShape)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Username and Timestamp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    comment.userName,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    getTimeAgo(comment.timestamp),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                )
            }

            Text(
                comment.commentText,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Black,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onLikeClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (comment.isLiked) Color.Red else Color.Gray,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            // Like Count beside icon
            if (comment.likesCount > 0) {
                Text(
                    "${comment.likesCount}",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (comment.isLiked) Color.Red else Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
fun CommentInputSection(
    commentText: String,
    onCommentChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.LightGray, CircleShape)
            )

            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        "Add a comment...",
                        style = TextStyle(fontSize = 14.sp)
                    ) 
                },
                textStyle = TextStyle(fontSize = 14.sp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                ),
                maxLines = 4
            )

            IconButton(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onSendClick()
                    }
                },
                enabled = commentText.isNotBlank()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (commentText.isNotBlank()) 
                        androidx.compose.material3.MaterialTheme.colorScheme.primary 
                    else 
                        Color.Gray
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Delete Comment",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Text(
                "Are you sure you want to delete this comment? This action cannot be undone.",
                style = TextStyle(fontSize = 14.sp)
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
                Text("Cancel")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m"
        diff < 86400000 -> "${diff / 3600000}h"
        diff < 604800000 -> "${diff / 86400000}d"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

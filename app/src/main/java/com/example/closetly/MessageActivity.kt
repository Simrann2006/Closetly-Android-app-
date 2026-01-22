package com.example.closetly

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.model.ChatModel
import com.example.closetly.model.UserModel
import com.example.closetly.repository.ChatRepoImpl
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.utils.getTimeAgoShort
import com.example.closetly.viewmodel.ChatViewModel
import com.example.closetly.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class MessageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MessageBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageBody() {
    val context = LocalContext.current
    val chatViewModel = remember { ChatViewModel(ChatRepoImpl()) }
    val userViewModel = remember { UserViewModel(UserRepoImpl(context)) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var searchQuery by remember { mutableStateOf("") }
    var chatList by remember { mutableStateOf<List<Pair<ChatModel, UserModel>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<UserModel?>(null) }
    var selectedChatForAction by remember { mutableStateOf<Pair<ChatModel, UserModel>?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var chatToDelete by remember { mutableStateOf<Pair<ChatModel, UserModel>?>(null) }

    LaunchedEffect(Unit) {
        userViewModel.getUserById(currentUserId) { success, _, user ->
            if (success && user != null) {
                currentUser = user
            }
        }
        chatViewModel.getUserChats(currentUserId) { success, _, chats ->
            isLoading = false
            if (success) {
                chatList = chats.filter { it.first.lastMessage.isNotEmpty() }
            }
        }
    }

    val filteredChats = remember(chatList, searchQuery) {
        if (searchQuery.isBlank()) {
            chatList
        } else {
            chatList.filter {
                it.second.username.contains(searchQuery, ignoreCase = true) ||
                        it.second.fullName.contains(searchQuery, ignoreCase = true) ||
                        it.first.lastMessage.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = Black
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {}
                    ) {
                        Text(
                            text = currentUser?.username ?: "User",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Black
                        )
                    }

                    IconButton(onClick = { showNewChatDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_edit_24),
                            contentDescription = null,
                            tint = Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                    placeholder = {
                        Text("Search users....", color = Grey.copy(alpha = 0.6f))
                    },
                    trailingIcon = {
                        Icon(painter = painterResource(R.drawable.baseline_search_24),
                            contentDescription = null,
                            tint = Black
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Black,
                        focusedBorderColor = Black,
                        unfocusedContainerColor = White,
                        focusedContainerColor = White,
                        unfocusedTextColor = Black,
                        focusedTextColor = Black
                    ),
                    singleLine = true
                )
            }
        },
        containerColor = White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Messages",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Black
                        )
                    }
                    filteredChats.isEmpty() -> {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No results found" else "No messages yet",
                            fontSize = 16.sp,
                            color = Grey,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredChats) { (chat, user) ->
                                MessageListItem(
                                    chat = chat,
                                    user = user,
                                    currentUserId = currentUserId,
                                    onClick = {
                                        val intent = Intent(context, ChatActivity::class.java).apply {
                                            putExtra("chatId", chat.chatId)
                                            putExtra("otherUserId", user.userId)
                                            putExtra("otherUserName", user.username)
                                            putExtra("otherUserImage", user.profilePicture)
                                        }
                                        context.startActivity(intent)
                                    },
                                    onLongPress = {
                                        selectedChatForAction = Pair(chat, user)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewChatDialog) {
        NewChatDialog(
            currentUserId = currentUserId,
            userViewModel = userViewModel,
            chatViewModel = chatViewModel,
            onDismiss = { showNewChatDialog = false },
            onChatCreated = { chatId, user ->
                showNewChatDialog = false
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("chatId", chatId)
                    putExtra("otherUserId", user.userId)
                    putExtra("otherUserName", user.username)
                    putExtra("otherUserImage", user.profilePicture)
                }
                context.startActivity(intent)
            }
        )
    }

    selectedChatForAction?.let { (chat, user) ->
        ChatActionDialog(
            onDismiss = { selectedChatForAction = null },
            onDelete = {
                chatToDelete = Pair(chat, user)
                showDeleteConfirmation = true
                selectedChatForAction = null
            },
        )
    }

    if (showDeleteConfirmation && chatToDelete != null) {
        DeleteChatConfirmationDialog(
            userName = chatToDelete!!.second.username,
            onConfirm = {
                chatViewModel.deleteChat(chatToDelete!!.first.chatId) { success, message ->
                    if (success) {
                        chatList = chatList.filter { it.first.chatId != chatToDelete!!.first.chatId }
                        Toast.makeText(
                            context,
                            "Chat deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Failed: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                showDeleteConfirmation = false
                chatToDelete = null
            },
            onDismiss = {
                showDeleteConfirmation = false
                chatToDelete = null
            }
        )
    }
}

@Composable
fun MessageListItem(
    chat: ChatModel,
    user: UserModel,
    currentUserId: String,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val unreadCount = chat.unreadCount[currentUserId] ?: 0
    val timeAgo = getTimeAgoShort(chat.lastMessageTime)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongPress() }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Light_grey),
            contentAlignment = Alignment.Center
        ) {
            if (user.profilePicture.isNotEmpty()) {
                AsyncImage(
                    model = user.profilePicture,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.baseline_person_24),
                    contentDescription = null,
                    tint = Grey,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.username,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Black
                )

                if (timeAgo.isNotEmpty()) {
                    Text(
                        text = timeAgo,
                        fontSize = 12.sp,
                        color = Grey,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val relativeTime = getTimeAgoShort(chat.lastMessageTime)
                val displayMessage = when {
                    unreadCount > 0 -> "$unreadCount new message${if (unreadCount > 1) "s" else ""}"
                    chat.lastMessage.isNotEmpty() -> chat.lastMessage
                    chat.lastMessageSenderId.isNotEmpty() -> {
                        if (chat.lastMessageSenderId == currentUserId) {
                            "Sent $relativeTime"
                        } else {
                            "Sent a photo"
                        }
                    }
                    else -> "Start a conversation"
                }

                Text(
                    text = displayMessage,
                    fontSize = 14.sp,
                    color = if (unreadCount > 0) Black else Grey,
                    fontWeight = if (unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatDialog(
    currentUserId: String,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    onDismiss: () -> Unit,
    onChatCreated: (String, UserModel) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var followingUsers by remember { mutableStateOf<List<UserModel>>(emptyList()) }
    var allUsers by remember { mutableStateOf<List<UserModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        userViewModel.getFollowingList(currentUserId) { users ->
            followingUsers = users
            isLoading = false
        }
        
        userViewModel.getAllUser { success, _, users ->
            if (success) {
                allUsers = users.filter { it.userId != currentUserId }
            }
        }
    }

    val filteredUsers = remember(followingUsers, allUsers, searchQuery) {
        if (searchQuery.isBlank()) {
            followingUsers
        } else {
            allUsers.filter {
                it.username.contains(searchQuery, ignoreCase = true) ||
                        it.fullName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "New Chat",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search users...", color = Grey) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Grey.copy(alpha = 0.5f),
                        focusedBorderColor = Black,
                        unfocusedContainerColor = White,
                        focusedContainerColor = White,
                        unfocusedTextColor = Black,
                        focusedTextColor = Black
                    ),
                    singleLine = true,
                    trailingIcon = {
                        Icon(painter = painterResource(R.drawable.baseline_search_24),
                            contentDescription = null,
                            tint = Black
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Black)
                        }
                    }
                    filteredUsers.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "You're not following anyone yet" else "No results",
                                color = Grey,
                                fontSize = 14.sp
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        ) {
                            items(filteredUsers) { user ->
                                UserListItem(
                                    user = user,
                                    onClick = {
                                        chatViewModel.getOrCreateChat(
                                            currentUserId,
                                            user.userId
                                        ) { success, _, chatId ->
                                            if (success && chatId != null) {
                                                onChatCreated(chatId, user)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel", color = Black)
                }
            }
        },
        containerColor = White
    )
}

@Composable
fun UserListItem(user: UserModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Light_grey),
            contentAlignment = Alignment.Center
        ) {
            if (user.profilePicture.isNotEmpty()) {
                AsyncImage(
                    model = user.profilePicture,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.baseline_person_24),
                    contentDescription = null,
                    tint = Grey,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = user.username,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Black
            )
            if (user.fullName.isNotEmpty()) {
                Text(
                    text = user.fullName,
                    fontSize = 14.sp,
                    color = Grey
                )
            }
        }
    }
}

@Composable
fun ChatActionDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                TextButton(
                    onClick = {
                        onDelete()
                        onDismiss()
                    },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_delete_24),
                            contentDescription = null,
                            tint = Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete", color = Red, fontSize = 17.sp)
                    }
                }
            }
        },
        containerColor = White,
        tonalElevation = 2.dp,
    )
}

@Composable
fun DeleteChatConfirmationDialog(
    userName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Chat?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete your conversation with $userName? This action cannot be undone.",
                fontSize = 16.sp,
                color = Black
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text("Delete", color = Red, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Grey)
            }
        },
        containerColor = White
    )
}
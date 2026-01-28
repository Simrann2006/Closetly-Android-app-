package com.example.closetly.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.R
import com.example.closetly.model.UserModel
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.utils.ThemeManager
import com.example.closetly.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BlockedUsersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            ClosetlyTheme(darkTheme = ThemeManager.isDarkMode) {
                BlockedUsersBody()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersBody() {
    val context = LocalContext.current
    val userRepo = remember { UserRepoImpl(context) }
    val userViewModel = remember { UserViewModel(userRepo) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    var blockedUsers by remember { mutableStateOf<List<UserModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showUnblockDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserModel?>(null) }
    val scope = rememberCoroutineScope()
    
    // Load blocked users in real-time
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            try {
                userRepo.getBlockedUsersListFlow(currentUserId).collectLatest { users ->
                    blockedUsers = users
                    isLoading = false
                }
            } catch (e: Exception) {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Blocked accounts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (ThemeManager.isDarkMode) Color.White else Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = if (ThemeManager.isDarkMode) Color.White else Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Add functionality if needed */ }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_add_24),
                            contentDescription = null,
                            tint = if (ThemeManager.isDarkMode) Color.White else Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                    titleContentColor = if (ThemeManager.isDarkMode) Color.White else Color.Black
                )
            )
        },
        containerColor = if (ThemeManager.isDarkMode) Background_Dark else White
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = if (ThemeManager.isDarkMode) Color.White else Color.Black)
            }
        } else if (blockedUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_block_24),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No blocked accounts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (ThemeManager.isDarkMode) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Blocked accounts will appear here",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (ThemeManager.isDarkMode) Background_Dark else Color.White)
                    .padding(padding),
                userScrollEnabled = true
            ) {
                items(
                    items = blockedUsers,
                    key = { it.userId }
                ) { user ->
                    BlockedUserItem(
                        user = user,
                        onUnblockClick = {
                            selectedUser = user
                            showUnblockDialog = true
                        }
                    )
                    HorizontalDivider(
                        color = if (ThemeManager.isDarkMode) Color.White.copy(alpha = 0.1f) else Color(0xFFE0E0E0),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 80.dp)
                    )
                }
                
                // Bottom section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "You may want to block",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Based on your Accounts Centre",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
    
    // Unblock confirmation dialog
    if (showUnblockDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showUnblockDialog = false },
            title = {
                Text(
                    "Unblock ${selectedUser?.username}?",
                    fontWeight = FontWeight.SemiBold,
                    color = if (ThemeManager.isDarkMode) Color.White else Color.Black
                )
            },
            text = {
                Text(
                    "They will be able to see your profile and posts again.",
                    color = if (ThemeManager.isDarkMode) Color.White else Color.Black
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            userViewModel.unblockUser(
                                currentUserId,
                                selectedUser!!.userId
                            ) { success, _ ->
                                if (success) {
                                    showUnblockDialog = false
                                    selectedUser = null
                                }
                            }
                        }
                    }
                ) {
                    Text("Unblock", color = Brown)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnblockDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = if (ThemeManager.isDarkMode) Surface_Dark else Color.White
        )
    }
}

@Composable
fun BlockedUserItem(
    user: UserModel,
    onUnblockClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (ThemeManager.isDarkMode) Background_Dark else Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(enabled = false) { /* Disable click */ },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile picture
        AsyncImage(
            model = user.profilePicture.ifEmpty { R.drawable.baseline_person_24 },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (ThemeManager.isDarkMode) Surface_Dark else Color(0xFFE0E0E0))
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // User info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user.username,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (ThemeManager.isDarkMode) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Includes other accounts they may have or create",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Unblock button
        Button(
            onClick = onUnblockClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (ThemeManager.isDarkMode) Surface_Dark else Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(
                text = "Unblock",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (ThemeManager.isDarkMode) Color.White else Color.Black
            )
        }
    }
}

package com.example.closetly

import ImageUtils
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.model.MessageModel
import com.example.closetly.repository.ChatRepoImpl
import com.example.closetly.repository.CommonRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : ComponentActivity() {
    lateinit var imageUtils: ImageUtils
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var showImagePreview by mutableStateOf(false)
    var selectedImages by mutableStateOf<List<Uri>>(emptyList())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val chatId = intent.getStringExtra("chatId") ?: ""
        val otherUserId = intent.getStringExtra("otherUserId") ?: ""
        val otherUserName = intent.getStringExtra("otherUserName") ?: ""
        val otherUserImage = intent.getStringExtra("otherUserImage") ?: ""
        
        imageUtils = ImageUtils(this, this)
        imageUtils.registerLaunchers { uri ->
            if (uri != null) {
                selectedImages = listOf(uri)
                showImagePreview = true
            }
        }
        
        setContent {
            ChatBody(
                chatId = chatId,
                otherUserId = otherUserId,
                otherUserName = otherUserName,
                otherUserImage = otherUserImage,
                selectedImages = selectedImages,
                showImagePreview = showImagePreview,
                onPickCamera = { imageUtils.launchCamera() },
                onPickGallery = { 
                    imageUtils.launchMultipleImagePicker { uris ->
                        selectedImages = uris.take(10)
                        showImagePreview = true
                    }
                },
                onPreviewDismiss = { showImagePreview = false; selectedImages = emptyList() },
                onImagesSent = { selectedImages = emptyList(); showImagePreview = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBody(
    chatId: String,
    otherUserId: String,
    otherUserName: String,
    otherUserImage: String,
    selectedImages: List<Uri>,
    showImagePreview: Boolean,
    onPickCamera: () -> Unit,
    onPickGallery: () -> Unit,
    onPreviewDismiss: () -> Unit,
    onImagesSent: () -> Unit
) {
    val context = LocalContext.current
    val chatViewModel = remember { ChatViewModel(ChatRepoImpl()) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<MessageModel>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()

    LaunchedEffect(chatId) {
        if (chatId.isNotEmpty()) {
            chatViewModel.getMessages(chatId) { success, _, messageList ->
                if (success) {
                    messages = messageList
                }
            }
            chatViewModel.markMessagesAsRead(chatId, currentUserId) { _, _ -> }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Light_grey),
                            contentAlignment = Alignment.Center
                        ) {
                            if (otherUserImage.isNotEmpty()) {
                                AsyncImage(
                                    model = otherUserImage,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_person_24),
                                    contentDescription = null,
                                    tint = Grey,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = otherUserName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Black
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = null,
                            tint = Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        },
        containerColor = White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        isCurrentUser = message.senderId == currentUserId
                    )
                }
            }

            if (isUploading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Black
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPickCamera,
                    enabled = !isUploading
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_camera_alt_24),
                        contentDescription = null,
                        tint = Black
                    )
                }

                IconButton(
                    onClick = onPickGallery,
                    enabled = !isUploading
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_insert_photo_24),
                        contentDescription = null,
                        tint = Black
                    )
                }

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message...", color = Grey) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Light_grey,
                        focusedBorderColor = Black,
                        unfocusedContainerColor = White,
                        focusedContainerColor = White,
                        unfocusedTextColor = Black,
                        focusedTextColor = Black
                    ),
                    maxLines = 4,
                    enabled = !isUploading
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (messageText.isNotBlank() && !isUploading) {
                            val message = MessageModel(
                                chatId = chatId,
                                senderId = currentUserId,
                                text = messageText.trim(),
                                timestamp = System.currentTimeMillis()
                            )
                            chatViewModel.sendMessage(chatId, message) { success, msg ->
                                if (success) {
                                    messageText = ""
                                } else {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    enabled = messageText.isNotBlank() && !isUploading
                ) {
                    Icon(painter = painterResource(R.drawable.baseline_send_24),
                        contentDescription = null,
                        tint = if (messageText.isNotBlank()) Black else Grey
                    )
                }
            }
        }
    }

    if (showImagePreview && selectedImages.isNotEmpty()) {
        ImagePreviewDialog(
            images = selectedImages,
            onDismiss = onPreviewDismiss,
            onSend = { imagesToSend ->
                isUploading = true
                val commonRepo = CommonRepoImpl()
                var uploadedCount = 0
                val totalImages = imagesToSend.size
                
                imagesToSend.forEach { imageUri ->
                    commonRepo.uploadImage(context, imageUri) { imageUrl ->
                        uploadedCount++
                        if (imageUrl != null) {
                            val message = MessageModel(
                                chatId = chatId,
                                senderId = currentUserId,
                                text = "",
                                imageUrl = imageUrl,
                                timestamp = System.currentTimeMillis()
                            )
                            chatViewModel.sendMessage(chatId, message) { _, _ -> }
                        }
                        
                        if (uploadedCount == totalImages) {
                            isUploading = false
                            onImagesSent()
                            if (uploadedCount != imagesToSend.size) {
                                android.widget.Toast.makeText(context, "Some images failed to upload", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ImagePreviewDialog(
    images: List<Uri>,
    onDismiss: () -> Unit,
    onSend: (List<Uri>) -> Unit
) {
    var selectedImageList by remember { mutableStateOf(images) }
    var currentIndex by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selected ${selectedImageList.size} photo${if (selectedImageList.size > 1) "s" else ""}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_close_24),
                            contentDescription = null,
                            tint = Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedImageList.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = selectedImageList[currentIndex],
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        IconButton(
                            onClick = {
                                selectedImageList = selectedImageList.filterIndexed { index, _ -> index != currentIndex }
                                if (currentIndex >= selectedImageList.size && currentIndex > 0) {
                                    currentIndex = selectedImageList.size - 1
                                }
                                if (selectedImageList.isEmpty()) {
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_delete_24),
                                contentDescription = null,
                                tint = White
                            )
                        }
                    }

                    if (selectedImageList.size > 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedImageList.forEachIndexed { index, uri ->
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (index == currentIndex) 2.dp else 0.dp,
                                            color = if (index == currentIndex) Black else androidx.compose.ui.graphics.Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { currentIndex = index }
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Grey, fontSize = 16.sp)
                    }

                    Button(
                        onClick = { onSend(selectedImageList) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Black
                        ),
                        enabled = selectedImageList.isNotEmpty()
                    ) {
                        Text(
                            "Send ${selectedImageList.size} photo${if (selectedImageList.size > 1) "s" else ""}",
                            color = White
                        )
                    }
                }
            }
        },
        containerColor = White
    )
}

@Composable
fun MessageBubble(message: MessageModel, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            if (message.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = message.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .heightIn(max = 300.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            if (message.text.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                                bottomEnd = if (isCurrentUser) 4.dp else 16.dp
                            )
                        )
                        .background(if (isCurrentUser) Black else Light_grey)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = message.text,
                        color = if (isCurrentUser) White else Black,
                        fontSize = 15.sp
                    )
                }
            }
            
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                fontSize = 11.sp,
                color = Grey,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}



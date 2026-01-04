package com.example.closetly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
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
import com.example.closetly.ui.theme.*

class MessageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MessageBody()
        }
    }
}

data class MessageItem(
    val username: String,
    val message: String,
    val timeAgo: String,
    val profileImage: Int? = null,
    val isNewMessage: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageBody() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val messages = remember {
        listOf(
            MessageItem(
                username = "Johnn.03",
                message = "Seen by johnn.03",
                timeAgo = "",
                isNewMessage = false
            ),
            MessageItem(
                username = "kendall",
                message = "Liked a message",
                timeAgo = "12h",
                isNewMessage = false
            ),
            MessageItem(
                username = "_anish5",
                message = "3 new message",
                timeAgo = "3d",
                isNewMessage = true
            )
        )
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
                            contentDescription = "Back",
                            tint = Black
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {}
                    ) {
                        Text(
                            text = "Emily",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Black
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Black
                        )
                    }

                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(R.drawable.edit),
                            contentDescription = "Edit",
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
                        Text("Search....", color = Grey.copy(alpha = 0.6f))
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
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
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(messages) { message ->
                    MessageListItem(message = message)
                }
            }
        }
    }
}

@Composable
fun MessageListItem(message: MessageItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Grey.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (message.profileImage != null) {
                AsyncImage(
                    model = message.profileImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                    contentDescription = null,
                    tint = Grey,
                    modifier = Modifier.size(24.dp)
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
                    text = message.username,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Black
                )

                if (message.timeAgo.isNotEmpty()) {
                    Text(
                        text = message.timeAgo,
                        fontSize = 12.sp,
                        color = Grey,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message.message,
                fontSize = 14.sp,
                color = if (message.isNewMessage) Black else Grey,
                fontWeight = if (message.isNewMessage) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
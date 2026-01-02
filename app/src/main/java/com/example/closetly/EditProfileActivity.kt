package com.example.closetly

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EditProfileScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen() {
    val context = LocalContext.current

    // Retrieve data from Intent
    var name by rememberSaveable { mutableStateOf((context as Activity).intent.getStringExtra("name") ?: "") }
    var username by rememberSaveable { mutableStateOf((context as Activity).intent.getStringExtra("username") ?: "") }
    var bio by rememberSaveable { mutableStateOf((context as Activity).intent.getStringExtra("bio") ?: "") }
    var imageUri by rememberSaveable { mutableStateOf<Any>((context as Activity).intent.getStringExtra("imageUri")?.let { Uri.parse(it) } ?: R.drawable.profile) }

    var usernameError by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imageUri = it }
    }

    Scaffold(
        topBar = {
            EditProfileTopBar(
                username = username,
                onBackClick = {
                    if (username.isBlank()) {
                        usernameError = true
                        Toast.makeText(context, "Username is required", Toast.LENGTH_SHORT).show()
                    } else {
                        val resultIntent = Intent().apply {
                            putExtra("name", name)
                            putExtra("username", username)
                            putExtra("bio", bio)
                            putExtra("imageUri", (imageUri as? Uri)?.toString())
                        }
                        (context as Activity).setResult(Activity.RESULT_OK, resultIntent)
                        (context as Activity).finish()
                    }
                },
                onSaveClick = {
                    if (username.isBlank()) {
                        usernameError = true
                        Toast.makeText(context, "Username is required", Toast.LENGTH_SHORT).show()
                    } else {
                        val resultIntent = Intent().apply {
                            putExtra("name", name)
                            putExtra("username", username)
                            putExtra("bio", bio)
                            putExtra("imageUri", (imageUri as? Uri)?.toString())
                        }
                        (context as Activity).setResult(Activity.RESULT_OK, resultIntent)
                        (context as Activity).finish()
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Background
            Image(
                painter = painterResource(id = R.drawable.registrationbg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Profile picture
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Card(
                        shape = CircleShape,
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { imagePickerLauncher.launch("image/*") }
                    ) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Change Profile Picture", color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Username
                TextField(
                    value = username,
                    onValueChange = {
                        username = it
                        usernameError = false
                    },
                    label = { Text("Username") },
                    isError = usernameError,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorLabelColor = Color.Red
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (usernameError) {
                    Text("Username is required", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bio
                TextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = false,
                    minLines = 3,
                    maxLines = 10
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileTopBar(
    username: String,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val context = LocalContext.current

    TopAppBar(
        title = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Edit Profile") } },
        navigationIcon = {
            IconButton(onClick = { onBackClick() }) {
                Image(painter = painterResource(R.drawable.back), contentDescription = "Back")
            }
        },
        actions = {
            TextButton(onClick = { onSaveClick() }) {
                Text("Save")
            }
        }
    )
}

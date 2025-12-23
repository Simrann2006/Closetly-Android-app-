package com.example.closetly

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EditProfile()
        }
    }
}

@Composable
fun EditProfile() {
    val notification = rememberSaveable { mutableStateOf("") }

    if (notification.value.isNotEmpty()) {
        Toast.makeText(
            LocalContext.current,
            notification.value,
            Toast.LENGTH_LONG
        ).show()
        notification.value = ""
    }

    var name by rememberSaveable { mutableStateOf("default name") }
    var username by rememberSaveable { mutableStateOf("default username") }
    var bio by rememberSaveable { mutableStateOf("default bio") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.registrationbg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Foreground UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            TopBar()
            ProfileImage()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Bio",
                    modifier = Modifier
                        .width(100.dp)
                        .padding(top = 8.dp)
                )

                TextField(
                    value = bio,
                    onValueChange = { bio = it },
                    singleLine = false,
                    modifier = Modifier.height(150.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
            }
        }
    }
}

@Composable
fun TopBar() {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                // Go back to ProfileActivity
                (context as? Activity)?.finish()
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Back"
            )
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Right Spacer
        Box(modifier = Modifier.size(48.dp))
    }
}

@Composable
fun ProfileImage() {
    val imageUri = rememberSaveable { mutableStateOf<Any>(R.drawable.baseline_person_outline_24) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { imageUri.value = it } }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = CircleShape,
            modifier = Modifier
                .padding(8.dp)
                .size(100.dp)
        ) {
            AsyncImage(
                model = imageUri.value,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { launcher.launch("image/*") }
            )
        }
        Text(text = "Change Profile Picture")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    EditProfile()
}

package com.example.closetly

import com.example.closetly.viewmodel.ProfileViewModel
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.ui.theme.Pink40
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel






@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateImage(it) }
    }

    // EditProfileActivity launcher
    val editProfileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                viewModel.updateProfile(
                    newName = data.getStringExtra("name") ?: viewModel.name,
                    newUsername = data.getStringExtra("username") ?: viewModel.username,
                    newBio = data.getStringExtra("bio") ?: viewModel.bio
                )
                viewModel.updateImage(
                    data.getStringExtra("imageUri")?.let { Uri.parse(it) }
                )
            }
        }
    }

    val selectedTab = remember { mutableStateOf("Posts") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // Profile Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") }
            ) {
                if (viewModel.imageUri != null) {
                    AsyncImage(
                        model = viewModel.imageUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(viewModel.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(viewModel.bio, fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat("0", "Posts")
                ProfileStat("0", "Followers")
                ProfileStat("0", "Following")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Edit Profile Button
        ProfileButton(
            text = "Edit Profile",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(40.dp),
            onClick = {
                editProfileLauncher.launch(
                    Intent(context, EditProfileActivity::class.java).apply {
                        putExtra("name", viewModel.name)
                        putExtra("username", viewModel.username)
                        putExtra("bio", viewModel.bio)
                        putExtra("imageUri", viewModel.imageUri?.toString())
                    }
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileTab(
                text = "Posts",
                selected = selectedTab.value == "Posts",
                onClick = { selectedTab.value = "Posts" }
            )

            ProfileTab(
                text = "Listings",
                selected = selectedTab.value == "Listings",
                onClick = { selectedTab.value = "Listings" }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Posts Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(5) {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(Color.LightGray)
                )
            }
        }
    }
}

@Composable
fun ProfileTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(40.dp)
                .background(if (selected) Pink40 else Color.Transparent)
        )
    }
}

@Composable
fun ProfileStat(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(number, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, fontSize = 12.sp)
    }
}

@Composable
fun ProfileButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Pink40)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

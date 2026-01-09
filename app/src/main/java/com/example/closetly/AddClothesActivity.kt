package com.example.closetly

import ImageUtils
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.ui.theme.*

class AddClothesActivity : ComponentActivity() {
    lateinit var imageUtils: ImageUtils
    var selectedImageUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageUtils = ImageUtils(this, this)
        imageUtils.registerLaunchers { uri ->
            selectedImageUri = uri
        }
        setContent {
            AddClothesBody(
                selectedImageUri = selectedImageUri,
                onPickCamera = { imageUtils.launchCamera() },
                onPickGallery = { imageUtils.launchImagePicker() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClothesBody(
    selectedImageUri: Uri?,
    onPickCamera: () -> Unit,
    onPickGallery: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(painterResource(R.drawable.baseline_close_24),
                            contentDescription = null)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val intent = Intent(context, ClothesDetailsActivity::class.java)
                            intent.putExtra("IMAGE_URI", selectedImageUri.toString())
                            context.startActivity(intent)
                            (context as? ComponentActivity)?.finish()
                        },
                        enabled = selectedImageUri != null
                    ) {
                        Text(
                            "Next",
                            color = if (selectedImageUri != null) Brown else Grey,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Black,
                    navigationIconContentColor = Black
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(560.dp)
                    .background(Light_grey),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        "No image selected",
                        fontSize = 16.sp,
                        color = Grey,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Divider(color = Grey.copy(alpha = 0.2f), thickness = 1.dp)

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(Light_grey, RoundedCornerShape(12.dp))
                            .clickable {
                                onPickCamera()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_camera_alt_24),
                                    contentDescription = null,
                                    tint = Brown,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                "Camera",
                                fontSize = 14.sp,
                                color = Grey,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(Light_grey, RoundedCornerShape(12.dp))
                            .clickable {
                                onPickGallery()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_insert_photo_24),
                                    contentDescription = null,
                                    tint = Brown,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                "Gallery",
                                fontSize = 14.sp,
                                color = Grey,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
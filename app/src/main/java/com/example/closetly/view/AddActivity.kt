package com.example.closetly.view

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
import com.example.closetly.R
import com.example.closetly.ui.theme.*
import kotlin.jvm.java

object AddFlow {
    const val CLOSET = "closet"
    const val POST = "post"
    const val LISTING = "listing"
}

class AddActivity : ComponentActivity() {
    lateinit var imageUtils: ImageUtils
    var selectedImageUri by mutableStateOf<Uri?>(null)
    private var flowType: String = AddFlow.CLOSET

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        flowType = intent.getStringExtra("FLOW_TYPE") ?: AddFlow.CLOSET

        imageUtils = ImageUtils(this, this)
        imageUtils.registerLaunchers { uri ->
            selectedImageUri = uri
        }
        setContent {
            AddBody(
                selectedImageUri = selectedImageUri,
                flowType = flowType,
                onPickCamera = { imageUtils.launchCamera() },
                onPickGallery = { imageUtils.launchImagePicker() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBody(
    selectedImageUri: Uri?,
    flowType: String,
    onPickCamera: () -> Unit,
    onPickGallery: () -> Unit
) {
    val context = LocalContext.current

    val title = when (flowType) {
        AddFlow.POST -> "Create Post"
        AddFlow.LISTING -> "Create Listing"
        else -> "Add Clothes"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            painterResource(R.drawable.baseline_close_24),
                            contentDescription = "Close"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val intent = when (flowType) {
                                AddFlow.POST -> {
                                    Intent(context, PostCreationActivity::class.java)
                                }
                                AddFlow.LISTING -> {
                                    Intent(context, ListingDetailsActivity::class.java)
                                }
                                else -> {
                                    Intent(context, ClothesDetailsActivity::class.java)
                                }
                            }
                            intent.putExtra("IMAGE_URI", selectedImageUri.toString())
                            intent.putExtra("FLOW_TYPE", flowType)
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_add_24),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Grey
                        )
                        Text(
                            "No image selected",
                            fontSize = 16.sp,
                            color = Grey,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(
                color = Grey.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ImageSourceOption(
                        icon = R.drawable.baseline_camera_alt_24,
                        label = "Camera",
                        onClick = onPickCamera
                    )
                }

                item {
                    ImageSourceOption(
                        icon = R.drawable.baseline_insert_photo_24,
                        label = "Gallery",
                        onClick = onPickGallery
                    )
                }
            }
        }
    }
}

@Composable
fun ImageSourceOption(
    icon: Int,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(Light_grey, RoundedCornerShape(12.dp))
            .clickable { onClick() },
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
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = Brown,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                label,
                fontSize = 14.sp,
                color = Grey,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
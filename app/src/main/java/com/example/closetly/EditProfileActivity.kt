package com.example.closetly

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.ui.theme.*

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
    val userRepo = remember { com.example.closetly.repository.UserRepoImpl(context) }
    val userViewModel = remember { com.example.closetly.viewmodel.UserViewModel(userRepo) }
    val commonRepo = remember { com.example.closetly.repository.CommonRepoImpl() }
    val commonViewModel = remember { com.example.closetly.viewmodel.CommonViewModel(commonRepo) }
    val currentUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }

    var name by rememberSaveable { mutableStateOf((context as Activity).intent.getStringExtra("name") ?: "") }
    var username by rememberSaveable { mutableStateOf((context as Activity).intent.getStringExtra("username") ?: "") }
    var bio by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<Any>((context as Activity).intent.getStringExtra("imageUri")?.let { Uri.parse(it) } ?: R.drawable.profile) }
    var cloudinaryImageUrl by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf(false) }
    var usernameErrorMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var shouldSaveAfterUpload by remember { mutableStateOf(false) }

    LaunchedEffect(isUploadingImage, shouldSaveAfterUpload) {
        if (!isUploadingImage && shouldSaveAfterUpload && isSaving) {
            shouldSaveAfterUpload = false
            currentUser?.let { user ->
                userRepo.checkUsernameExists(username.lowercase(), user.uid) { exists ->
                    if (exists) {
                        isSaving = false
                        usernameError = true
                        usernameErrorMessage = "Username already taken"
                        Toast.makeText(context, "Username already taken", Toast.LENGTH_SHORT).show()
                    } else {
                        val updatedUser = com.example.closetly.model.UserModel(
                            userId = user.uid,
                            fullName = name,
                            email = user.email ?: "",
                            phoneNumber = phoneNumber,
                            selectedCountry = location,
                            profilePicture = cloudinaryImageUrl,
                            username = username.lowercase(),
                            bio = bio
                        )

                        userViewModel.editProfile(user.uid, updatedUser) { success, message ->
                            isSaving = false
                            if (success) {
                                (context as Activity).setResult(Activity.RESULT_OK)
                                (context as Activity).finish()
                            } else {
                                Toast.makeText(context, "Failed: $message", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            userViewModel.getUserById(user.uid) { success, message, userData ->
                if (success && userData != null) {
                    if (userData.profilePicture.isNotEmpty()) {
                        cloudinaryImageUrl = userData.profilePicture
                    }
                    if (userData.bio.isNotEmpty()) {
                        bio = userData.bio
                    }
                    if (userData.phoneNumber.isNotEmpty()) {
                        phoneNumber = userData.phoneNumber
                    }
                    if (userData.selectedCountry.isNotEmpty()) {
                        location = userData.selectedCountry
                    }
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            isUploadingImage = true
            commonViewModel.uploadImage(context, it) { uploadedUrl ->
                isUploadingImage = false
                uploadedUrl?.let { url ->
                    cloudinaryImageUrl = url
                } ?: run {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            EditProfileTopBar(
                isLoading = isSaving || isUploadingImage,
                onBackClick = {
                    (context as Activity).finish()
                },
                onSaveClick = {
                    if (username.isBlank()) {
                        usernameError = true
                        usernameErrorMessage = "Username is required"
                        Toast.makeText(context, "Username is required", Toast.LENGTH_SHORT).show()
                    } else if (!isSaving) {
                        isSaving = true

                        if (isUploadingImage) {
                            shouldSaveAfterUpload = true
                            return@EditProfileTopBar
                        }

                        currentUser?.let { user ->
                            userRepo.checkUsernameExists(username.lowercase(), user.uid) { exists ->
                                if (exists) {
                                    isSaving = false
                                    usernameError = true
                                    usernameErrorMessage = "Username already taken"
                                    Toast.makeText(context, "Username already taken", Toast.LENGTH_SHORT).show()
                                } else {
                                    val updatedUser = com.example.closetly.model.UserModel(
                                        userId = user.uid,
                                        fullName = name,
                                        email = user.email ?: "",
                                        phoneNumber = phoneNumber,
                                        selectedCountry = location,
                                        profilePicture = cloudinaryImageUrl,
                                        username = username.lowercase(),
                                        bio = bio
                                    )

                                    userViewModel.editProfile(user.uid, updatedUser) { success, message ->
                                        isSaving = false
                                        if (success) {
                                            (context as Activity).setResult(Activity.RESULT_OK)
                                            (context as Activity).finish()
                                        } else {
                                            Toast.makeText(context, "Failed: $message", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Light_grey)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            Brown,
                            shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(120.dp)
                    ) {
                        Card(
                            shape = CircleShape,
                            modifier = Modifier
                                .size(120.dp)
                                .border(4.dp, White, CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Light_grey)
                            ) {
                                if (cloudinaryImageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = cloudinaryImageUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Grey,
                                            modifier = Modifier.size(60.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            shape = CircleShape,
                            color = Brown,
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    if (isUploadingImage) {
                        Spacer(modifier = Modifier.height(4.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Brown
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSectionCard(title = "Personal Information") {
                ProfileTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    icon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        usernameError = false
                    },
                    label = "Username",
                    icon = Icons.Default.AccountCircle,
                    isError = usernameError,
                    errorMessage = usernameErrorMessage
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileTextField(
                    value = currentUser?.email ?: "",
                    onValueChange = { },
                    label = "Email",
                    icon = Icons.Default.Email,
                    enabled = false
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSectionCard(title = "Contact & Location") {
                ProfileTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = "Phone Number",
                    icon = Icons.Default.Phone
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Location",
                    icon = Icons.Default.LocationOn
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSectionCard(title = "About") {
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio", color = Grey, fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = false,
                    minLines = 4,
                    maxLines = 8
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileTopBar(
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Edit Profile",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = { onBackClick() }, enabled = !isLoading) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIos,
                    contentDescription = null,
                    tint = Black
                )
            }
        },
        actions = {
            TextButton(
                onClick = { onSaveClick() },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Brown
                    )
                } else {
                    Text(
                        "Save",
                        color = Brown,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = White
        )
    )
}

@Composable
fun ProfileSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGrey,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isError) Red else Brown
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (enabled) White else Light_grey1.copy(alpha = 0.3f),
                unfocusedContainerColor = if (enabled) White else Light_grey1.copy(alpha = 0.3f),
                disabledContainerColor = White,
                focusedTextColor = Black,
                unfocusedTextColor = Black,
                disabledTextColor = Grey,
                focusedBorderColor = Brown,
                unfocusedBorderColor = Grey.copy(alpha = 0.3f),
                disabledBorderColor = Grey.copy(alpha = 0.2f),
                errorBorderColor = Red,
                focusedLabelColor = Brown,
                unfocusedLabelColor = Grey
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

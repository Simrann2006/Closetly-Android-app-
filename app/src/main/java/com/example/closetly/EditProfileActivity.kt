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
    val userRepo = remember { com.example.closetly.repository.UserRepoImpl() }
    val userViewModel = remember { com.example.closetly.viewmodel.UserViewModel(userRepo) }
    val commonRepo = remember { com.example.closetly.repository.CommonRepoImpl() }
    val commonViewModel = remember { com.example.closetly.viewmodel.CommonViewModel(commonRepo) }
    val currentUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }

    var name by rememberSaveable { mutableStateOf((context as Activity).intent.getStringExtra("name") ?: "") }
    var username by rememberSaveable { mutableStateOf((context as Activity).intent.getStringExtra("username") ?: "") }
    var bio by remember { mutableStateOf("") }
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
                            phoneNumber = "",
                            selectedCountry = "",
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
                                        phoneNumber = "",
                                        selectedCountry = "",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                    Text(usernameErrorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(8.dp))

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
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    TopAppBar(
        title = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Edit Profile") } },
        navigationIcon = {
            IconButton(onClick = { onBackClick() }, enabled = !isLoading) {
                Image(painter = painterResource(R.drawable.back), contentDescription = "Back")
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
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text("Save")
                }
            }
        }
    )
}

package com.example.closetly

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.repository.ProductRepoImpl
import com.example.closetly.repository.PostRepoImpl
import com.example.closetly.viewmodel.ProductViewModel
import com.example.closetly.viewmodel.PostViewModel
import com.example.closetly.model.ProductModel
import com.example.closetly.model.PostModel
import com.example.closetly.model.ListingType
import com.example.closetly.ui.theme.Pink40
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.White
import com.example.closetly.ui.theme.Black
import com.example.closetly.ui.theme.Grey
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val userRepo = remember { UserRepoImpl() }
    val userViewModel = remember { com.example.closetly.viewmodel.UserViewModel(userRepo) }
    val productRepo = remember { ProductRepoImpl() }
    val productViewModel = remember { ProductViewModel(productRepo) }
    val postRepo = remember { PostRepoImpl() }
    val postViewModel = remember { PostViewModel(postRepo) }
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profilePicture by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var userListings by remember { mutableStateOf<List<ProductModel>>(emptyList()) }
    var userPosts by remember { mutableStateOf<List<PostModel>>(emptyList()) }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.let { user ->
            userViewModel.getUserById(user.uid) { success, _, userData ->
                if (success && userData != null) {
                    name = userData.fullName
                    username = userData.username
                    bio = userData.bio
                    profilePicture = userData.profilePicture
                }
                isLoading = false
            }
            productViewModel.getUserProducts(user.uid) { listings ->
                userListings = listings
            }
            postViewModel.getUserPosts(user.uid) { posts ->
                userPosts = posts
            }
        } ?: run {
            isLoading = false
        }
    }

    val editProfileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentUser?.let { user ->
                userViewModel.getUserById(user.uid) { success, _, userData ->
                    if (success && userData != null) {
                        name = userData.fullName
                        username = userData.username
                        bio = userData.bio
                        profilePicture = userData.profilePicture
                    }
                }
                productViewModel.getUserProducts(user.uid) { listings ->
                    userListings = listings
                }
                postViewModel.getUserPosts(user.uid) { posts ->
                    userPosts = posts
                }
            }
        }
    }

    val selectedTab = remember { mutableStateOf("Posts") }
    
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Pink40)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
            ) {
                if (profilePicture.isNotEmpty()) {
                    AsyncImage(
                        model = profilePicture,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0))
                    )
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat("${userPosts.size}", "Posts")
                ProfileStat("0", "Followers")
                ProfileStat("0", "Following")
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
            
            Text(
                text = username,
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            if (bio.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bio,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ProfileButton(
            text = "Edit Profile",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(40.dp),
            onClick = {
                editProfileLauncher.launch(
                    Intent(context, EditProfileActivity::class.java).apply {
                        putExtra("name", name)
                        putExtra("username", username)
                        putExtra("bio", bio)
                        putExtra("imageUri", profilePicture)
                    }
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

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

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab.value) {
            "Posts" -> {
                if (userPosts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No posts yet",
                            fontSize = 16.sp,
                            color = Grey
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 1.dp),
                        contentPadding = PaddingValues(1.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(userPosts.size) { index ->
                            val post = userPosts[index]
                            ProfilePostCard(
                                post = post,
                                onRefresh = {
                                    currentUser?.let { user ->
                                        postViewModel.getUserPosts(user.uid) { posts ->
                                            userPosts = posts
                                        }
                                    }
                                },
                                onClick = {
                                    val intent = Intent(context, PostFeedActivity::class.java)
                                    intent.putExtra("USER_ID", currentUser?.uid)
                                    intent.putExtra("INITIAL_INDEX", index)
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
            "Listings" -> {
                if (userListings.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No listings yet",
                            fontSize = 16.sp,
                            color = Grey
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 1.dp),
                        contentPadding = PaddingValues(1.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(userListings.size) { index ->
                            val listing = userListings[index]
                            ProfileListingCard(
                                product = listing,
                                onRefresh = {
                                    currentUser?.let { user ->
                                        productViewModel.getUserProducts(user.uid) { listings ->
                                            userListings = listings
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
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

@Composable
fun ProfileListingCard(
    product: ProductModel,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable {
                val intent = Intent(context, ListingViewerActivity::class.java).apply {
                    putExtra("productId", product.id)
                }
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun EditListingDialog(
    product: ProductModel,
    productViewModel: ProductViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    var title by remember { mutableStateOf(product.title) }
    var description by remember { mutableStateOf(product.description) }
    var price by remember { mutableStateOf(product.price.toString()) }
    var rentPrice by remember { mutableStateOf(product.rentPricePerDay?.toString() ?: "") }
    var brand by remember { mutableStateOf(product.brand) }
    var size by remember { mutableStateOf(product.size) }
    var condition by remember { mutableStateOf(product.condition) }
    var status by remember { mutableStateOf(product.status) }
    
    var expandedCondition by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    
    val conditions = listOf("New", "Like New", "Good", "Fair", "Poor")
    val statusOptions = if (product.listingType == ListingType.THRIFT) {
        listOf("Available", "Sold Out")
    } else {
        listOf("Available", "On Rent")
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "Edit Listing",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey
                    ),
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey
                    )
                )
                
                if (product.listingType == ListingType.RENT) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = rentPrice,
                        onValueChange = { rentPrice = it },
                        label = { Text("Rent Price per Day") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brown,
                            unfocusedBorderColor = Grey
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Size") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = condition,
                        onValueChange = { },
                        label = { Text("Condition") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Grey,
                            disabledTextColor = Black
                        ),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_arrow_drop_down_24),
                                contentDescription = null
                            )
                        }
                    )
                    
                    DropdownMenu(
                        expanded = expandedCondition,
                        onDismissRequest = { expandedCondition = false },
                        modifier = Modifier.background(White)
                    ) {
                        conditions.forEach { cond ->
                            DropdownMenuItem(
                                text = { Text(cond) },
                                onClick = {
                                    condition = cond
                                    expandedCondition = false
                                }
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expandedCondition = true }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = { },
                        label = { Text(if (product.listingType == ListingType.THRIFT) "Availability" else "Rental Status") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = when (status) {
                                "Sold Out" -> Color.Red
                                "On Rent" -> Color(0xFFFFA500)
                                else -> Grey
                            },
                            disabledTextColor = when (status) {
                                "Sold Out" -> Color.Red
                                "On Rent" -> Color(0xFFFFA500)
                                else -> Black
                            }
                        ),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_arrow_drop_down_24),
                                contentDescription = null,
                                tint = when (status) {
                                    "Sold Out" -> Color.Red
                                    "On Rent" -> Color(0xFFFFA500)
                                    else -> Grey
                                }
                            )
                        }
                    )
                    
                    DropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false },
                        modifier = Modifier.background(White)
                    ) {
                        statusOptions.forEach { stat ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        stat,
                                        color = when (stat) {
                                            "Sold Out" -> Color.Red
                                            "On Rent" -> Color(0xFFFFA500)
                                            else -> Black
                                        },
                                        fontWeight = if (stat != "Available") FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    status = stat
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expandedStatus = true }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Grey
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val updatedProduct = product.copy(
                                title = title,
                                description = description,
                                price = price.toDoubleOrNull() ?: product.price,
                                rentPricePerDay = rentPrice.toDoubleOrNull(),
                                brand = brand,
                                size = size,
                                condition = condition,
                                status = status
                            )
                            productViewModel.editProduct(updatedProduct) { success, _ ->
                                if (success) {
                                    onSaved()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brown
                        )
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun ListingDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Grey
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Black
        )
    }
}

@Composable
fun ListingDeleteConfirmationDialog(
    listingTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Listing",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("Are you sure you want to delete \"$listingTitle\"? This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = White
    )
}

@Composable
fun ProfilePostCard(
    post: PostModel,
    onRefresh: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        AsyncImage(
            model = post.imageUrl,
            contentDescription = post.caption,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
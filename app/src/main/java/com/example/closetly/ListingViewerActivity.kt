package com.example.closetly

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.closetly.model.ListingType
import com.example.closetly.model.ProductModel
import com.example.closetly.repository.ProductRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.utils.getTimeAgo
import com.example.closetly.viewmodel.ProductViewModel
import com.google.firebase.auth.FirebaseAuth

class ListingViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val productId = intent.getStringExtra("productId") ?: ""

        setContent {
            ListingViewerBody(productId = productId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingViewerBody(productId: String) {
    val context = LocalContext.current
    val productRepo = remember { ProductRepoImpl() }
    val productViewModel = remember { ProductViewModel(productRepo) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var product by remember { mutableStateOf<ProductModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isOwner by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            productViewModel.getProductById(productId) { success, _, fetchedProduct ->
                if (success && fetchedProduct != null) {
                    product = fetchedProduct
                    isOwner = fetchedProduct.sellerId == currentUserId
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Listing",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = "Back",
                            tint = Black
                        )
                    }
                },
                actions = {
                    if (isOwner && product != null) {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_more_vert_24),
                                contentDescription = "More options",
                                tint = Black
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit", color = Black) },
                                onClick = {
                                    showMenu = false
                                    showEditDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_edit_24),
                                        contentDescription = null,
                                        tint = Black
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = Black) },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_delete_24),
                                        contentDescription = null,
                                        tint = Black
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Black
                )
            )
        },
        containerColor = White
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Brown)
            }
        } else if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Listing not found",
                        fontSize = 16.sp,
                        color = Grey
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Text("Go Back")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    AsyncImage(
                        model = product!!.imageUrl,
                        contentDescription = product!!.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (product!!.status != "Available") {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            color = when (product!!.status) {
                                "Sold Out" -> Color.Red
                                "On Rent" -> Color(0xFFFFA500)
                                else -> Grey
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = product!!.status.uppercase(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product!!.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = if (product!!.listingType == ListingType.RENT && product!!.rentPricePerDay != null) {
                                "$${product!!.rentPricePerDay}/day"
                            } else {
                                "$${product!!.price}"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Brown
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = product!!.listingType.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Grey
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (product!!.description.isNotEmpty()) {
                        Text(
                            text = product!!.description,
                            fontSize = 14.sp,
                            color = Black,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    HorizontalDivider(color = Grey.copy(alpha = 0.3f))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ListingDetailRowViewer(label = "Condition", value = product!!.condition)
                    if (product!!.brand.isNotEmpty()) {
                        ListingDetailRowViewer(label = "Brand", value = product!!.brand)
                    }
                    if (product!!.size.isNotEmpty()) {
                        ListingDetailRowViewer(label = "Size", value = product!!.size)
                    }
                    ListingDetailRowViewer(label = "Status", value = product!!.status)
                    ListingDetailRowViewer(label = "Posted", value = getTimeAgo(product!!.timestamp))
                }
            }
        }

        if (showEditDialog && product != null) {
            EditListingDialogViewer(
                product = product!!,
                productViewModel = productViewModel,
                onDismiss = { showEditDialog = false },
                onSaved = {
                    showEditDialog = false
                    // Refresh product data
                    productViewModel.getProductById(productId) { success, _, fetchedProduct ->
                        if (success && fetchedProduct != null) {
                            product = fetchedProduct
                        }
                    }
                    Toast.makeText(context, "Listing updated", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showDeleteDialog && product != null) {
            ListingDeleteConfirmationDialogViewer(
                listingTitle = product!!.title,
                onConfirm = {
                    productViewModel.deleteProduct(product!!.id) { success, message ->
                        if (success) {
                            Toast.makeText(context, "Listing deleted", Toast.LENGTH_SHORT).show()
                            (context as? ComponentActivity)?.finish()
                        } else {
                            Toast.makeText(context, "Failed: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
fun ListingDetailRowViewer(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Grey
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Black,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListingDialogViewer(
    product: ProductModel,
    productViewModel: ProductViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
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
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Edit Listing",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brown,
                            focusedLabelColor = Brown
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brown,
                            focusedLabelColor = Brown
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brown,
                            focusedLabelColor = Brown
                        )
                    )

                    if (product.listingType == ListingType.RENT) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = rentPrice,
                            onValueChange = { rentPrice = it },
                            label = { Text("Rent Price per Day") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brown,
                                focusedLabelColor = Brown
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brown,
                            focusedLabelColor = Brown
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text("Size") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brown,
                            focusedLabelColor = Brown
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedCondition,
                        onExpandedChange = { expandedCondition = it }
                    ) {
                        OutlinedTextField(
                            value = condition,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Condition") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCondition) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brown,
                                focusedLabelColor = Brown
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCondition,
                            onDismissRequest = { expandedCondition = false },
                            modifier = Modifier.background(White)
                        ) {
                            conditions.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = Black) },
                                    onClick = {
                                        condition = item
                                        expandedCondition = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = { expandedStatus = it }
                    ) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brown,
                                focusedLabelColor = Brown
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false },
                            modifier = Modifier.background(White)
                        ) {
                            statusOptions.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = Black) },
                                    onClick = {
                                        status = item
                                        expandedStatus = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val updatedProduct = product.copy(
                            title = title,
                            description = description,
                            price = price.toDoubleOrNull() ?: product.price,
                            rentPricePerDay = if (product.listingType == ListingType.RENT)
                                rentPrice.toDoubleOrNull() else product.rentPricePerDay,
                            brand = brand,
                            size = size,
                            condition = condition,
                            status = status
                        )
                        productViewModel.editProduct(updatedProduct) { success, message ->
                            if (success) {
                                onSaved()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed: $message",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Brown),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ListingDeleteConfirmationDialogViewer(
    listingTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Delete Listing",
                fontWeight = FontWeight.Bold,
                color = Black
            )
        },
        text = {
            Text(
                "Are you sure you want to delete \"$listingTitle\"? This action cannot be undone.",
                color = Black
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Grey)
            ) {
                Text("Cancel")
            }
        },
        containerColor = White
    )
}


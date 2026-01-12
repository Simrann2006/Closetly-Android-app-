package com.example.closetly

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.model.ListingType
import com.example.closetly.model.ProductModel
import com.example.closetly.repository.CommonRepoImpl
import com.example.closetly.repository.ProductRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.viewmodel.CommonViewModel
import com.example.closetly.viewmodel.ProductViewModel

class ListingDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageUriString = intent.getStringExtra("IMAGE_URI")
        val imageUri = imageUriString?.let { Uri.parse(it) }

        setContent {
            ListingDetailsBody(imageUri = imageUri)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailsBody(imageUri: Uri?) {
    val context = LocalContext.current
    val activity = context as? Activity

    val productRepo = remember { ProductRepoImpl() }
    val productViewModel = remember { ProductViewModel(productRepo) }
    val commonRepo = remember { CommonRepoImpl() }
    val commonViewModel = remember { CommonViewModel(commonRepo) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var rentPrice by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var listingType by remember { mutableStateOf(ListingType.THRIFT) }

    var showConditionDropdown by remember { mutableStateOf(false) }
    var showListingTypeDropdown by remember { mutableStateOf(false) }

    val conditions = listOf("New", "Like New", "Good", "Fair", "Poor")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Listing Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (imageUri != null) {
                                commonViewModel.uploadImage(context, imageUri) { imageUrl ->
                                    if (imageUrl != null) {
                                        val model = ProductModel(
                                            title = title,
                                            description = description,
                                            price = price.toDoubleOrNull() ?: 0.0,
                                            rentPricePerDay = if (listingType == ListingType.RENT)
                                                rentPrice.toDoubleOrNull() else null,
                                            imageUrl = imageUrl,
                                            size = size,
                                            brand = brand,
                                            condition = condition,
                                            listingType = listingType
                                        )
                                        productViewModel.addProduct(model) { success, message ->
                                            Toast.makeText(context, message, Toast.LENGTH_LONG)
                                                .show()
                                            if (success) activity?.finish()
                                        }
                                    } else {
                                        Log.e(
                                            "Upload Error",
                                            "Failed to upload image to Cloudinary"
                                        )
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please select an image first",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Text(
                            "Add",
                            color = if (title.isNotEmpty() && price.isNotEmpty() && listingType != null && condition.isNotEmpty()) Brown else Grey,
                            fontWeight = FontWeight.SemiBold,
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
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Divider(color = Light_grey)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box {
                    OutlinedTextField(
                        value = listingType.name,
                        onValueChange = { },
                        label = { Text("Listing Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showListingTypeDropdown = true },
                        readOnly = true,
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Grey,
                            disabledTextColor = Black,
                            disabledContainerColor = White
                        ),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_arrow_drop_down_24),
                                contentDescription = null,
                                tint = Grey
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = showListingTypeDropdown,
                        onDismissRequest = { showListingTypeDropdown = false },
                        modifier = Modifier.background(White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("THRIFT", color = Black) },
                            onClick = {
                                listingType = ListingType.THRIFT
                                showListingTypeDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("RENT", color = Black) },
                            onClick = {
                                listingType = ListingType.RENT
                                showListingTypeDropdown = false
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showListingTypeDropdown = true }
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey
                    ),
                    maxLines = 5
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text(if (listingType == ListingType.RENT) "Sale Price*" else "Price*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey
                    )
                )

                if (listingType == ListingType.RENT) {
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

                Box {
                    OutlinedTextField(
                        value = condition,
                        onValueChange = { },
                        label = { Text("Condition*") },
                        placeholder = { Text("Select condition") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showConditionDropdown = true },
                        readOnly = true,
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Grey,
                            disabledTextColor = Black,
                            disabledContainerColor = White
                        ),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_arrow_drop_down_24),
                                contentDescription = null,
                                tint = Grey
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = showConditionDropdown,
                        onDismissRequest = { showConditionDropdown = false },
                        modifier = Modifier.background(White)
                    ) {
                        conditions.forEach { cond ->
                            DropdownMenuItem(
                                text = { Text(cond, color = Black) },
                                onClick = {
                                    condition = cond
                                    showConditionDropdown = false
                                }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showConditionDropdown = true }
                    )
                }

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

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
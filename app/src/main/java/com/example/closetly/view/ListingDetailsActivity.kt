package com.example.closetly.view

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.R
import com.example.closetly.model.ListingType
import com.example.closetly.model.ProductModel
import com.example.closetly.repository.CommonRepoImpl
import com.example.closetly.repository.ProductRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.utils.ThemeManager
import com.example.closetly.viewmodel.CommonViewModel
import com.example.closetly.viewmodel.ProductViewModel

class ListingDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.initialize(this)

        val imageUriString = intent.getStringExtra("IMAGE_URI")
        val imageUri = imageUriString?.let { Uri.parse(it) }

        setContent {
            ClosetlyTheme(darkTheme = ThemeManager.isDarkMode) {
                ListingDetailsBody(imageUri = imageUri)
            }
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
                    containerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                    titleContentColor = if (ThemeManager.isDarkMode) OnSurface_Dark else Black,
                    navigationIconContentColor = if (ThemeManager.isDarkMode) OnSurface_Dark else Black
                )
            )
        },
        containerColor = if (ThemeManager.isDarkMode) Background_Dark else Background_Light
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
                Column {
                    Text(
                        "Listing Type",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    Box {
                        OutlinedTextField(
                            value = listingType.name,
                            onValueChange = { },
                            placeholder = { Text("Select listing type", style = TextStyle(fontFamily = FontFamily(Font(R.font.poppins_regular)), fontSize = 14.sp)) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = if (listingType.name.isNotEmpty()) Brown else if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1,
                                disabledTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else Brown,
                                disabledContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White
                            ),
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_arrow_drop_down_24),
                                    contentDescription = null,
                                    tint = Grey
                                )
                            },
                            textStyle = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 14.sp
                            )
                        )

                    DropdownMenu(
                        expanded = showListingTypeDropdown,
                        onDismissRequest = { showListingTypeDropdown = false },
                        modifier = Modifier.background(if (ThemeManager.isDarkMode) Surface_Dark else White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("THRIFT", color = if (ThemeManager.isDarkMode) OnSurface_Dark else Black) },
                            onClick = {
                                listingType = ListingType.THRIFT
                                showListingTypeDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("RENT", color = if (ThemeManager.isDarkMode) OnSurface_Dark else Black) },
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
                }

                Column {
                    Text(
                        "Title *",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Enter title", style = TextStyle(fontFamily = FontFamily(Font(R.font.poppins_regular)), fontSize = 14.sp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brown,
                            unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            unfocusedBorderColor = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1,
                            focusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
                            unfocusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp
                        )
                    )
                }

                Column {
                    Text(
                        "Description",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Enter description", style = TextStyle(fontFamily = FontFamily(Font(R.font.poppins_regular)), fontSize = 14.sp)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brown,
                            unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            unfocusedBorderColor = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1,
                            focusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
                            unfocusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp
                        )
                    )
                }

                Column {
                    Text(
                        if (listingType == ListingType.RENT) "Sale Price *" else "Price *",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        placeholder = { Text("Enter price", style = TextStyle(fontFamily = FontFamily(Font(R.font.poppins_regular)), fontSize = 14.sp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brown,
                            unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            unfocusedBorderColor = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1,
                            focusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
                            unfocusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp
                        )
                    )
                }

                if (listingType == ListingType.RENT) {
                    Column {
                        Text(
                            "Rent Price per Day",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                            ),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = rentPrice,
                            onValueChange = { rentPrice = it },
                            placeholder = { Text("Enter rent price", style = TextStyle(fontFamily = FontFamily(Font(R.font.poppins_regular)), fontSize = 14.sp)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brown,
                                unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                                focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                                unfocusedBorderColor = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1,
                                focusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
                                unfocusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
                            ),
                            textStyle = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                Column {
                    Text(
                        "Condition *",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    Box {
                        OutlinedTextField(
                            value = condition,
                            onValueChange = { },
                            placeholder = { Text("Select condition", style = TextStyle(fontFamily = FontFamily(Font(R.font.poppins_regular)), fontSize = 14.sp)) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = if (condition.isNotEmpty()) Brown else if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1,
                                disabledTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else Brown,
                                disabledContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White
                            ),
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_arrow_drop_down_24),
                                    contentDescription = null,
                                    tint = Grey
                                )
                            },
                            textStyle = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 14.sp
                            )
                        )

                    DropdownMenu(
                        expanded = showConditionDropdown,
                        onDismissRequest = { showConditionDropdown = false },
                        modifier = Modifier.background(if (ThemeManager.isDarkMode) Surface_Dark else White)
                    ) {
                        conditions.forEach { cond ->
                            DropdownMenuItem(
                                text = { Text(cond, color = if (ThemeManager.isDarkMode) OnSurface_Dark else Black) },
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
                }

                Column {
                    Text(
                        "Brand",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        placeholder = { Text("Enter brand", style = TextStyle(fontFamily = FontFamily(Font(R.font.poppins_regular)), fontSize = 14.sp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brown,
                            unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            unfocusedBorderColor = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1,
                            focusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
                            unfocusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp
                        )
                    )
                }

                Column {
                    Text(
                        "Size",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        placeholder = { Text("Enter size", style = TextStyle(fontFamily = FontFamily(Font(R.font.poppins_regular)), fontSize = 14.sp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brown,
                            unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                            unfocusedBorderColor = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey1,
                            focusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
                            unfocusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}
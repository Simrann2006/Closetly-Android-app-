package com.example.closetly.view

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.example.closetly.model.CategoryModel
import com.example.closetly.model.ClothesModel
import com.example.closetly.repository.CategoryRepoImpl
import com.example.closetly.repository.ClothesRepoImpl
import com.example.closetly.repository.CommonRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.utils.ThemeManager
import com.example.closetly.viewmodel.CategoryViewModel
import com.example.closetly.viewmodel.ClothesViewModel
import com.example.closetly.viewmodel.CommonViewModel

class ClothesDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.initialize(this)

        val imageUriString = intent.getStringExtra("IMAGE_URI")
        val imageUri = imageUriString?.let { Uri.parse(it) }

        setContent {
            ClosetlyTheme(darkTheme = ThemeManager.isDarkMode) {
                ClothesDetailsBody(imageUri = imageUri)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothesDetailsBody(
    imageUri: Uri?
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val clothesRepo = remember { ClothesRepoImpl() }
    val clothesViewModel = remember { ClothesViewModel(clothesRepo) }
    val commonRepo = remember { CommonRepoImpl() }
    val commonViewModel = remember { CommonViewModel(commonRepo) }
    val categoryRepo = remember { CategoryRepoImpl() }
    val categoryViewModel = remember { CategoryViewModel(categoryRepo) }

    var clothesName by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var season by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var occasion by remember { mutableStateOf("") }

    var isUploading by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    // Predefined options
    val colorOptions = listOf(
        "Red",
        "Blue",
        "Black",
        "White",
        "Green",
        "Yellow",
        "Pink",
        "Brown",
        "Gray",
        "Beige",
        "Navy"
    )
    val seasonOptions = listOf("Spring", "Summer", "Fall", "Winter", "All Season")
    val occasionOptions = listOf("Casual", "Formal", "Party", "Work", "Sport", "Beach", "Date")

    var selectedColors by remember { mutableStateOf(setOf<String>()) }
    var selectedSeasons by remember { mutableStateOf(setOf<String>()) }
    var selectedOccasions by remember { mutableStateOf(setOf<String>()) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    var categories by remember { mutableStateOf<List<CategoryModel>>(emptyList()) }

    LaunchedEffect(Unit) {
        categoryViewModel.getAllCategories { success, _, data ->
            if (success) {
                if (data == null || data.isEmpty()) {
                    val defaultCategories = listOf("Tops", "Bottoms", "Shoes")

                    var addedCount = 0
                    defaultCategories.forEach { categoryName ->
                        val newCategory = CategoryModel(categoryName = categoryName)
                        categoryViewModel.addCategory(newCategory) { addSuccess, _ ->
                            if (addSuccess) {
                                addedCount++
                                if (addedCount == defaultCategories.size) {
                                    categoryViewModel.getAllCategories { _, _, refreshedData ->
                                        if (refreshedData != null) {
                                            categories = refreshedData
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    categories = data
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Details",
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
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 12.dp),
                            color = Brown,
                            strokeWidth = 2.dp
                        )
                    } else {
                        TextButton(
                            onClick = {
                                if (imageUri != null && !isUploading) {
                                    isUploading = true
                                    // Use uploadImageWithBackgroundRemoval for clothes images
                                    commonViewModel.uploadImageWithBackgroundRemoval(
                                        context,
                                        imageUri
                                    ) { imageUrl ->
                                        if (imageUrl != null) {
                                            val model = ClothesModel(
                                                clothesName = clothesName.trim(),
                                                brand = brand.trim(),
                                                season = season.trim(),
                                                color = color.trim(),
                                                occasion = occasion.trim(),
                                                price = price.trim(),
                                                notes = notes.trim(),
                                                categoryId = selectedCategoryId,
                                                categoryName = selectedCategoryName,
                                                image = imageUrl
                                            )
                                            clothesViewModel.addClothes(model) { success, message ->
                                                isUploading = false
                                                Toast.makeText(context, message, Toast.LENGTH_LONG)
                                                    .show()
                                                if (success) activity?.finish()
                                            }
                                        } else {
                                            isUploading = false
                                            Toast.makeText(
                                                context,
                                                "Failed to process image",
                                                Toast.LENGTH_SHORT
                                            ).show()
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
                            },
                            enabled = clothesName.isNotBlank() && selectedCategoryId.isNotBlank() && imageUri != null && !isUploading
                        ) {
                            Text(
                                "Save",
                                color = if (clothesName.isNotBlank() && selectedCategoryId.isNotBlank() && imageUri != null && !isUploading)
                                    Brown else Grey,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                    titleContentColor = if (ThemeManager.isDarkMode) OnSurface_Dark else Brown,
                    navigationIconContentColor = if (ThemeManager.isDarkMode) OnSurface_Dark else Brown
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

            Divider(color = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        "Clothes Name *",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = clothesName,
                        onValueChange = { clothesName = it },
                        placeholder = {
                            Text(
                                "Enter clothes name",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                    fontSize = 14.sp
                                )
                            )
                        },
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
                        "Category *",
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
                            value = selectedCategoryName,
                            onValueChange = { },
                            placeholder = {
                                Text(
                                    "Select category",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                        fontSize = 14.sp
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCategoryDropdown = true },
                            enabled = false,
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = if (selectedCategoryName.isNotEmpty()) Brown else Grey.copy(
                                    alpha = 0.5f
                                ),
                                disabledTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else Brown,
                                disabledContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White
                            ),
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
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
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(if (ThemeManager.isDarkMode) Surface_Dark else White)
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            category.categoryName,
                                            color = if (ThemeManager.isDarkMode) OnSurface_Dark else Brown
                                        )
                                    },
                                    onClick = {
                                        selectedCategoryId = category.categoryId
                                        selectedCategoryName = category.categoryName
                                        showCategoryDropdown = false
                                    },
                                    modifier = Modifier.background(if (ThemeManager.isDarkMode) Surface_Dark else White)
                                )
                            }
                            Divider(color = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Light_grey)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_add_24),
                                            contentDescription = null,
                                            tint = Brown,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            "Add New Category",
                                            color = Brown,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                onClick = {
                                    showCategoryDropdown = false
                                    showAddCategoryDialog = true
                                },
                                modifier = Modifier.background(if (ThemeManager.isDarkMode) Surface_Dark else White)
                            )
                        }
                    }

                    Column {
                        Text(
                            "Color",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                            ),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = color,
                            onValueChange = { color = it },
                            placeholder = {
                                Text(
                                    "Enter color",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                        fontSize = 14.sp
                                    )
                                )
                            },
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
                            "Price",
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
                            placeholder = {
                                Text(
                                    "Enter price",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                        fontSize = 14.sp
                                    )
                                )
                            },
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
                            "Season",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                            ),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = season,
                            onValueChange = { season = it },
                            placeholder = {
                                Text(
                                    "Enter season",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                        fontSize = 14.sp
                                    )
                                )
                            },
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
                            "Occasion",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                            ),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = occasion,
                            onValueChange = { occasion = it },
                            placeholder = {
                                Text(
                                    "Enter occasion",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                        fontSize = 14.sp
                                    )
                                )
                            },
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
                            placeholder = {
                                Text(
                                    "Enter brand",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                        fontSize = 14.sp
                                    )
                                )
                            },
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
                            "Notes",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (ThemeManager.isDarkMode) OnBackground_Dark else DarkGrey
                            ),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = {
                                Text(
                                    "Enter notes",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                        fontSize = 14.sp
                                    )
                                )
                            },
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
                }
            }
        }

        if (showAddCategoryDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddCategoryDialog = false
                    newCategoryName = ""
                },
                title = {
                    Text(
                        text = "Add New Category",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter category name:",
                            fontSize = 14.sp,
                            color = if (ThemeManager.isDarkMode) OnSurface_Dark.copy(alpha = 0.7f) else Grey
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brown,
                                unfocusedBorderColor = Grey,
                                unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                                focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                                focusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light,
                                unfocusedTextColor = if (ThemeManager.isDarkMode) OnSurface_Dark else OnSurface_Light
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                val categoryToAdd = newCategoryName
                                val newCategory = CategoryModel(categoryName = categoryToAdd)
                                categoryViewModel.addCategory(newCategory) { success, message ->
                                    if (success) {
                                        Handler(getMainLooper()).postDelayed({
                                            categoryViewModel.getAllCategories { _, _, data ->
                                                if (data != null) {
                                                    categories = data
                                                    val addedCategory =
                                                        data.find { it.categoryName == categoryToAdd }
                                                    if (addedCategory != null) {
                                                        selectedCategoryId =
                                                            addedCategory.categoryId
                                                        selectedCategoryName =
                                                            addedCategory.categoryName
                                                    }
                                                }
                                            }
                                        }, 300)
                                        newCategoryName = ""
                                        showAddCategoryDialog = false
                                    } else {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Add", color = Brown, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddCategoryDialog = false
                            newCategoryName = ""
                        }
                    ) {
                        Text("Cancel", color = Grey)
                    }
                },
                containerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                titleContentColor = if (ThemeManager.isDarkMode) OnSurface_Dark else Brown,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
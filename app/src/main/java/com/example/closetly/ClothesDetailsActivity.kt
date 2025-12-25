package com.example.closetly

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.ui.theme.*

class ClothesDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageUris = intent.getStringArrayListExtra("IMAGE_URIS")?.map { Uri.parse(it) } ?: emptyList()

        setContent {
            ClothesDetailsBody(imageUris = imageUris)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothesDetailsBody(
    imageUris: List<Uri>
) {
    val context = LocalContext.current
    var clothesName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var season by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    var categories by remember { mutableStateOf(CategoryPreferences.getCategories(context).filter { it != "All" }) }

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
                    TextButton(
                        onClick = {
                            // TODO: Save clothes to database
                            (context as? ComponentActivity)?.finish()
                        },
                        enabled = clothesName.isNotBlank() && selectedCategory.isNotBlank()
                    ) {
                        Text(
                            "Save",
                            color = if (clothesName.isNotBlank() && selectedCategory.isNotBlank())
                                Brown else Grey,
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
                .verticalScroll(rememberScrollState())
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imageUris) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
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
                OutlinedTextField(
                    value = clothesName,
                    onValueChange = { clothesName = it },
                    label = { Text("Clothes Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        focusedLabelColor = Brown,
                        unfocusedContainerColor = White,
                        focusedContainerColor = White
                    )
                )

                Box {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { },
                        label = { Text("Category *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDropdown = true },
                        enabled = false,
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = if (selectedCategory.isNotEmpty()) Brown else Grey.copy(alpha = 0.5f),
                            disabledLabelColor = if (selectedCategory.isNotEmpty()) Brown else Grey,
                            disabledTextColor = Black,
                            disabledContainerColor = White
                        ),
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Grey
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(White)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, color = Black) },
                                onClick = {
                                    selectedCategory = category
                                    showCategoryDropdown = false
                                },
                                modifier = Modifier.background(White)
                            )
                        }
                        Divider(color = Light_grey)
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
                                    Text("Add New Category", color = Brown, fontWeight = FontWeight.Medium)
                                }
                            },
                            onClick = {
                                showCategoryDropdown = false
                                showAddCategoryDialog = true
                            },
                            modifier = Modifier.background(White)
                        )
                    }
                }

                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        focusedLabelColor = Brown,
                        unfocusedContainerColor = White,
                        focusedContainerColor = White
                    )
                )

                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        focusedLabelColor = Brown,
                        unfocusedContainerColor = White,
                        focusedContainerColor = White
                    )
                )

                OutlinedTextField(
                    value = season,
                    onValueChange = { season = it },
                    label = { Text("Season") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        focusedLabelColor = Brown,
                        unfocusedContainerColor = White,
                        focusedContainerColor = White
                    )
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        focusedLabelColor = Brown,
                        unfocusedContainerColor = White,
                        focusedContainerColor = White
                    )
                )
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
                        color = Grey
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
                            unfocusedContainerColor = White,
                            focusedContainerColor = White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategoryName.isNotBlank() && !categories.contains(newCategoryName)) {
                            CategoryPreferences.addCategory(context, newCategoryName)
                            categories = CategoryPreferences.getCategories(context).filter { it != "All" }
                            selectedCategory = newCategoryName
                            newCategoryName = ""
                            showAddCategoryDialog = false
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
            containerColor = White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
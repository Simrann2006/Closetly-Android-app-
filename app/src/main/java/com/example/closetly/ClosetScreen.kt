package com.example.closetly

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.model.ClothesModel
import com.example.closetly.repository.ClothesRepoImpl
import com.example.closetly.ui.theme.Black
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.Grey
import com.example.closetly.ui.theme.Light_brown
import com.example.closetly.ui.theme.Light_grey
import com.example.closetly.ui.theme.Skin
import com.example.closetly.repository.CategoryRepoImpl
import com.example.closetly.viewmodel.CategoryViewModel
import com.example.closetly.ui.theme.White
import com.example.closetly.viewmodel.ClothesViewModel

object CategoryPreferences {
    private const val PREFS_NAME = "closetly_categories"
    private const val KEY_CATEGORIES = "categories"
    private const val DEFAULT_CATEGORIES = "All,Tops,Bottoms,Shoes"

    fun getCategories(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val categoriesString = prefs.getString(KEY_CATEGORIES, DEFAULT_CATEGORIES) ?: DEFAULT_CATEGORIES
        return categoriesString.split(",")
    }

    private fun saveCategories(context: Context, categories: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CATEGORIES, categories.joinToString(",")).apply()
    }
}

@Composable
fun ClosetScreen() {

    val context = LocalContext.current
    val categoryRepo = remember { CategoryRepoImpl() }
    val categoryViewModel = remember { CategoryViewModel(categoryRepo) }
    val clothesRepo = remember { ClothesRepoImpl() }
    val clothesViewModel = remember { ClothesViewModel(clothesRepo) }
    
    var selectedCategory by remember { mutableStateOf("All") }
    var searchText by remember { mutableStateOf("") }

    var categories by remember { mutableStateOf(listOf("All")) }
    var allClothes by remember { mutableStateOf<List<ClothesModel>>(emptyList()) }

    LaunchedEffect(Unit) {
        categoryViewModel.getAllCategories { success, _, data ->
            if (success && data != null) {
                val categoryNames = listOf("All") + data.map { it.categoryName }
                categories = categoryNames
            }
        }
    }

    LaunchedEffect(Unit) {
        clothesViewModel.getAllClothes { success, _, data ->
            if (success && data != null) {
                allClothes = data
            }
        }
    }

    val filteredClothes = remember(selectedCategory, allClothes) {
        if (selectedCategory == "All") {
            allClothes
        } else {
            allClothes.filter { it.categoryName == selectedCategory }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    placeholder = {
                        Text(
                            "Search your closet...",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = Grey
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.baseline_search_24),
                            contentDescription = null,
                            tint = Grey,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Light_grey,
                        focusedContainerColor = Light_grey,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Brown
                    ),
                    textStyle = TextStyle(fontSize = 14.sp),
                    singleLine = true
                )

                FloatingActionButton(
                    onClick = {
                        context.startActivity(Intent(context, AddClothesActivity::class.java))
                    },
                    containerColor = Skin,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_add_24),
                        contentDescription = null
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Skin
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Try on Avatar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Light_grey
            ) {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { category ->
                        CategoryButton(
                            text = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredClothes.size) { index ->
                    val clothes = filteredClothes[index]
                    ClothesItem(clothes = clothes)
                }
            }
        }
    }
}

@Composable
fun ClothesItem(clothes: ClothesModel) {
    var showDialog by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableStateOf(0) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable { showDialog = true },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
            ) {
                AsyncImage(
                    model = clothes.image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    text = clothes.clothesName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = clothes.categoryName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Grey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
    
    if (showDialog) {
        ClothesDetailsDialog(
            clothes = clothes,
            onDismiss = { showDialog = false },
            onDeleted = { 
                showDialog = false
                refreshKey++
            }
        )
    }
}

@Composable
fun ClothesDetailsDialog(
    clothes: ClothesModel,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val clothesRepo = remember { ClothesRepoImpl() }
    val clothesViewModel = remember { ClothesViewModel(clothesRepo) }
    val categoryRepo = remember { CategoryRepoImpl() }
    val categoryViewModel = remember { CategoryViewModel(categoryRepo) }
    
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                AsyncImage(
                    model = clothes.image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Light_grey, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = clothes.clothesName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                DetailRow(label = "Brand", value = clothes.brand)
                DetailRow(label = "Category", value = clothes.categoryName)
                DetailRow(label = "Season", value = clothes.season)
                
                if (clothes.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notes:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Grey
                    )
                    Text(
                        text = clothes.notes,
                        fontSize = 14.sp,
                        color = Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_edit_24),
                            contentDescription = null,
                            tint = Color.Green
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_delete_24),
                            contentDescription = null,
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
    
    if (showEditDialog) {
        EditClothesDialog(
            clothes = clothes,
            clothesViewModel = clothesViewModel,
            categoryViewModel = categoryViewModel,
            onDismiss = { 
                showEditDialog = false
                onDismiss()
            },
            onSaved = {
                showEditDialog = false
                onDismiss()
            }
        )
    }
    
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            clothesName = clothes.clothesName,
            onConfirm = {
                clothesViewModel.deleteClothes(clothes.clothesId) { success, message ->
                    if (success) {
                        onDeleted()
                    }
                }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
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
fun EditClothesDialog(
    clothes: ClothesModel,
    clothesViewModel: ClothesViewModel,
    categoryViewModel: CategoryViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    var clothesName by remember { mutableStateOf(clothes.clothesName) }
    var brand by remember { mutableStateOf(clothes.brand) }
    var season by remember { mutableStateOf(clothes.season) }
    var notes by remember { mutableStateOf(clothes.notes) }
    var selectedCategory by remember { mutableStateOf(clothes.categoryName) }
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var expandedCategory by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        categoryViewModel.getAllCategories { success, _, data ->
            if (success && data != null) {
                categories = data.map { it.categoryName }
            }
        }
    }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Edit Clothes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = clothesName,
                    onValueChange = { clothesName = it },
                    label = { Text("Clothes Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey
                    )
                )
                
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
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { },
                        label = { Text("Category") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedCategory = true },
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
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false },
                        modifier = Modifier.background(White)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expandedCategory = true }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = season,
                    onValueChange = { season = it },
                    label = { Text("Season") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
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
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Light_grey
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", color = Black)
                    }
                    
                    Button(
                        onClick = {
                            val updatedClothes = clothes.copy(
                                clothesName = clothesName,
                                brand = brand,
                                categoryName = selectedCategory,
                                season = season,
                                notes = notes
                            )
                            clothesViewModel.editClothes(updatedClothes) { success, message ->
                                if (success) {
                                    onSaved()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brown
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", color = White)
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    clothesName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Clothes?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("Are you sure you want to delete \"$clothesName\"? This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CategoryButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(34.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) White else Light_grey,
            contentColor = if (isSelected) Brown else Grey
        ),
        shape = RoundedCornerShape(17.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp,
            pressedElevation = if (isSelected) 4.dp else 1.dp
        ),
        border = if (isSelected) BorderStroke(
            width = 1.5.dp,
            brush = SolidColor(Brown)
        ) else null,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
@Preview
fun PreviewCloset() {
    ClosetScreen()
}
package com.example.closetly.view

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.BarChart
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.closetly.model.CategoryModel
import com.example.closetly.model.ClothesModel
import com.example.closetly.repository.ClothesRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.repository.CategoryRepoImpl
import com.example.closetly.viewmodel.CategoryViewModel
import com.example.closetly.viewmodel.ClothesViewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.window.Dialog
import com.example.closetly.R
import com.example.closetly.utils.ThemeManager
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosetScreen() {

    val context = LocalContext.current

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val categoryRepo = remember(currentUserId) { CategoryRepoImpl() }
    val categoryViewModel = remember(currentUserId) { CategoryViewModel(categoryRepo) }
    val clothesRepo = remember(currentUserId) { ClothesRepoImpl() }
    val clothesViewModel = remember(currentUserId) { ClothesViewModel(clothesRepo) }

    var selectedCategory by remember { mutableStateOf("All") }
    var searchText by remember { mutableStateOf("") }

    var categories by remember(currentUserId) { mutableStateOf(listOf("All")) }
    var allCategoriesData by remember(currentUserId) { mutableStateOf<List<CategoryModel>>(emptyList()) }
    var allClothes by remember(currentUserId) { mutableStateOf<List<ClothesModel>>(emptyList()) }
    var isLoading by remember(currentUserId) { mutableStateOf(true) }
    var categoriesLoaded by remember(currentUserId) { mutableStateOf(false) }
    var clothesLoaded by remember(currentUserId) { mutableStateOf(false) }

    var isRefreshing by remember { mutableStateOf(false) }

    fun refreshData() {
        isRefreshing = true
        var categoriesDone = false
        var clothesDone = false

        categoryViewModel.getAllCategories { success, _, data ->
            if (success && data != null) {
                allCategoriesData = data
                val categoryNames = listOf("All") + data.map { it.categoryName }
                categories = categoryNames
            }
            categoriesDone = true
            if (categoriesDone && clothesDone) isRefreshing = false
        }

        clothesViewModel.getAllClothes { success, _, data ->
            if (success && data != null) {
                allClothes = data
            }
            clothesDone = true
            if (categoriesDone && clothesDone) isRefreshing = false
        }
    }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUserId) {
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
                                            allCategoriesData = refreshedData
                                            val categoryNames = listOf("All") + refreshedData.map { it.categoryName }
                                            categories = categoryNames
                                            categoriesLoaded = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    allCategoriesData = data
                    val categoryNames = listOf("All") + data.map { it.categoryName }
                    categories = categoryNames
                    categoriesLoaded = true
                }
            }
        }
    }

    LaunchedEffect(currentUserId) {
        clothesViewModel.getAllClothes { success, _, data ->
            if (success && data != null) {
                allClothes = data
            }
            clothesLoaded = true
        }
    }

    LaunchedEffect(categoriesLoaded, clothesLoaded) {
        if (categoriesLoaded && clothesLoaded) {
            isLoading = false
        }
    }

    val filteredClothes = remember(selectedCategory, allClothes, searchText) {
        var clothes = if (selectedCategory == "All") {
            allClothes
        } else {
            allClothes.filter { it.categoryName == selectedCategory }
        }

        if (searchText.isNotEmpty()) {
            clothes = clothes.filter { item ->
                item.clothesName.contains(searchText, ignoreCase = true) ||
                        item.brand.contains(searchText, ignoreCase = true) ||
                        item.color.contains(searchText, ignoreCase = true) ||
                        item.categoryName.contains(searchText, ignoreCase = true) ||
                        item.season.contains(searchText, ignoreCase = true)
            }
        }

        clothes
    }

    val pullToRefreshState = rememberPullToRefreshState()

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { refreshData() },
            modifier = Modifier.fillMaxSize(),
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    containerColor = if (ThemeManager.isDarkMode) Black else White,
                    color = if (ThemeManager.isDarkMode) White else Black
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (ThemeManager.isDarkMode) Background_Dark else Background_Light)
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
                                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
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
                            unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else Light_grey,
                            focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else Light_grey,
                            unfocusedBorderColor = if (ThemeManager.isDarkMode) Grey.copy(alpha = 0.3f) else Color.Transparent,
                            focusedBorderColor = Brown,
                            focusedTextColor = if (ThemeManager.isDarkMode) White else Black,
                            unfocusedTextColor = if (ThemeManager.isDarkMode) White else Black
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp
                        ),
                        singleLine = true
                    )

                    FloatingActionButton(
                        onClick = {
                            val intent = Intent(context, AddActivity::class.java)
                            intent.putExtra("FLOW_TYPE", AddFlow.CLOSET)
                            context.startActivity(intent)
                        },
                        containerColor = Brown,
                        contentColor = White,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_add_24),
                            contentDescription = null,
                            tint = White
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        context.startActivity(Intent(context, PlanOutfitActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brown
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_checkroom_24),
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Plan Outfit",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = White
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(context, SavedOutfitsActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Brown
                    ),
                    border = BorderStroke(1.5.dp, Brown),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_folder_special_24),
                        contentDescription = null,
                        tint = Brown,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Saved Outfits",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Brown
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = if (ThemeManager.isDarkMode) Surface_Dark else Light_grey
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
                                onLongPress = {
                                    val defaultCategories = listOf("All", "Tops", "Bottoms", "Shoes")
                                    if (!defaultCategories.contains(category)) {
                                        categoryToDelete = category
                                        showDeleteDialog = true
                                    }
                                }
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

        if (showDeleteDialog && categoryToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    categoryToDelete = null
                },
                title = {
                    Text(
                        text = "Delete Category",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (ThemeManager.isDarkMode) White else Black
                        )
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete '$categoryToDelete'? All items in this category will also be deleted.",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp,
                            color = if (ThemeManager.isDarkMode) White.copy(alpha = 0.9f) else Black
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val categoryName = categoryToDelete
                            val categoryData = allCategoriesData.firstOrNull { it.categoryName == categoryName }
                            val categoryId = categoryData?.categoryId ?: ""

                            if (categoryId.isNotEmpty()) {
                                categoryViewModel.deleteCategory(categoryId) { success, message ->
                                    if (success) {
                                        if (selectedCategory == categoryName) {
                                            selectedCategory = "All"
                                        }
                                        categoryViewModel.getAllCategories { _, _, refreshedData ->
                                            if (refreshedData != null) {
                                                allCategoriesData = refreshedData
                                                categories = listOf("All") + refreshedData.map { it.categoryName }
                                            }
                                        }
                                        clothesViewModel.getAllClothes { success, _, data ->
                                            if (success && data != null) {
                                                allClothes = data
                                            }
                                        }
                                    }
                                }
                            }
                            showDeleteDialog = false
                            categoryToDelete = null
                        }
                    ) {
                        Text(
                            "Delete",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                color = Color.Red,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            categoryToDelete = null
                        }
                    ) {
                        Text(
                            "Cancel",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                color = Grey,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                },
                containerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (ThemeManager.isDarkMode)
                            Background_Dark.copy(alpha = 0.8f)
                        else
                            White.copy(alpha = 0.8f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Skin,
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
            }
        }

        FloatingActionButton(
            onClick = {
                val intent = Intent(context, AnalysisActivity::class.java)
                context.startActivity(intent)
            },
            containerColor = Brown,
            contentColor = White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.BarChart,
                contentDescription = "View Analysis",
                modifier = Modifier.size(24.dp)
            )
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
        colors = CardDefaults.cardColors(
            containerColor = if (ThemeManager.isDarkMode)
                Surface_Dark.copy(alpha = 0.7f)
            else
                White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (ThemeManager.isDarkMode) White else Black
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = clothes.categoryName,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Grey
                    ),
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

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (ThemeManager.isDarkMode)
                    Surface_Dark.copy(alpha = 0.95f)
                else
                    White
            )
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
                        .background(
                            if (ThemeManager.isDarkMode)
                                Surface_Dark
                            else
                                Light_grey,
                            RoundedCornerShape(12.dp)
                        ),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = clothes.clothesName,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ThemeManager.isDarkMode) White else Black
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(label = "Category", value = clothes.categoryName)
                DetailRow(label = "Brand", value = clothes.brand)
                DetailRow(label = "Price", value = clothes.price)
                DetailRow(label = "Color", value = clothes.color)
                DetailRow(label = "Season", value = clothes.season)

                if (clothes.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notes:",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Grey
                        )
                    )
                    Text(
                        text = clothes.notes,
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            fontSize = 14.sp,
                            color = if (ThemeManager.isDarkMode) White.copy(alpha = 0.9f) else Black
                        ),
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
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Grey
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (ThemeManager.isDarkMode) White else Black
            )
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
    var color by remember { mutableStateOf(clothes.color) }
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (ThemeManager.isDarkMode)
                    Surface_Dark.copy(alpha = 0.95f)
                else
                    White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Edit Clothes",
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ThemeManager.isDarkMode) White else Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = clothesName,
                    onValueChange = { clothesName = it },
                    label = {
                        Text(
                            "Clothes Name",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular))
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey,
                        focusedTextColor = if (ThemeManager.isDarkMode) White else Black,
                        unfocusedTextColor = if (ThemeManager.isDarkMode) White else Black,
                        focusedLabelColor = Brown,
                        unfocusedLabelColor = Grey
                    ),
                    textStyle = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular))
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { },
                        label = {
                            Text(
                                "Category",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.poppins_regular))
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedCategory = true },
                        readOnly = true,
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Grey,
                            disabledTextColor = if (ThemeManager.isDarkMode) White else Black
                        ),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_arrow_drop_down_24),
                                contentDescription = null,
                                tint = Grey
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = FontFamily(Font(R.font.poppins_regular))
                        )
                    )

                    DropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false },
                        modifier = Modifier.background(
                            if (ThemeManager.isDarkMode) Surface_Dark else White
                        )
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        category,
                                        style = TextStyle(
                                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                            color = if (ThemeManager.isDarkMode) White else Black
                                        )
                                    )
                                },
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
                    value = brand,
                    onValueChange = { brand = it },
                    label = {
                        Text(
                            "Brand",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular))
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey,
                        focusedTextColor = if (ThemeManager.isDarkMode) White else Black,
                        unfocusedTextColor = if (ThemeManager.isDarkMode) White else Black,
                        focusedLabelColor = Brown,
                        unfocusedLabelColor = Grey
                    ),
                    textStyle = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular))
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = {
                        Text(
                            "Color",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular))
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey,
                        focusedTextColor = if (ThemeManager.isDarkMode) White else Black,
                        unfocusedTextColor = if (ThemeManager.isDarkMode) White else Black,
                        focusedLabelColor = Brown,
                        unfocusedLabelColor = Grey
                    ),
                    textStyle = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular))
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = season,
                    onValueChange = { season = it },
                    label = {
                        Text(
                            "Season",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular))
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey,
                        focusedTextColor = if (ThemeManager.isDarkMode) White else Black,
                        unfocusedTextColor = if (ThemeManager.isDarkMode) White else Black,
                        focusedLabelColor = Brown,
                        unfocusedLabelColor = Grey
                    ),
                    textStyle = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular))
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = {
                        Text(
                            "Notes",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular))
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey,
                        focusedTextColor = if (ThemeManager.isDarkMode) White else Black,
                        unfocusedTextColor = if (ThemeManager.isDarkMode) White else Black,
                        focusedLabelColor = Brown,
                        unfocusedLabelColor = Grey
                    ),
                    maxLines = 4,
                    textStyle = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular))
                    )
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
                        Text(
                            "Cancel",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                color = Black
                            )
                        )
                    }

                    Button(
                        onClick = {
                            val updatedClothes = clothes.copy(
                                clothesName = clothesName,
                                brand = brand,
                                categoryName = selectedCategory,
                                season = season,
                                color = color,
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
                        Text(
                            "Save",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                color = White
                            )
                        )
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
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontWeight = FontWeight.Bold,
                    color = if (ThemeManager.isDarkMode) White else Black
                )
            )
        },
        text = {
            Text(
                "Are you sure you want to delete \"$clothesName\"? This action cannot be undone.",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    color = if (ThemeManager.isDarkMode) White.copy(alpha = 0.9f) else Black
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Red
                )
            ) {
                Text(
                    "Delete",
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular))
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        color = Grey
                    )
                )
            }
        },
        containerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .height(34.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        color = if (isSelected) {
            if (ThemeManager.isDarkMode) Surface_Dark else White
        } else {
            if (ThemeManager.isDarkMode) Background_Dark else Light_grey
        },
        shape = RoundedCornerShape(17.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        border = if (isSelected) BorderStroke(
            width = 1.5.dp,
            brush = SolidColor(Brown)
        ) else null
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) Brown else Grey
                )
            )
        }
    }
}

@Composable
@Preview
fun PreviewCloset() {
    ClosetScreen()
}
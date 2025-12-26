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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

    fun addCategory(context: Context, category: String) {
        val categories = getCategories(context).toMutableList()
        if (!categories.contains(category)) {
            categories.add(category)
            saveCategories(context, categories)
        }
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
                    contentDescription = clothes.clothesName,
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
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun ClothesDetailsDialog(
    clothes: ClothesModel,
    onDismiss: () -> Unit
) {
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
                    contentDescription = clothes.clothesName,
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
            }
        }
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
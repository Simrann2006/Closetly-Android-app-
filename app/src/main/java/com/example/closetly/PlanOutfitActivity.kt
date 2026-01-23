package com.example.closetly

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.closetly.model.ClothesModel
import com.example.closetly.model.OutfitItemModel
import com.example.closetly.model.OutfitModel
import com.example.closetly.repository.ClothesRepoImpl
import com.example.closetly.repository.OutfitRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.viewmodel.ClothesViewModel
import com.example.closetly.viewmodel.OutfitViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class PositionedClothesItem(
    val clothes: ClothesModel,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    var scale: Float = 1f,
    var width: Float = 120f,
    var height: Float = 120f,
    val id: String = clothes.clothesId
)

class PlanOutfitActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val outfitId = intent.getStringExtra("outfitId")
        val selectedDate = intent.getStringExtra("selectedDate")
        
        setContent {
            PlanOutfitScreen(
                outfitId = outfitId,
                preSelectedDate = selectedDate
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanOutfitScreen(
    outfitId: String? = null,
    preSelectedDate: String? = null
) {
    val context = LocalContext.current
    val clothesRepo = remember { ClothesRepoImpl() }
    val clothesViewModel = remember { ClothesViewModel(clothesRepo) }
    val outfitRepo = remember { OutfitRepoImpl() }
    val outfitViewModel = remember { OutfitViewModel(outfitRepo) }

    var allClothes by remember { mutableStateOf<List<ClothesModel>>(emptyList()) }
    var selectedClothes by remember { mutableStateOf<List<PositionedClothesItem>>(emptyList()) }
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var canvasWidth by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0f) }
    
    var outfitName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(preSelectedDate ?: "") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var occasion by remember { mutableStateOf("") }
    var occasionNotes by remember { mutableStateOf("") }
    var isMultiDay by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    
    var showSaveDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showOccasionPicker by remember { mutableStateOf(false) }
    
    val occasions = listOf("Casual", "Formal", "Work", "Party", "Sport", "Date", "Beach", "Wedding", "Travel", "Other")
    
    LaunchedEffect(outfitId) {
        outfitId?.let { id ->
            outfitViewModel.getOutfitById(id) { success, _, outfit ->
                if (success && outfit != null) {
                    outfitName = outfit.outfitName
                    selectedDate = outfit.plannedDate
                    startDate = outfit.startDate
                    endDate = outfit.endDate
                    occasion = outfit.occasion
                    occasionNotes = outfit.occasionNotes
                    isMultiDay = outfit.isMultiDay()
                    isFavorite = outfit.isFavorite
                    
                    val clothesIds = outfit.items.map { it.clothesId }
                    clothesViewModel.getAllClothes { clothesSuccess, _, clothes ->
                        if (clothesSuccess && clothes != null) {
                            val matchingClothes = clothes.filter { it.clothesId in clothesIds }
                            selectedClothes = outfit.items.mapNotNull { item ->
                                matchingClothes.find { it.clothesId == item.clothesId }?.let { cloth ->
                                    PositionedClothesItem(
                                        clothes = cloth,
                                        offsetX = item.offsetX,
                                        offsetY = item.offsetY,
                                        scale = item.scale
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        clothesViewModel.getAllClothes { success, _, clothes ->
            if (success && clothes != null) {
                allClothes = clothes
            }
            isLoading = false
        }
    }

    val filteredClothes = remember(allClothes, selectedCategory, searchQuery, selectedClothes) {
        allClothes.filter { clothes ->
            val matchesCategory = selectedCategory == "All" || clothes.categoryName == selectedCategory
            val matchesSearch = searchQuery.isEmpty() || 
                               clothes.clothesName.contains(searchQuery, ignoreCase = true) ||
                               clothes.brand.contains(searchQuery, ignoreCase = true)
            val notSelected = !selectedClothes.any { it.clothes.clothesId == clothes.clothesId }
            matchesCategory && matchesSearch && notSelected
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (outfitId != null) "Edit Outfit" else "Plan Outfit",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isFavorite = !isFavorite
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Grey
                        )
                    }
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = Brown)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Black,
                    navigationIconContentColor = Black
                )
            )
        },
        containerColor = Light_grey
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFFFFF)
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Outfit",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black
                        )
                        Text(
                            text = "${selectedClothes.size} items",
                            fontSize = 14.sp,
                            color = Grey
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (selectedClothes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 2.dp,
                                    color = Grey.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(
                                    color = Color(0xFFFFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedItemId = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_add_circle_outline_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Grey
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap items below to add them here\nDrag to position your outfit",
                                    fontSize = 14.sp,
                                    color = Grey,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .onGloballyPositioned { coordinates ->
                                    canvasWidth = coordinates.size.width.toFloat()
                                    canvasHeight = coordinates.size.height.toFloat()
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        selectedItemId = null
                                    }
                                }
                        ) {
                            selectedClothes.forEach { positionedItem ->
                                DraggableOutfitItem(
                                    positionedItem = positionedItem,
                                    isSelected = selectedItemId == positionedItem.id,
                                    onSelect = { selectedItemId = positionedItem.id },
                                    canvasWidth = canvasWidth,
                                    canvasHeight = canvasHeight,
                                    onPositionChange = { newX, newY, newScale ->
                                        selectedClothes = selectedClothes.map {
                                            if (it.id == positionedItem.id) {
                                                it.copy(offsetX = newX, offsetY = newY, scale = newScale)
                                            } else it
                                        }
                                    },
                                    onRemove = {
                                        selectedClothes = selectedClothes.filter { it.id != positionedItem.id }
                                        selectedItemId = null
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { showBottomSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brown),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_add_circle_outline_24),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Items from Closet")
            }
            
            AnimatedVisibility(
                visible = selectedItemId != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Button(
                    onClick = {
                        selectedItemId?.let { id ->
                            selectedClothes = selectedClothes.filter { it.id != id }
                            selectedItemId = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Selected Item")
                }
            }
        }

        if (showSaveDialog) {
            OutfitSaveDialog(
                outfitName = outfitName,
                onOutfitNameChange = { outfitName = it },
                selectedDate = selectedDate,
                onDateSelect = { selectedDate = it },
                startDate = startDate,
                onStartDateSelect = { startDate = it },
                endDate = endDate,
                onEndDateSelect = { endDate = it },
                occasion = occasion,
                onOccasionSelect = { occasion = it },
                occasionNotes = occasionNotes,
                onOccasionNotesChange = { occasionNotes = it },
                isMultiDay = isMultiDay,
                onMultiDayToggle = { isMultiDay = it },
                occasions = occasions,
                onDismiss = { showSaveDialog = false },
                onSave = {
                    if (selectedClothes.isEmpty()) {
                        Toast.makeText(context, "Please add items to your outfit", Toast.LENGTH_SHORT).show()
                        return@OutfitSaveDialog
                    }
                    
                    if (outfitName.isEmpty()) {
                        Toast.makeText(context, "Please enter outfit name", Toast.LENGTH_SHORT).show()
                        return@OutfitSaveDialog
                    }
                    
                    val outfit = OutfitModel(
                        outfitId = outfitId ?: "",
                        outfitName = outfitName,
                        items = selectedClothes.mapIndexed { index, positionedItem ->
                            OutfitItemModel(
                                clothesId = positionedItem.clothes.clothesId,
                                clothesName = positionedItem.clothes.clothesName,
                                categoryName = positionedItem.clothes.categoryName,
                                image = positionedItem.clothes.image,
                                position = index,
                                offsetX = positionedItem.offsetX,
                                offsetY = positionedItem.offsetY,
                                scale = positionedItem.scale
                            )
                        },
                        plannedDate = if (!isMultiDay) selectedDate else "",
                        startDate = if (isMultiDay) startDate else "",
                        endDate = if (isMultiDay) endDate else "",
                        occasion = occasion,
                        occasionNotes = occasionNotes,
                        isFavorite = isFavorite,
                        thumbnailUrl = selectedClothes.firstOrNull()?.clothes?.image ?: "",
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    if (outfitId != null) {
                        outfitViewModel.editOutfit(outfit) { success, message ->
                            if (success) {
                                Toast.makeText(context, "Outfit updated successfully", Toast.LENGTH_SHORT).show()
                                (context as? ComponentActivity)?.finish()
                            } else {
                                Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        outfitViewModel.addOutfit(outfit) { success, message ->
                            if (success) {
                                Toast.makeText(context, "Outfit saved successfully", Toast.LENGTH_SHORT).show()
                                (context as? ComponentActivity)?.finish()
                            } else {
                                Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        }

        if (showFilterDialog) {
            CategoryFilterDialog(
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it },
                onDismiss = { showFilterDialog = false }
            )
        }
        
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = White,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Your Closet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Search items...", fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Grey)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = White,
                                unfocusedContainerColor = White,
                                focusedBorderColor = Brown,
                                unfocusedBorderColor = Grey.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Button(
                            onClick = { showFilterDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Brown),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Brown)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp)
                        ) {
                            items(filteredClothes) { clothes ->
                                DraggableClothesItem(
                                    clothes = clothes,
                                    onAddToOutfit = {
                                        val randomX = (0..250).random().toFloat()
                                        val randomY = (0..250).random().toFloat()
                                        selectedClothes = selectedClothes + PositionedClothesItem(
                                            clothes = clothes,
                                            offsetX = randomX,
                                            offsetY = randomY
                                        )
                                        showBottomSheet = false
                                    }
                                )
                            }
                            
                            if (filteredClothes.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No items found",
                                            fontSize = 14.sp,
                                            color = Grey
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DraggableClothesItem(
    clothes: ClothesModel,
    onAddToOutfit: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .scale(scale)
            .clickable { 
                onAddToOutfit()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        onAddToOutfit()
                    }
                )
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AsyncImage(
                    model = clothes.image,
                    contentDescription = clothes.clothesName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(Brown, CircleShape)
                        .padding(4.dp),
                    tint = White
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = clothes.clothesName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Black,
                    maxLines = 1
                )
                Text(
                    text = clothes.categoryName,
                    fontSize = 10.sp,
                    color = Grey,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun DraggableOutfitItem(
    positionedItem: PositionedClothesItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    canvasWidth: Float,
    canvasHeight: Float,
    onPositionChange: (Float, Float, Float) -> Unit,
    onRemove: () -> Unit
) {
    var offsetX by remember { mutableStateOf(positionedItem.offsetX) }
    var offsetY by remember { mutableStateOf(positionedItem.offsetY) }
    var scale by remember { mutableStateOf(positionedItem.scale) }
    
    val baseSize = 120.dp
    val baseSizePx = with(LocalDensity.current) { baseSize.toPx() }
    
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(baseSize)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.3f, 5f)
                    
                    if (canvasWidth > 0 && canvasHeight > 0) {
                        val visualSize = baseSizePx * scale
                        val minX = (visualSize - baseSizePx) / 2
                        val maxX = canvasWidth - baseSizePx + (baseSizePx - visualSize) / 2
                        val minY = (visualSize - baseSizePx) / 2
                        val maxY = canvasHeight - baseSizePx + (baseSizePx - visualSize) / 2
                        
                        offsetX = if (minX <= maxX) {
                            (offsetX + pan.x).coerceIn(minX, maxX)
                        } else {
                            (offsetX + pan.x).coerceIn(maxX, minX)
                        }
                        offsetY = if (minY <= maxY) {
                            (offsetY + pan.y).coerceIn(minY, maxY)
                        } else {
                            (offsetY + pan.y).coerceIn(maxY, minY)
                        }
                    } else {
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                    
                    onPositionChange(offsetX, offsetY, scale)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    onSelect()
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, Color(0xFFBEFF00), RoundedCornerShape(8.dp))
                    } else Modifier
                )
        ) {
            AsyncImage(
                model = positionedItem.clothes.image,
                contentDescription = positionedItem.clothes.clothesName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun SelectedClothesItem(
    clothes: ClothesModel,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            AsyncImage(
                model = clothes.image,
                contentDescription = clothes.clothesName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .offset(x = 4.dp, y = (-4).dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = "Remove",
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.Red, CircleShape),
                tint = White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitSaveDialog(
    outfitName: String,
    onOutfitNameChange: (String) -> Unit,
    selectedDate: String,
    onDateSelect: (String) -> Unit,
    startDate: String,
    onStartDateSelect: (String) -> Unit,
    endDate: String,
    onEndDateSelect: (String) -> Unit,
    occasion: String,
    onOccasionSelect: (String) -> Unit,
    occasionNotes: String,
    onOccasionNotesChange: (String) -> Unit,
    isMultiDay: Boolean,
    onMultiDayToggle: (Boolean) -> Unit,
    occasions: List<String>,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    var showOccasionMenu by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = "Save Outfit",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = outfitName,
                    onValueChange = onOutfitNameChange,
                    label = { Text("Outfit Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        focusedLabelColor = Brown
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Multi-day event", fontSize = 14.sp, color = Black)
                    Switch(
                        checked = isMultiDay,
                        onCheckedChange = onMultiDayToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = Brown)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isMultiDay) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showDatePicker(context) { date ->
                                    onStartDateSelect(date)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Brown
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Start Date", fontSize = 10.sp)
                                Text(
                                    text = if (startDate.isEmpty()) "Select" else startDate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        OutlinedButton(
                            onClick = {
                                showDatePicker(context) { date ->
                                    onEndDateSelect(date)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Brown
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("End Date", fontSize = 10.sp)
                                Text(
                                    text = if (endDate.isEmpty()) "Select" else endDate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            showDatePicker(context) { date ->
                                onDateSelect(date)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Brown
                        )
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedDate.isEmpty()) "Select Date (Optional)" else selectedDate,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ExposedDropdownMenuBox(
                    expanded = showOccasionMenu,
                    onExpandedChange = { showOccasionMenu = it }
                ) {
                    OutlinedTextField(
                        value = occasion,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Occasion (Optional)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showOccasionMenu)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Brown,
                            focusedLabelColor = Brown
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showOccasionMenu,
                        onDismissRequest = { showOccasionMenu = false }
                    ) {
                        occasions.forEach { occ ->
                            DropdownMenuItem(
                                text = { Text(occ) },
                                onClick = {
                                    onOccasionSelect(occ)
                                    showOccasionMenu = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = occasionNotes,
                    onValueChange = onOccasionNotesChange,
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brown,
                        focusedLabelColor = Brown
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Grey)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Brown)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryFilterDialog(
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf("All", "Tops", "Bottoms", "Shoes", "Accessories", "Outerwear", "Dresses")
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Filter by Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                categories.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCategorySelect(category)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == category,
                            onClick = {
                                onCategorySelect(category)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Brown)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = category,
                            fontSize = 16.sp,
                            color = Black
                        )
                    }
                }
            }
        }
    }
}

private fun showDatePicker(context: android.content.Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(selectedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.show()
}

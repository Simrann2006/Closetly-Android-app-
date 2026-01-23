package com.example.closetly

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.closetly.model.OutfitModel
import com.example.closetly.repository.OutfitRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.viewmodel.OutfitViewModel

class SavedOutfitsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            SavedOutfitsScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedOutfitsScreen() {
    val context = LocalContext.current
    val outfitRepo = remember { OutfitRepoImpl() }
    val outfitViewModel = remember { OutfitViewModel(outfitRepo) }

    var allOutfits by remember { mutableStateOf<List<OutfitModel>>(emptyList()) }
    var displayedOutfits by remember { mutableStateOf<List<OutfitModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedOutfit by remember { mutableStateOf<OutfitModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var outfitToDelete by remember { mutableStateOf<OutfitModel?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val tabs = listOf("All Outfits", "Scheduled", "Favorites")

    LaunchedEffect(refreshTrigger) {
        outfitViewModel.getAllOutfits { success, _, outfits ->
            if (success && outfits != null) {
                allOutfits = outfits
            }
            isLoading = false
        }
    }

    LaunchedEffect(allOutfits, selectedTab, searchQuery) {
        displayedOutfits = when (selectedTab) {
            0 -> allOutfits
            1 -> allOutfits.filter { it.hasDate() || it.isMultiDay() }
            2 -> allOutfits.filter { it.isFavorite }
            else -> allOutfits
        }.filter { outfit ->
            searchQuery.isEmpty() ||
                    outfit.outfitName.contains(searchQuery, ignoreCase = true) ||
                    outfit.occasion.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Outfits",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Black,
                    navigationIconContentColor = Black
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    context.startActivity(Intent(context, PlanOutfitActivity::class.java))
                },
                containerColor = Brown,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Outfit")
            }
        },
        containerColor = Light_grey
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search outfits...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Grey)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Grey)
                        }
                    }
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

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = White,
                contentColor = Brown,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Brown
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Brown)
                }
            } else if (displayedOutfits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_checkroom_24),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Grey.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No outfits found" else "No outfits yet",
                            fontSize = 16.sp,
                            color = Grey,
                            textAlign = TextAlign.Center
                        )
                        if (searchQuery.isEmpty()) {
                            Button(
                                onClick = {
                                    context.startActivity(Intent(context, PlanOutfitActivity::class.java))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Brown)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Your First Outfit")
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedOutfits) { outfit ->
                        OutfitCard(
                            outfit = outfit,
                            onClick = {
                                selectedOutfit = outfit
                                showDetailDialog = true
                            },
                            onEdit = {
                                val intent = Intent(context, PlanOutfitActivity::class.java)
                                intent.putExtra("outfitId", outfit.outfitId)
                                context.startActivity(intent)
                            },
                            onDelete = {
                                outfitToDelete = outfit
                                showDeleteDialog = true
                            },
                            onToggleFavorite = {
                                outfitViewModel.toggleFavorite(outfit) { success, message ->
                                    if (success) {
                                        refreshTrigger++
                                    } else {
                                        Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showDetailDialog && selectedOutfit != null) {
            OutfitDetailDialog(
                outfit = selectedOutfit!!,
                onDismiss = { showDetailDialog = false },
                onEdit = {
                    showDetailDialog = false
                    val intent = Intent(context, PlanOutfitActivity::class.java)
                    intent.putExtra("outfitId", selectedOutfit!!.outfitId)
                    context.startActivity(intent)
                },
                onDelete = {
                    showDetailDialog = false
                    outfitToDelete = selectedOutfit
                    showDeleteDialog = true
                },
                onMarkWorn = {
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    outfitViewModel.markAsWorn(selectedOutfit!!.outfitId, today) { success, message ->
                        if (success) {
                            Toast.makeText(context, "Marked as worn!", Toast.LENGTH_SHORT).show()
                            refreshTrigger++
                            showDetailDialog = false
                        } else {
                            Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        if (showDeleteDialog && outfitToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = Color.White,
                title = {
                    Text("Delete Outfit?")
                },
                text = {
                    Text("Are you sure you want to delete \"${outfitToDelete!!.outfitName}\"? This action cannot be undone.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            outfitViewModel.deleteOutfit(outfitToDelete!!.outfitId) { success, message ->
                                if (success) {
                                    Toast.makeText(context, "Outfit deleted", Toast.LENGTH_SHORT).show()
                                    refreshTrigger++
                                } else {
                                    Toast.makeText(context, "Error: $message", Toast.LENGTH_SHORT).show()
                                }
                            }
                            showDeleteDialog = false
                            outfitToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = Grey)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OutfitCard(
    outfit: OutfitModel,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White)
                ) {
                    if (outfit.items.isNotEmpty()) {
                        OutfitLayoutPreview(outfit)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = outfit.outfitName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (outfit.occasion.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = outfit.occasion,
                            fontSize = 12.sp,
                            color = Brown,
                            maxLines = 1
                        )
                    }
                    
                    if (outfit.hasDate() || outfit.isMultiDay()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Grey
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = outfit.getDisplayDate(),
                                fontSize = 11.sp,
                                color = Grey,
                                maxLines = 1
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${outfit.items.size} items",
                        fontSize = 11.sp,
                        color = Grey
                    )
                }
            }

            if (outfit.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(White.copy(alpha = 0.9f), CircleShape)
                        .padding(4.dp),
                    tint = Color.Red
                )
            }

            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Black
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        showMenu = false
                        onEdit()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (outfit.isFavorite) "Remove from Favorites" else "Add to Favorites") },
                    onClick = {
                        showMenu = false
                        onToggleFavorite()
                    },
                    leadingIcon = {
                        Icon(
                            if (outfit.isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    }
                )
            }
        }
    }
}

@Composable
fun OutfitDetailDialog(
    outfit: OutfitModel,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkWorn: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f)
                        .background(Color.White)
                ) {
                    if (outfit.items.isNotEmpty()) {
                        OutfitLayoutPreview(outfit)
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Grey,
                            modifier = Modifier
                                .background(White.copy(alpha = 0.9f), CircleShape)
                                .padding(4.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = outfit.outfitName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Black,
                            modifier = Modifier.weight(1f)
                        )
                        if (outfit.isFavorite) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Favorite",
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (outfit.occasion.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow(
                            icon = Icons.Default.Event,
                            label = "Occasion",
                            value = outfit.occasion
                        )
                    }

                    if (outfit.hasDate() || outfit.isMultiDay()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Date",
                            value = outfit.getDisplayDate()
                        )
                    }

                    if (outfit.occasionNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(
                            icon = Icons.Default.Notes,
                            label = "Notes",
                            value = outfit.occasionNotes
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(
                        icon = Icons.Default.Checkroom,
                        label = "Items",
                        value = "${outfit.items.size} pieces"
                    )

                    if (outfit.wornCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(
                            icon = Icons.Default.Check,
                            label = "Worn",
                            value = "${outfit.wornCount} time${if (outfit.wornCount > 1) "s" else ""}"
                        )
                        if (outfit.lastWornDate.isNotEmpty()) {
                            Text(
                                text = "Last worn: ${outfit.lastWornDate}",
                                fontSize = 12.sp,
                                color = Grey,
                                modifier = Modifier.padding(start = 32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onMarkWorn,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Brown)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Worn", fontSize = 13.sp)
                        }
                        Button(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Brown)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Outfit")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Brown
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Grey,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = Black
            )
        }
    }
}

@Composable
fun OutfitLayoutPreview(outfit: OutfitModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        val baseCanvasSize = 400f
        
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val previewWidth = constraints.maxWidth.toFloat()
            val previewHeight = constraints.maxHeight.toFloat()
            val scaleFactor = minOf(previewWidth / baseCanvasSize, previewHeight / baseCanvasSize).coerceAtMost(1f)
            
            outfit.items.forEach { item ->
                val scaledOffsetX = item.offsetX * scaleFactor
                val scaledOffsetY = item.offsetY * scaleFactor
                val scaledSize = 120.dp.value * scaleFactor * item.scale
                
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                scaledOffsetX.toInt(),
                                scaledOffsetY.toInt()
                            )
                        }
                        .size(scaledSize.dp)
                ) {
                    AsyncImage(
                        model = item.image,
                        contentDescription = item.clothesName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
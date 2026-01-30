package com.example.closetly.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.example.closetly.R
import com.example.closetly.model.OutfitItemModel
import com.example.closetly.model.OutfitModel
import com.example.closetly.repository.OutfitRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.utils.ThemeManager
import com.example.closetly.viewmodel.OutfitViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavedOutfitsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val scrollToOutfitId = intent.getStringExtra("scrollToOutfitId")

        setContent {
            SavedOutfitsScreen(scrollToOutfitId = scrollToOutfitId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedOutfitsScreen(scrollToOutfitId: String? = null) {
    val context = LocalContext.current
    val outfitRepo = remember { OutfitRepoImpl() }
    val outfitViewModel = remember { OutfitViewModel(outfitRepo) }

    var allOutfits by remember { mutableStateOf<List<OutfitModel>>(emptyList()) }
    var displayedOutfits by remember { mutableStateOf<List<OutfitModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedOutfit by remember { mutableStateOf<OutfitModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var outfitToDelete by remember { mutableStateOf<OutfitModel?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var highlightedOutfitId by remember { mutableStateOf(scrollToOutfitId) }
    
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()

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
    
    LaunchedEffect(displayedOutfits, scrollToOutfitId) {
        if (scrollToOutfitId != null && displayedOutfits.isNotEmpty()) {
            val index = displayedOutfits.indexOfFirst { it.outfitId == scrollToOutfitId }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }
    
    LaunchedEffect(highlightedOutfitId) {
        if (highlightedOutfitId != null) {
            kotlinx.coroutines.delay(2000)
            highlightedOutfitId = null
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
                    containerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                    titleContentColor = if (ThemeManager.isDarkMode) White else Black,
                    navigationIconContentColor = if (ThemeManager.isDarkMode) White else Black
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
        containerColor = if (ThemeManager.isDarkMode) Background_Dark else Light_grey
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
                    Icon(Icons.Default.Search, contentDescription = null, tint = if (ThemeManager.isDarkMode) White.copy(alpha = 0.7f) else Grey)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = if (ThemeManager.isDarkMode) White.copy(alpha = 0.7f) else Grey)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                    unfocusedContainerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
                    focusedBorderColor = Brown,
                    unfocusedBorderColor = if (ThemeManager.isDarkMode) White.copy(alpha = 0.3f) else Grey.copy(alpha = 0.3f),
                    focusedTextColor = if (ThemeManager.isDarkMode) White else Black,
                    unfocusedTextColor = if (ThemeManager.isDarkMode) White else Black
                ),
                shape = RoundedCornerShape(12.dp)
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = if (ThemeManager.isDarkMode) Surface_Dark else White,
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
                            tint = if (ThemeManager.isDarkMode) White.copy(alpha = 0.5f) else Grey.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No outfits found" else "No outfits yet",
                            fontSize = 16.sp,
                            color = if (ThemeManager.isDarkMode) White.copy(alpha = 0.7f) else Grey,
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
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        outfitViewModel.getAllOutfits { success, _, outfits ->
                            if (success && outfits != null) {
                                allOutfits = outfits
                            }
                            isRefreshing = false
                        }
                    },
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
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                    items(displayedOutfits) { outfit ->
                        ImprovedOutfitCard(
                            outfit = outfit,
                            isHighlighted = outfit.outfitId == highlightedOutfitId,
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
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Date())
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
                containerColor = if (ThemeManager.isDarkMode) Surface_Dark else Color.White,
                title = {
                    Text("Delete Outfit?", color = if (ThemeManager.isDarkMode) White else Black)
                },
                text = {
                    Text("Are you sure you want to delete \"${outfitToDelete!!.outfitName}\"? This action cannot be undone.", color = if (ThemeManager.isDarkMode) White.copy(alpha = 0.9f) else Black)
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
fun ImprovedOutfitCard(
    outfit: OutfitModel,
    isHighlighted: Boolean = false,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val borderColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isHighlighted) Brown else {
            if (ThemeManager.isDarkMode) Color.Gray.copy(alpha = 0.3f) else Color.Transparent
        },
        animationSpec = androidx.compose.animation.core.tween(500),
        label = "border"
    )
    val borderWidth by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isHighlighted) 3.dp else {
            if (ThemeManager.isDarkMode) 1.dp else 0.dp
        },
        animationSpec = androidx.compose.animation.core.tween(500),
        label = "borderWidth"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (ThemeManager.isDarkMode) Surface_Dark else White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f)
                    .background(if (ThemeManager.isDarkMode) Background_Dark else White)
            ) {
                if (outfit.items.isNotEmpty()) {
                    OutfitLayoutPreview(outfit, isGridPreview = false)
                }

                if (outfit.isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFE57373)
                            )
                            Text(
                                text = "Favorite",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE57373)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(if (ThemeManager.isDarkMode) Surface_Dark.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = if (ThemeManager.isDarkMode) White else Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = if (ThemeManager.isDarkMode) Surface_Dark else White
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Outfit", color = if (ThemeManager.isDarkMode) White else Black) },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Brown)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (outfit.isFavorite) "Remove from Favorites" else "Add to Favorites", color = if (ThemeManager.isDarkMode) White else Black) },
                        onClick = {
                            showMenu = false
                            onToggleFavorite()
                        },
                        leadingIcon = {
                            Icon(
                                if (outfit.isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFE57373)
                            )
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    DropdownMenuItem(
                        text = { Text("Delete Outfit", color = Color.Red) },
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


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (outfit.outfitName.isNotEmpty()) {
                    Text(
                        text = outfit.outfitName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ThemeManager.isDarkMode) White else Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (outfit.occasion.isNotEmpty()) {
                        InfoChip(
                            icon = Icons.Default.Event,
                            text = outfit.occasion,
                            containerColor = Brown.copy(alpha = 0.1f),
                            contentColor = Brown
                        )
                    }

                    InfoChip(
                        icon = Icons.Default.Checkroom,
                        text = "${outfit.items.size} items",
                        containerColor = if (ThemeManager.isDarkMode) White.copy(alpha = 0.1f) else Grey.copy(alpha = 0.1f),
                        contentColor = if (ThemeManager.isDarkMode) White.copy(alpha = 0.7f) else Grey
                    )
                }

                if (outfit.hasDate() || outfit.isMultiDay()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (ThemeManager.isDarkMode) White.copy(alpha = 0.7f) else Grey
                        )
                        Text(
                            text = outfit.getDisplayDate(),
                            fontSize = 13.sp,
                            color = if (ThemeManager.isDarkMode) White.copy(alpha = 0.7f) else Grey
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
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
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (ThemeManager.isDarkMode) Surface_Dark else Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f)
                        .background(if (ThemeManager.isDarkMode) Background_Dark else Color(0xFFFAFAFA))
                ) {
                    if (outfit.items.isNotEmpty()) {
                        OutfitLayoutPreview(outfit, isGridPreview = false)
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(if (ThemeManager.isDarkMode) Surface_Dark.copy(alpha = 0.95f) else White.copy(alpha = 0.95f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (ThemeManager.isDarkMode) White else Black
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
                    if (outfit.outfitName.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = outfit.outfitName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ThemeManager.isDarkMode) White else Black,
                                modifier = Modifier.weight(1f)
                            )
                            if (outfit.isFavorite) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = "Favorite",
                                    tint = Color(0xFFE57373),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else if (outfit.isFavorite) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (outfit.occasion.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow(
                            icon = Icons.Default.Event,
                            label = "Occasion",
                            value = outfit.occasion
                        )
                    }

                    if (outfit.hasDate() || outfit.isMultiDay()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Date",
                            value = outfit.getDisplayDate()
                        )
                    }

                    if (outfit.occasionNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow(
                            icon = Icons.Default.Notes,
                            label = "Notes",
                            value = outfit.occasionNotes
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow(
                        icon = Icons.Default.Checkroom,
                        label = "Items",
                        value = "${outfit.items.size} pieces"
                    )

                    if (outfit.wornCount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow(
                            icon = Icons.Default.Check,
                            label = "Worn",
                            value = "${outfit.wornCount} time${if (outfit.wornCount > 1) "s" else ""}"
                        )
                        if (outfit.lastWornDate.isNotEmpty()) {
                            Text(
                                text = "Last worn: ${outfit.lastWornDate}",
                                fontSize = 12.sp,
                                color = if (ThemeManager.isDarkMode) White.copy(alpha = 0.7f) else Grey,
                                modifier = Modifier.padding(start = 32.dp, top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onMarkWorn,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Brown),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Worn", fontSize = 14.sp)
                        }
                        Button(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Brown),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit", fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Outfit", fontSize = 14.sp)
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
                color = if (ThemeManager.isDarkMode) White.copy(alpha = 0.7f) else Grey,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                color = if (ThemeManager.isDarkMode) White else Black,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

data class Bounds(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
)

fun calculateBounds(
    items: List<OutfitItemModel>,
    baseItemSizePx: Float
): Bounds {
    val lefts = items.map { it.offsetX }
    val rights = items.map { it.offsetX + baseItemSizePx * it.scale }
    val tops = items.map { it.offsetY }
    val bottoms = items.map { it.offsetY + baseItemSizePx * it.scale }

    return Bounds(
        minX = lefts.minOrNull() ?: 0f,
        maxX = rights.maxOrNull() ?: 0f,
        minY = tops.minOrNull() ?: 0f,
        maxY = bottoms.maxOrNull() ?: 0f
    )
}

@Composable
fun OutfitLayoutPreview(
    outfit: OutfitModel,
    isGridPreview: Boolean
) {
    val baseItemSize = 120f

    if (outfit.items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (ThemeManager.isDarkMode) Background_Dark else White)
        )
        return
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(if (ThemeManager.isDarkMode) Background_Dark else White),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalContext.current.resources.displayMetrics.density
        val containerWidthPx = constraints.maxWidth.toFloat()
        val containerHeightPx = constraints.maxHeight.toFloat()

        val bounds = calculateBounds(outfit.items, baseItemSize * density)
        val outfitWidthPx = bounds.maxX - bounds.minX
        val outfitHeightPx = bounds.maxY - bounds.minY

        if (outfitWidthPx <= 0 || outfitHeightPx <= 0) return@BoxWithConstraints

        val paddingPx = 20f
        val availableWidthPx = containerWidthPx - (paddingPx * 2)
        val availableHeightPx = containerHeightPx - (paddingPx * 2)

        val scaleFactor = minOf(
            availableWidthPx / outfitWidthPx,
            availableHeightPx / outfitHeightPx
        ).coerceIn(0.4f, 2.5f)

        val scaledWidthPx = outfitWidthPx * scaleFactor
        val scaledHeightPx = outfitHeightPx * scaleFactor

        val centerOffsetX = (containerWidthPx - scaledWidthPx) / 2
        val centerOffsetY = (containerHeightPx - scaledHeightPx) / 2

        Box(modifier = Modifier.fillMaxSize()) {
            outfit.items.forEach { item ->
                val originalItemSizePx = baseItemSize * density * item.scale
                
                val scaledItemSizePx = originalItemSizePx * scaleFactor
                
                val itemSizeDp = scaledItemSizePx / density

                val relativeX = (item.offsetX - bounds.minX) * scaleFactor
                val relativeY = (item.offsetY - bounds.minY) * scaleFactor

                val finalX = centerOffsetX + relativeX
                val finalY = centerOffsetY + relativeY

                Box(
                    modifier = Modifier
                        .offset { IntOffset(finalX.toInt(), finalY.toInt()) }
                        .size(itemSizeDp.dp)
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
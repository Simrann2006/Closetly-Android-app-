package com.example.closetly.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.closetly.R
import com.example.closetly.model.ActivityStats
import com.example.closetly.model.ClosetCategory
import com.example.closetly.model.UnderusedItem
import com.example.closetly.model.WornColor
import com.example.closetly.model.ClothesModel
import com.example.closetly.model.OutfitModel
import com.example.closetly.repository.ClothesRepoImpl
import com.example.closetly.repository.OutfitRepoImpl
import com.example.closetly.viewmodel.ClothesViewModel
import com.example.closetly.repository.AnalysisRepoImpl
import com.example.closetly.viewmodel.AnalysisViewModel
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.viewmodel.UserViewModel
import com.example.closetly.ui.theme.*
import com.example.closetly.utils.ThemeManager
import com.google.firebase.auth.FirebaseAuth

class AnalysisActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            ClosetlyTheme(darkTheme = ThemeManager.isDarkMode) {
                val clothesRepo = remember { ClothesRepoImpl() }
                val clothesViewModel = remember { ClothesViewModel(clothesRepo) }
                val analysisRepo = remember { AnalysisRepoImpl() }
                val analysisViewModel = remember { AnalysisViewModel(analysisRepo) }
                val outfitRepo = remember { OutfitRepoImpl() }
                AnalysisScreen(
                    clothesViewModel = clothesViewModel,
                    analysisViewModel = analysisViewModel,
                    outfitRepo = outfitRepo
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    clothesViewModel: ClothesViewModel,
    analysisViewModel: AnalysisViewModel,
    outfitRepo: OutfitRepoImpl,
    userImageUrl: String = ""
) {
    val context = LocalContext.current
    val isDarkMode = ThemeManager.isDarkMode
    val userViewModel = remember { UserViewModel(UserRepoImpl(context)) }
    
    var showCPWDialog by remember { mutableStateOf(false) }
    var clothesList by remember { mutableStateOf<List<ClothesModel>>(emptyList()) }
    var outfitsList by remember { mutableStateOf<List<OutfitModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var userProfilePicture by remember { mutableStateOf("") }

    val backgroundColor = if (isDarkMode) Background_Dark else Background_Light
    val primaryColor = if (isDarkMode) Primary_Dark else Primary_Light
    val onSurfaceColor = if (isDarkMode) OnSurface_Dark else OnSurface_Light

    LaunchedEffect(Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            userViewModel.getUserById(currentUserId) { success, _, user ->
                if (success && user != null) {
                    userProfilePicture = user.profilePicture
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        clothesViewModel.getAllClothes { success, _, data ->
            if (success && data != null) {
                clothesList = data
            }
        }
        outfitRepo.getAllOutfits { success, _, data ->
            if (success && data != null) {
                outfitsList = data
            }
            isLoading = false
        }
    }

    val activityStats = remember(clothesList, outfitsList) {
        analysisViewModel.calculateActivityStats(clothesList).copy(outfits = outfitsList.size)
    }

    val closetCategories = remember(clothesList) {
        analysisViewModel.calculateClosetBreakdown(clothesList)
    }

    val mostWornColors = remember(clothesList) {
        analysisViewModel.calculateMostWornColors(clothesList)
    }

    val underusedItem = remember(clothesList) {
        analysisViewModel.findUnderusedItem(clothesList)
    }

    val totalWearCount = remember(clothesList) {
        clothesList.sumOf { it.wearCount }
    }

    val pricedItems = remember(clothesList) {
        clothesList.filter { (it.price.toDoubleOrNull() ?: 0.0) > 0 }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analysis",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Cursive,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = if (ThemeManager.isDarkMode) White else Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = "Back",
                            tint = onSurfaceColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCPWDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Calculate CPW",
                            tint = onSurfaceColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = onSurfaceColor,
                    navigationIconContentColor = onSurfaceColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                HeaderSection(
                    userImageUrl = userProfilePicture,
                    activityStats = activityStats,
                    totalItems = clothesList.size,
                    totalOutfits = outfitsList.size,
                    isDarkMode = isDarkMode
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                if (closetCategories.isNotEmpty()) {
                    ClosetBreakdownCard(
                        categories = closetCategories,
                        isDarkMode = isDarkMode
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    EmptyStateCard(
                        message = "No clothes added yet. Start adding items to see your closet breakdown!",
                        isDarkMode = isDarkMode
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                SeasonalBreakdownCard(
                    clothesList = clothesList,
                    isDarkMode = isDarkMode
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (pricedItems.isNotEmpty()) {
                    PriceDistributionCard(
                        clothesList = pricedItems,
                        isDarkMode = isDarkMode
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (mostWornColors.isNotEmpty()) {
                        EnhancedMostWornColorCard(
                            colors = mostWornColors,
                            modifier = Modifier.weight(1f),
                            isDarkMode = isDarkMode
                        )
                    } else {
                        EmptyColorCard(
                            modifier = Modifier.weight(1f),
                            isDarkMode = isDarkMode
                        )
                    }

                    if (underusedItem != null) {
                        EnhancedUnderusedItemCard(
                            item = underusedItem,
                            modifier = Modifier.weight(1f),
                            isDarkMode = isDarkMode
                        )
                    } else {
                        EmptyUnderusedCard(
                            modifier = Modifier.weight(1f),
                            isDarkMode = isDarkMode
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (outfitsList.isNotEmpty()) {
                    OutfitAnalysisCard(
                        outfitsList = outfitsList,
                        isDarkMode = isDarkMode
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (pricedItems.isNotEmpty() && totalWearCount > 0) {
                    ValueAnalysisCard(
                        clothesList = clothesList,
                        isDarkMode = isDarkMode
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showCPWDialog) {
        CPWCalculatorDialog(
            onDismiss = { showCPWDialog = false },
            isDarkMode = isDarkMode
        )
    }
}

@Composable
private fun HeaderSection(
    userImageUrl: String,
    activityStats: ActivityStats,
    totalItems: Int,
    totalOutfits: Int,
    isDarkMode: Boolean
) {
    val textColor = if (isDarkMode) OnSurface_Dark else Color(0xFF6B4F54)
    val accentColor = if (isDarkMode) AICardDarkColor2 else Color(0xFFE56F7B)
    val cardColor = if (isDarkMode) DarkGrey else Color.White
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                if (isDarkMode) Primary_Dark else Color(0xFFD38799),
                                if (isDarkMode) AICardDarkColor2 else Color(0xFFB79A9F)
                            )
                        )
                    )
                    .padding(3.dp)
            ) {
                AsyncImage(
                    model = userImageUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "My Wardrobe",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Cursive,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = R.drawable.baseline_checkroom_24,
                count = totalItems,
                label = "Items",
                iconTint = accentColor,
                cardColor = cardColor,
                textColor = textColor,
                modifier = Modifier.weight(1f),
                isDarkMode = isDarkMode
            )
            StatCard(
                icon = R.drawable.outline_apparel_24,
                count = totalOutfits,
                label = "Outfits",
                iconTint = accentColor,
                cardColor = cardColor,
                textColor = textColor,
                modifier = Modifier.weight(1f),
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: Int,
    count: Int,
    label: String,
    iconTint: Color,
    cardColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(if (isDarkMode) 2.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = count.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CategorySpendingRow(
    category: String,
    amount: Double,
    textColor: Color,
    isDarkMode: Boolean
) {
    val accentColors = if (isDarkMode) {
        listOf(AICardDarkColor1, AICardDarkColor2, AICardDarkColor3)
    } else {
        listOf(AICardColor1, AICardColor2, AICardColor3)
    }
    val color = accentColors[category.hashCode() % accentColors.size]
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = category,
                fontSize = 14.sp,
                color = textColor
            )
        }
        
        Text(
            text = "$${String.format("%.0f", amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun ClosetBreakdownCard(
    categories: List<ClosetCategory>,
    isDarkMode: Boolean
) {
    var chartType by remember { mutableStateOf(0) }
    val cardColor = if (isDarkMode) DarkGrey else Color.White
    val textColor = if (isDarkMode) OnSurface_Dark else Color(0xFF6B4F54)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(if (isDarkMode) 2.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Closet Breakdown",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "${categories.size} categories",
                        fontSize = 13.sp,
                        color = textColor.copy(alpha = 0.6f)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ChartTypeButton(
                        icon = Icons.Default.PieChart,
                        isSelected = chartType == 0,
                        onClick = { chartType = 0 },
                        textColor = textColor,
                        isDarkMode = isDarkMode
                    )
                    ChartTypeButton(
                        icon = Icons.Default.DonutSmall,
                        isSelected = chartType == 1,
                        onClick = { chartType = 1 },
                        textColor = textColor,
                        isDarkMode = isDarkMode
                    )
                    ChartTypeButton(
                        icon = Icons.Default.BarChart,
                        isSelected = chartType == 2,
                        onClick = { chartType = 2 },
                        textColor = textColor,
                        isDarkMode = isDarkMode
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            when (chartType) {
                0 -> PieChart(
                    categories = categories,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    isDarkMode = isDarkMode
                )
                1 -> DonutChart(
                    categories = categories,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    isDarkMode = isDarkMode
                )
                2 -> VerticalBarChart(
                    categories = categories,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    isDarkMode = isDarkMode
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            CategoryLegend(categories = categories, textColor = textColor)
        }
    }
}

@Composable
private fun ChartTypeButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    textColor: Color,
    isDarkMode: Boolean
) {
    val backgroundColor = if (isSelected) {
        if (isDarkMode) Primary_Dark else Color(0xFFE8B4BC)
    } else {
        if (isDarkMode) DarkGrey1.copy(alpha = 0.3f) else Color(0xFFF5F5F5)
    }
    
    val iconColor = if (isSelected) {
        if (isDarkMode) Color.Black else Color.White
    } else {
        textColor.copy(alpha = 0.5f)
    }
    
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PieChart(
    categories: List<ClosetCategory>,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean
) {
    val animatable = remember { Animatable(0f) }
    val totalPercentage = categories.sumOf { it.percentage.toDouble() }.toFloat()
    
    LaunchedEffect(categories) {
        animatable.animateTo(1f, animationSpec = tween(1500, easing = FastOutSlowInEasing))
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2
            val centerX = size.width / 2
            val centerY = size.height / 2
            
            var startAngle = -90f
            
            categories.forEach { category ->
                val sweepAngle = (category.percentage / totalPercentage * 360f) * animatable.value
                
                drawArc(
                    color = category.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Fill
                )
                
                if (sweepAngle > 1f) {
                    drawArc(
                        color = if (isDarkMode) Color.Black.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.3f),
                        startAngle = startAngle + sweepAngle - 1f,
                        sweepAngle = 2f,
                        useCenter = true,
                        topLeft = Offset(centerX - radius, centerY - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Fill
                    )
                }
                
                startAngle += sweepAngle
            }
            
            drawCircle(
                color = if (isDarkMode) DarkGrey.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.3f),
                radius = radius * 0.2f,
                center = Offset(centerX, centerY)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = categories.size.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) OnSurface_Dark else Color(0xFF6B4F54)
            )
            Text(
                text = "Categories",
                fontSize = 11.sp,
                color = if (isDarkMode) OnSurface_Dark.copy(alpha = 0.7f) else Color(0xFF6B4F54).copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun DonutChart(
    categories: List<ClosetCategory>,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean
) {
    val animatable = remember { Animatable(0f) }
    val totalPercentage = categories.sumOf { it.percentage.toDouble() }.toFloat()
    
    LaunchedEffect(categories) {
        animatable.animateTo(1f, animationSpec = tween(1200))
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2
            val innerRadius = radius * 0.6f
            val centerX = size.width / 2
            val centerY = size.height / 2
            
            var startAngle = -90f
            
            categories.forEach { category ->
                val sweepAngle = (category.percentage / totalPercentage * 360f) * animatable.value
                
                drawArc(
                    color = category.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )
                
                startAngle += sweepAngle
            }
            
            drawCircle(
                color = if (isDarkMode) Background_Dark else Background_Light,
                radius = innerRadius,
                center = Offset(centerX, centerY)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = categories.size.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) OnSurface_Dark else OnSurface_Light
            )
            Text(
                text = "Categories",
                fontSize = 12.sp,
                color = if (isDarkMode) OnSurface_Dark.copy(alpha = 0.7f) else OnSurface_Light.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun VerticalBarChart(
    categories: List<ClosetCategory>,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean
) {
    val animatable = remember { Animatable(0f) }
    
    LaunchedEffect(categories) {
        animatable.animateTo(1f, animationSpec = tween(1000))
    }
    
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (categories.isEmpty()) return@Canvas
            
            val maxPercentage = categories.maxOf { it.percentage }
            val barWidth = size.width / (categories.size * 2f)
            val spacing = barWidth * 0.5f
            
            categories.forEachIndexed { index, category ->
                val barHeight = (category.percentage / maxPercentage) * size.height * 0.9f * animatable.value
                val x = spacing + (index * (barWidth + spacing))
                
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            category.color,
                            category.color.copy(alpha = 0.7f)
                        )
                    ),
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
        }
    }
}

@Composable
private fun SeasonalBreakdownCard(
    clothesList: List<ClothesModel>,
    isDarkMode: Boolean
) {
    val cardColor = if (isDarkMode) DarkGrey else Color.White
    val textColor = if (isDarkMode) OnSurface_Dark else Color(0xFF6B4F54)
    
    val seasonalData = clothesList
        .groupBy { it.season.ifEmpty { "Unspecified" } }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(if (isDarkMode) 2.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seasonal Breakdown",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            seasonalData.forEach { (season, count) ->
                SeasonRow(
                    season = season,
                    count = count,
                    total = clothesList.size,
                    textColor = textColor,
                    isDarkMode = isDarkMode
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SeasonRow(
    season: String,
    count: Int,
    total: Int,
    textColor: Color,
    isDarkMode: Boolean
) {
    val percentage = if (total > 0) (count.toFloat() / total) * 100 else 0f
    val seasonIcon = when (season.lowercase()) {
        "summer" -> Icons.Default.WbSunny
        "winter" -> Icons.Default.AcUnit
        "spring" -> Icons.Default.LocalFlorist
        "fall", "autumn" -> Icons.Default.Park
        else -> Icons.Default.CalendarMonth
    }
    val seasonColor = when (season.lowercase()) {
        "summer" -> if (isDarkMode) DarkYellow else Color(0xFFFFA726)
        "winter" -> if (isDarkMode) SkyBlue else Color(0xFF42A5F5)
        "spring" -> if (isDarkMode) Color(0xFF66BB6A) else Color(0xFF66BB6A)
        "fall", "autumn" -> if (isDarkMode) Color(0xFFFF7043) else Color(0xFFFF7043)
        else -> textColor
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = seasonIcon,
            contentDescription = null,
            tint = seasonColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = season,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Text(
                    text = "$count (${String.format("%.0f", percentage)}%)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(textColor.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(percentage / 100f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(seasonColor)
                )
            }
        }
    }
}

@Composable
private fun PriceDistributionCard(
    clothesList: List<ClothesModel>,
    isDarkMode: Boolean
) {
    val cardColor = if (isDarkMode) DarkGrey else Color.White
    val textColor = if (isDarkMode) OnSurface_Dark else Color(0xFF6B4F54)
    val accentColor = if (isDarkMode) Primary_Dark else Color(0xFFE8B4BC)
    
    val priceRanges = listOf(
        "Rs.0-500" to clothesList.count { (it.price.toDoubleOrNull() ?: 0.0) in 0.0..500.0 },
        "Rs.500-1K" to clothesList.count { (it.price.toDoubleOrNull() ?: 0.0) in 500.0..1000.0 },
        "Rs.1K-5K" to clothesList.count { (it.price.toDoubleOrNull() ?: 0.0) in 1000.0..5000.0 },
        "Rs.5K-10K" to clothesList.count { (it.price.toDoubleOrNull() ?: 0.0) in 5000.0..10000.0 },
        "Rs.10K+" to clothesList.count { (it.price.toDoubleOrNull() ?: 0.0) > 10000.0 }
    )
    
    val totalInvestment = clothesList.sumOf { it.price.toDoubleOrNull() ?: 0.0 }
    val avgPrice = if (clothesList.isNotEmpty()) totalInvestment / clothesList.size else 0.0
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(if (isDarkMode) 2.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Price Analysis",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "${clothesList.size} items tracked",
                        fontSize = 13.sp,
                        color = textColor.copy(alpha = 0.6f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = if (isDarkMode) DarkYellow else Color(0xFFFFC107),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PriceStatBox(
                    label = "Total Invested",
                    value = "Rs.${String.format("%.0f", totalInvestment)}",
                    textColor = textColor,
                    accentColor = accentColor,
                    isDarkMode = isDarkMode
                )
                PriceStatBox(
                    label = "Avg Price",
                    value = "Rs.${String.format("%.0f", avgPrice)}",
                    textColor = textColor,
                    accentColor = accentColor,
                    isDarkMode = isDarkMode
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Price Distribution",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val maxCount = priceRanges.maxOf { it.second }
            
            priceRanges.forEach { (range, count) ->
                PriceRangeRow(
                    range = range,
                    count = count,
                    maxCount = maxCount,
                    textColor = textColor,
                    accentColor = accentColor,
                    isDarkMode = isDarkMode
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun PriceStatBox(
    label: String,
    value: String,
    textColor: Color,
    accentColor: Color,
    isDarkMode: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDarkMode) DarkGrey1.copy(alpha = 0.3f) else accentColor.copy(alpha = 0.1f))
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PriceRangeRow(
    range: String,
    count: Int,
    maxCount: Int,
    textColor: Color,
    accentColor: Color,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = range,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.width(75.dp),
            maxLines = 1
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(textColor.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (maxCount > 0) count.toFloat() / maxCount else 0f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
        }
        
        Text(
            text = count.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier
                .padding(start = 12.dp)
                .width(30.dp)
        )
    }
}

@Composable
private fun EnhancedMostWornColorCard(
    colors: List<WornColor>,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean
) {
    val cardColor = if (isDarkMode) DarkGrey else Color.White
    val textColor = if (isDarkMode) OnSurface_Dark else Color(0xFF6B4F54)
    
    Card(
        modifier = modifier.height(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(if (isDarkMode) 2.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Top Colors",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                colors.take(3).forEach { wornColor ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(wornColor.color)
                                .then(
                                    if (wornColor.color == Color.White || wornColor.name.lowercase() == "white") {
                                        Modifier.background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color.LightGray.copy(alpha = 0.3f),
                                                    Color.White
                                                )
                                            )
                                        )
                                    } else Modifier
                                )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = wornColor.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedUnderusedItemCard(
    item: UnderusedItem,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean
) {
    val cardColor = if (isDarkMode) DarkGrey else Color.White
    val textColor = if (isDarkMode) OnSurface_Dark else Color(0xFF6B4F54)
    val accentColor = if (isDarkMode) AICardDarkColor2 else Color(0xFFD4A5AE)
    
    Card(
        modifier = modifier.height(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(if (isDarkMode) 2.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Underused",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            
            if (item.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Checkroom,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(45.dp)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.name,
                    fontSize = 13.sp,
                    color = textColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "0 wears",
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun ValueAnalysisCard(
    clothesList: List<ClothesModel>,
    isDarkMode: Boolean
) {
    val cardColor = if (isDarkMode) DarkGrey else Color.White
    val textColor = if (isDarkMode) OnSurface_Dark else Color(0xFF6B4F54)
    
    val bestValueItems = clothesList
        .filter { it.wearCount > 0 && (it.price.toDoubleOrNull() ?: 0.0) > 0 }
        .map { item ->
            val price = item.price.toDoubleOrNull() ?: 0.0
            val cpw = price / item.wearCount
            Triple(item, cpw, price)
        }
        .sortedBy { it.second }
        .take(5)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(if (isDarkMode) 2.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Best Value Items",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (isDarkMode) DarkYellow else Color(0xFFFFC107),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (bestValueItems.isEmpty()) {
                Text(
                    text = "Wear your items to see value analysis",
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                bestValueItems.forEach { (item, cpw, _) ->
                    ValueItemRow(
                        itemName = item.clothesName,
                        category = item.categoryName,
                        cpw = cpw,
                        wearCount = item.wearCount,
                        textColor = textColor,
                        isDarkMode = isDarkMode
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ValueItemRow(
    itemName: String,
    category: String,
    cpw: Double,
    wearCount: Int,
    textColor: Color,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDarkMode) DarkGrey1.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = itemName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                maxLines = 1
            )
            Text(
                text = "$category • $wearCount wears",
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Rs.${String.format("%.2f", cpw)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFF66BB6A) else Color(0xFF4CAF50)
            )
            Text(
                text = "per wear",
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun OutfitAnalysisCard(
    outfitsList: List<OutfitModel>,
    isDarkMode: Boolean
) {
    val cardColor = if (isDarkMode) DarkGrey else Color.White
    val textColor = if (isDarkMode) OnSurface_Dark else Color(0xFF6B4F54)
    
    val totalOutfits = outfitsList.size
    val totalWornOutfits = outfitsList.count { it.wornCount > 0 }
    val avgItemsPerOutfit = if (outfitsList.isNotEmpty()) {
        outfitsList.sumOf { it.items.size }.toFloat() / outfitsList.size
    } else 0f
    val favoriteOutfits = outfitsList.count { it.isFavorite }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(if (isDarkMode) 2.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Outfit Insights",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = if (isDarkMode) AICardDarkColor1 else Color(0xFFE91E63),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutfitStatBox(
                    value = totalOutfits.toString(),
                    label = "Total Outfits",
                    icon = Icons.Default.Collections,
                    textColor = textColor,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )
                
                OutfitStatBox(
                    value = totalWornOutfits.toString(),
                    label = "Worn",
                    icon = Icons.Default.CheckCircle,
                    textColor = textColor,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutfitStatBox(
                    value = String.format("%.1f", avgItemsPerOutfit),
                    label = "Avg Items",
                    icon = Icons.Default.Layers,
                    textColor = textColor,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )
                
                OutfitStatBox(
                    value = favoriteOutfits.toString(),
                    label = "Favorites",
                    icon = Icons.Default.Star,
                    textColor = textColor,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OutfitStatBox(
    value: String,
    label: String,
    icon: ImageVector,
    textColor: Color,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDarkMode) DarkGrey1.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CategoryLegend(
    categories: List<ClosetCategory>,
    textColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        categories.forEach { category ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(category.color)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = category.name,
                    fontSize = 14.sp,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "${category.percentage.toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CPWCalculatorDialog(
    onDismiss: () -> Unit,
    isDarkMode: Boolean
) {
    var cost by remember { mutableStateOf("") }
    var timesWorn by remember { mutableStateOf("") }
    var calculatedCPW by remember { mutableStateOf<Double?>(null) }
    var showError by remember { mutableStateOf(false) }
    
    val cardColor = if (isDarkMode) Surface_Dark else Color.White
    val textColor = if (isDarkMode) OnSurface_Dark else Color.Black
    val primaryColor = if (isDarkMode) Primary_Dark else Primary_Light
    val resultCardColor = if (isDarkMode) DarkGrey else Color(0xFFEFD9DC)
    val resultTextColor = if (isDarkMode) OnSurface_Dark else Color(0xFF6B4F54)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Calculate Cost Per Wear",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = cost,
                    onValueChange = {
                        cost = it
                        calculatedCPW = null
                        showError = false
                    },
                    label = { Text("Cost of the Item", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Cost",
                            tint = primaryColor
                        )
                    },
                    placeholder = { Text("Enter cost (e.g., 50)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cardColor,
                        unfocusedContainerColor = cardColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = if (isDarkMode) Grey else Grey.copy(alpha = 0.3f),
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = if (isDarkMode) Grey else Grey,
                        cursorColor = primaryColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = timesWorn,
                    onValueChange = {
                        timesWorn = it
                        calculatedCPW = null
                        showError = false
                    },
                    label = { Text("Times Worn", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Loop,
                            contentDescription = "Times Worn",
                            tint = primaryColor
                        )
                    },
                    placeholder = { Text("Enter number of times") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cardColor,
                        unfocusedContainerColor = cardColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = if (isDarkMode) Grey else Grey.copy(alpha = 0.3f),
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = if (isDarkMode) Grey else Grey,
                        cursorColor = primaryColor
                    )
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please enter valid numbers",
                        color = if (isDarkMode) Color(0xFFFF6B6B) else Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val costValue = cost.toDoubleOrNull()
                        val wornValue = timesWorn.toIntOrNull()

                        if (costValue != null && wornValue != null && wornValue > 0) {
                            calculatedCPW = costValue / wornValue
                            showError = false
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    )
                ) {
                    Text(
                        text = "Calculate",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkMode) Color.Black else Color.White
                    )
                }

                if (calculatedCPW != null) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = resultCardColor
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Cost Per Wear",
                                fontSize = 14.sp,
                                color = resultTextColor.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Rs.${String.format("%.2f", calculatedCPW)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = resultTextColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when {
                                    calculatedCPW!! < 5 -> "Excellent value! 🌟"
                                    calculatedCPW!! < 10 -> "Great value! 👍"
                                    calculatedCPW!! < 20 -> "Good value 👌"
                                    else -> "Consider wearing more often!"
                                },
                                fontSize = 12.sp,
                                color = resultTextColor.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Close",
                        color = primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    message: String,
    isDarkMode: Boolean
) {
    val cardColor = if (isDarkMode) DarkGrey else Color.White
    val textColor = if (isDarkMode) OnSurface_Dark else Color(0xFF6B4F54)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(if (isDarkMode) 2.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyColorCard(modifier: Modifier = Modifier, isDarkMode: Boolean) {
    val cardColor = if (isDarkMode) DarkGrey else Color.White
    val textColor = if (isDarkMode) OnSurface_Dark else Color(0xFF6B4F54)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(if (isDarkMode) 2.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.3f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No color data",
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun EmptyUnderusedCard(modifier: Modifier = Modifier, isDarkMode: Boolean) {
    val cardColor = if (isDarkMode) DarkGrey else Color.White
    val textColor = if (isDarkMode) OnSurface_Dark else Color(0xFF6B4F54)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(if (isDarkMode) 2.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.TrendingDown,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.3f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All items in use",
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}
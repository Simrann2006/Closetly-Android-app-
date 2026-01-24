package com.example.closetly.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.closetly.repository.ClothesRepoImpl
import com.example.closetly.viewmodel.ClothesViewModel
import com.example.closetly.utils.AnalysisUtils
import com.example.closetly.repository.UserRepoImpl
import com.example.closetly.viewmodel.UserViewModel
import com.example.closetly.ui.theme.Brown
import com.example.closetly.ui.theme.Grey
import com.example.closetly.ui.theme.White
import com.google.firebase.auth.FirebaseAuth

class AnalysisActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val clothesRepo = remember { ClothesRepoImpl() }
            val clothesViewModel = remember { ClothesViewModel(clothesRepo) }
            AnalysisScreen(clothesViewModel = clothesViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    clothesViewModel: ClothesViewModel,
    userImageUrl: String = ""
) {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl(context)) }
    var showCPWDialog by remember { mutableStateOf(false) }
    var clothesList by remember { mutableStateOf<List<ClothesModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var userProfilePicture by remember { mutableStateOf("") }

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
            isLoading = false
        }
    }

    val activityStats = remember(clothesList) {
        AnalysisUtils.calculateActivityStats(clothesList)
    }
    
    val closetCategories = remember(clothesList) {
        AnalysisUtils.calculateClosetBreakdown(clothesList)
    }
    
    val mostWornColors = remember(clothesList) {
        AnalysisUtils.calculateMostWornColors(clothesList)
    }
    
    val underusedItem = remember(clothesList) {
        AnalysisUtils.findUnderusedItem(clothesList)
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = White
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Brown)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                HeaderSection(
                    userImageUrl = userProfilePicture,
                    activityStats = activityStats
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                if (closetCategories.isNotEmpty()) {
                    ClosetBreakdownCard(categories = closetCategories)
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    EmptyStateCard(message = "No clothes added yet. Start adding items to see your closet breakdown!")
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (mostWornColors.isNotEmpty()) {
                        MostWornColorCard(
                            colors = mostWornColors,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        EmptyColorCard(modifier = Modifier.weight(1f))
                    }
                    
                    if (underusedItem != null) {
                        UnderusedItemCard(
                            item = underusedItem,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        EmptyUnderusedCard(modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showCPWDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brown
                    )
                ) {
                    Text(
                        text = "Calculate CPW",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
    if (showCPWDialog) {
        CPWCalculatorDialog(
            onDismiss = { showCPWDialog = false }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CPWCalculatorDialog(
    onDismiss: () -> Unit
) {
    var cost by remember { mutableStateOf("") }
    var timesWorn by remember { mutableStateOf("") }
    var calculatedCPW by remember { mutableStateOf<Double?>(null) }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = White
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
                    color = Color(0xFF6B4F54),
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
                    label = { Text("Cost of the Dress", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Cost",
                            tint = Brown
                        )
                    },
                    placeholder = { Text("Enter cost (e.g., 50)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey.copy(alpha = 0.3f),
                        focusedLabelColor = Brown,
                        unfocusedLabelColor = Grey
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
                            tint = Brown
                        )
                    },
                    placeholder = { Text("Enter number of times") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Brown,
                        unfocusedBorderColor = Grey.copy(alpha = 0.3f),
                        focusedLabelColor = Brown,
                        unfocusedLabelColor = Grey
                    )
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please enter valid numbers",
                        color = Color(0xFFD32F2F),
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
                        containerColor = Brown
                    )
                ) {
                    Text(
                        text = "Calculate",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                }

                if (calculatedCPW != null) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFEFD9DC)
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
                                color = Color(0xFF8B7075)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$${String.format("%.2f", calculatedCPW)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B4F54)
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
                                color = Color(0xFF8B7075),
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
                        color = Color(0xFF6B4F54),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
private fun HeaderSection(
    userImageUrl: String,
    activityStats: ActivityStats
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            AsyncImage(
                model = userImageUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB8A0A4)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "My Activity",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B4F54)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard(
                icon = Icons.Default.ShoppingCart,
                count = activityStats.items,
                label = "Items",
                iconTint = Color(0xFFE8B4BC)
            )
            StatCard(
                icon = Icons.Default.Person,
                count = activityStats.outfits,
                label = "Outfits",
                iconTint = Color(0xFFE8B4BC)
            )
            StatCard(
                icon = Icons.Default.Refresh,
                count = activityStats.reuse,
                label = "Reuse",
                iconTint = Color(0xFFE8B4BC)
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    count: Int,
    label: String,
    iconTint: Color
) {
    Card(
        modifier = Modifier.size(110.dp, 80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFD9DC)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Column {
                    Text(
                        text = count.toString(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B4F54)
                    )
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = Color(0xFF8B7075)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClosetBreakdownCard(categories: List<ClosetCategory>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFD9DC)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Closet Breakdown",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B4F54),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PieChart(
                categories = categories,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            CategoryLegend(categories = categories)
        }
    }
}

@Composable
private fun PieChart(
    categories: List<ClosetCategory>,
    modifier: Modifier = Modifier,
    animationDuration: Int = 1000
) {
    val totalPercentage = categories.sumOf { it.percentage.toDouble() }.toFloat()
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(key1 = categories) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = animationDuration)
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(200.dp)
        ) {
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
                    size = Size(radius * 2, radius * 2)
                )

                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 3f)
                )

                startAngle += sweepAngle
            }
        }
    }
}

@Composable
private fun CategoryLegend(categories: List<ClosetCategory>) {
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
                    color = Color(0xFF6B4F54),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${category.percentage.toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B4F54)
                )
            }
        }
    }
}

@Composable
private fun MostWornColorCard(
    colors: List<WornColor>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFD9DC)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Most Worn Color",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B4F54)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colors.forEach { wornColor ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(wornColor.color)
                                .then(
                                    if (wornColor.color == Color.White) {
                                        Modifier.background(Color.White)
                                    } else Modifier
                                )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = wornColor.name,
                            fontSize = 14.sp,
                            color = Color(0xFF6B4F54)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnderusedItemCard(
    item: UnderusedItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFD9DC)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Underused Item",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B4F54),
                modifier = Modifier.align(Alignment.Start)
            )

            if (item.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFD4A5AE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Text(
                text = item.name,
                fontSize = 14.sp,
                color = Color(0xFF6B4F54),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFD9DC)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF6B4F54),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyColorCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFD9DC)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Most Worn Color",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B4F54)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Add colors to your clothes",
                fontSize = 12.sp,
                color = Color(0xFF6B4F54).copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyUnderusedCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFD9DC)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Underused Item",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B4F54)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No items yet",
                fontSize = 12.sp,
                color = Color(0xFF6B4F54).copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

//@Composable
//@Preview
//fun PreviewAnalysisActivity(){
//    AnalysisScreen()
//}
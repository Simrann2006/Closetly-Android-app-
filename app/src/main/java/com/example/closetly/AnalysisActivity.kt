package com.example.closetl

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Data Models
data class ActivityStats(
    val items: Int,
    val outfits: Int,
    val reuse: Int
)

data class ClosetCategory(
    val name: String,
    val percentage: Float,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    userImageUrl: String = "",
    activityStats: ActivityStats = ActivityStats(128, 24, 12),
    closetCategories: List<ClosetCategory> = listOf(
        ClosetCategory("Tops", 35f, Color(0xFFE8B4BC)),
        ClosetCategory("Bottoms", 25f, Color(0xFFD4A5AE)),
        ClosetCategory("Dresses", 20f, Color(0xFFC096A0)),
        ClosetCategory("Outerwear", 12f, Color(0xFFAC8792)),
        ClosetCategory("Accessories", 8f, Color(0xFF987884))
    )
) {
    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5E6E8))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            HeaderSection(
                userImageUrl = userImageUrl,
                activityStats = activityStats
            )
            Spacer(modifier = Modifier.height(24.dp))
            ClosetBreakdownCard(categories = closetCategories)
            Spacer(modifier = Modifier.height(24.dp))
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

            // Pie Chart
            PieChart(
                categories = categories,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
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

                // Draw pie slice
                drawArc(
                    color = category.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )

                // Draw border between slices
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
@Preview
fun PreviewAnalysisActivity(){
    AnalysisScreen()
}
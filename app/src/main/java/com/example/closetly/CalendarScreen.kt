package com.example.closetly

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.closetly.model.ClothesModel
import com.example.closetly.model.WeatherData
import com.example.closetly.repository.ClothesRepoImpl
import com.example.closetly.repository.WeatherRepoImpl
import com.example.closetly.ui.theme.*
import com.example.closetly.viewmodel.ClothesViewModel
import com.example.closetly.viewmodel.WeatherViewModel
import com.google.android.gms.location.LocationServices
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.closetly.utils.OutfitRecommendationHelper
import com.example.closetly.utils.OutfitRecommendationHelper.aiCache
import kotlin.math.abs

data class AIRecommendationCache(
    val date: LocalDate,
    val temperature: Double,
    val weatherCondition: String,
    val clothesCount: Int,
    val recommendation: String,
    val selectedClothes: List<ClothesModel>,
    val explanation: String
)

@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showDialog by remember { mutableStateOf(false) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val weatherRepo = remember { WeatherRepoImpl() }
    val weatherViewModel = remember { WeatherViewModel(weatherRepo) }

    val clothesRepo = remember { ClothesRepoImpl() }
    val clothesViewModel = remember { ClothesViewModel(clothesRepo) }

    val weatherData by weatherViewModel.weatherData.observeAsState()
    val weatherLoading by weatherViewModel.loading.observeAsState(false)
    val allClothes = remember { mutableStateOf<List<ClothesModel>>(emptyList()) }
    
    var aiRecommendation by remember { mutableStateOf<String?>(null) }
    var aiLoading by remember { mutableStateOf(false) }
    var aiSelectedClothes by remember { mutableStateOf<List<ClothesModel>>(emptyList()) }
    var aiExplanation by remember { mutableStateOf<String?>(null) }
    
    
    var refreshTrigger by remember { mutableStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            getLocationAndFetchWeather(context, weatherViewModel)
            refreshTrigger++
        }
    }
    
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val currentPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (currentPermission) {
            hasLocationPermission = true
            getLocationAndFetchWeather(context, weatherViewModel)
            refreshTrigger++
        }
    }
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                
                if (currentPermission && !hasLocationPermission) {
                    hasLocationPermission = true
                    getLocationAndFetchWeather(context, weatherViewModel)
                    refreshTrigger++
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        clothesViewModel.getAllClothes { success, _, data ->
            if (success && data != null) {
                allClothes.value = data
            }
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            getLocationAndFetchWeather(context, weatherViewModel)
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    
    LaunchedEffect(weatherData, allClothes.value, refreshTrigger) {
        if (weatherData != null && allClothes.value.isNotEmpty()) {
            val today = LocalDate.now()
            val currentTemp = weatherData!!.temperature
            val currentCondition = weatherData!!.condition
            val currentClothesCount = allClothes.value.size

            val shouldRegenerate = aiCache == null ||
                aiCache!!.date != today ||
                kotlin.math.abs(aiCache!!.temperature - currentTemp) > 0.01 ||
                aiCache!!.weatherCondition != currentCondition ||
                aiCache!!.clothesCount != currentClothesCount ||
                refreshTrigger > 0 && refreshTrigger != (aiCache?.let { it.clothesCount + it.temperature.toInt() } ?: -1)

            if (shouldRegenerate) {
                aiLoading = true

                val selection = OutfitRecommendationHelper.getAISelectedOutfits(
                    currentTemp,
                    currentCondition,
                    allClothes.value
                )
                aiSelectedClothes = selection.selectedClothes
                aiExplanation = selection.explanation

                aiRecommendation = OutfitRecommendationHelper.getAIRecommendations(
                    currentTemp,
                    currentCondition,
                    aiSelectedClothes
                )

                aiCache = AIRecommendationCache(
                    date = today,
                    temperature = currentTemp,
                    weatherCondition = currentCondition,
                    clothesCount = currentClothesCount,
                    recommendation = aiRecommendation ?: "",
                    selectedClothes = aiSelectedClothes,
                    explanation = aiExplanation ?: ""
                )

                aiLoading = false
            } else {
                aiRecommendation = aiCache!!.recommendation
                aiSelectedClothes = aiCache!!.selectedClothes
                aiExplanation = aiCache!!.explanation
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        item {
            Spacer(Modifier.height(20.dp))
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = White
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                            Icon(
                                Icons.Default.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = DarkGrey1
                            )
                        }

                        Text(
                            text = "${
                                currentMonth.month.getDisplayName(
                                    TextStyle.FULL,
                                    Locale.getDefault()
                                )
                            } ${currentMonth.year}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGrey
                        )

                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = DarkGrey1
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkGrey1
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    val firstDayOfMonth = currentMonth.atDay(1)
                    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
                    val daysInMonth = currentMonth.lengthOfMonth()
                    val today = LocalDate.now()

                    var dayCounter = 1
                    for (week in 0..5) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (dayOfWeek in 0..6) {
                                val dayIndex = week * 7 + dayOfWeek

                                if (dayIndex >= firstDayOfWeek && dayCounter <= daysInMonth) {
                                    val date = currentMonth.atDay(dayCounter)
                                    val isToday = date == today
                                    val isSelected = date == selectedDate

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(4.dp)
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isToday -> CalendarPurple
                                                    isSelected -> CalendarBlue
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = if (isToday) 2.dp else 0.dp,
                                                color = if (isToday) CalendarPurple else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                selectedDate = date
                                                showDialog = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayCounter.toString(),
                                            fontSize = 14.sp,
                                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isToday || isSelected -> White
                                                else -> DarkGrey
                                            }
                                        )
                                    }
                                    dayCounter++
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
        
        item {
            Spacer(Modifier.height(20.dp))

            WeatherCard(
                weatherData = weatherData,
                isLoading = weatherLoading,
                onRefresh = {
                    if (hasLocationPermission) {
                        getLocationAndFetchWeather(context, weatherViewModel)
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                onRequestLocation = {
                    if (hasLocationPermission) {
                        settingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            )
        }
        
        if (weatherData != null && allClothes.value.isNotEmpty()) {
            item {
                Spacer(Modifier.height(16.dp))
                
                AIInsightsCard(
                    aiRecommendation = aiRecommendation,
                    isLoading = aiLoading,
                    onRefresh = {
                        aiCache = null
                        refreshTrigger++
                    }
                )
            }
        }

        if (weatherData != null && allClothes.value.isNotEmpty()) {
            item {
                Spacer(Modifier.height(16.dp))

                    val filteredClothes = if (!aiRecommendation.isNullOrBlank()) {
                        aiSelectedClothes.filter { clothes ->
                            aiRecommendation!!.contains(clothes.clothesName, ignoreCase = true)
                        }
                    } else aiSelectedClothes

                    OutfitRecommendationsCard(
                        weatherData = weatherData!!,
                        recommendedClothes = filteredClothes,
                        aiExplanation = aiExplanation,
                        isAILoading = aiLoading
                    )
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
        }
    }

    if (showDialog && selectedDate != null) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { showDialog = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text("×", fontSize = 24.sp, color = DarkGrey1)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "${
                                selectedDate!!.month.getDisplayName(
                                    TextStyle.FULL,
                                    Locale.getDefault()
                                )
                            } ${selectedDate!!.dayOfMonth}, ${selectedDate!!.year}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGrey
                        )

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = "No outfit scheduled",
                            fontSize = 15.sp,
                            color = LightGrey1
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = {
                                showDialog = false
                                TODO("Navigate to Avatar section")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CalendarPurple
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Plan Outfit",
                                color = White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
fun WeatherCard(
        weatherData: WeatherData?,
        isLoading: Boolean,
        onRefresh: () -> Unit,
        onRequestLocation: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                WeatherBlue,
                                SkyBlue
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Fetching weather...", color = White, fontSize = 14.sp)
                    }
                } else if (weatherData != null) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = weatherData.cityName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = White
                                )
                            }

                            IconButton(onClick = onRefresh) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_refresh_24),
                                    contentDescription = null,
                                    tint = White
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = OutfitRecommendationHelper.formatTemperature(weatherData.temperature),
                                    fontSize = 52.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )

                                Text(
                                    text = "${
                                        OutfitRecommendationHelper.formatTemperatureFahrenheit(
                                            weatherData.temperature
                                        )
                                    }",
                                    fontSize = 18.sp,
                                    color = White.copy(alpha = 0.9f)
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = weatherData.description.uppercase(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = White
                                )
                            }

                            Image(
                                painter = painterResource(id = OutfitRecommendationHelper.getWeatherIcon(
                                    weatherData.condition,
                                    weatherData.weatherIcon
                                )),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            WeatherDetailItem(
                                iconRes = R.drawable.humidity,
                                label = "Humidity",
                                value = "${weatherData.humidity}%"
                            )

                            WeatherDetailItem(
                                iconRes = R.drawable.thermometer,
                                label = "Feels Like",
                                value = OutfitRecommendationHelper.formatTemperature(weatherData.feelsLike)
                            )

                            WeatherDetailItem(
                                iconRes = R.drawable.wind,
                                label = "Wind",
                                value = "${weatherData.windSpeed} m/s"
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Weather Unavailable",
                            color = White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onRequestLocation,
                            colors = ButtonDefaults.buttonColors(containerColor = White)
                        ) {
                            Text("Enable Location", color = WeatherBlue)
                        }
                    }
                }
            }
        }
    }

@Composable
fun WeatherDetailItem(iconRes: Int, label: String, value: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = White.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
        }
    }

@Composable
fun OutfitRecommendationsCard(
    weatherData: WeatherData,
    recommendedClothes: List<ClothesModel>,
    aiExplanation: String?,
    isAILoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.outfit),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "AI Outfit Picks",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black
                )
            }

            Spacer(Modifier.height(20.dp))

            if (isAILoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = CalendarPurple)
                        Spacer(Modifier.height(12.dp))
                        Text("AI is selecting outfits...", color = Grey, fontSize = 14.sp)
                    }
                }
            } else if (recommendedClothes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add clothes to your closet to get recommendations!",
                            fontSize = 14.sp,
                            color = Grey,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(((recommendedClothes.size / 3 + 1) * 140).dp),
                        userScrollEnabled = false
                    ) {
                        items(
                            items = recommendedClothes,
                            key = { it.clothesId }
                        ) { clothes ->
                            RecommendedClothesItem(clothes)
                        }
                    }
                }
            }
        }
    }

@Composable
fun RecommendedClothesItem(clothes: ClothesModel) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(125.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = LightBlue),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = clothes.image,
                    contentDescription = clothes.clothesName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(White)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = clothes.clothesName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

@Composable
fun AIInsightsCard(
    aiRecommendation: String?,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AICardColor1,
                            AICardColor2,
                            AICardColor3
                        )
                    )
                )
                .padding(20.dp)
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "AI is analyzing your wardrobe...",
                        color = White,
                        fontSize = 14.sp
                    )
                }
            } else if (aiRecommendation != null) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.advice),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "AI Fashion Advisor",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                        
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_refresh_24),
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = aiRecommendation,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = White,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "AI insights coming soon...",
                        color = White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private fun getLocationAndFetchWeather(
        context: android.content.Context,
        weatherViewModel: WeatherViewModel
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        weatherViewModel.fetchWeatherByLocation(location.latitude, location.longitude)
                    }
                }
            }
        } catch (e: SecurityException) {
        }
    }

@Preview
@Composable
fun PreviewCalendar() {
    CalendarScreen()
}
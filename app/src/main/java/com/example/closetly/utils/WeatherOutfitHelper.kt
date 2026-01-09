package com.example.closetly.utils

import com.example.closetly.BuildConfig
import com.example.closetly.R
import com.example.closetly.model.ClothesModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import kotlin.math.roundToInt

data class GroqRequest(
    val model: String = "llama-3.1-8b-instant",
    val messages: List<GroqMessage>,
    val max_tokens: Int = 200,
    val temperature: Double = 0.7
)

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqResponse(
    val choices: List<GroqChoice>?
)

data class GroqChoice(
    val message: GroqMessageResponse?
)

data class GroqMessageResponse(
    val content: String?
)

interface GroqService {
    @POST("openai/v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GroqRequest
    ): GroqResponse
}

object OutfitRecommendationHelper {

    private val GROQ_API_KEY = BuildConfig.AI_API
    private const val TAG = "OutfitAI"
    
    private val groqApi by lazy {
        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
            
        Retrofit.Builder()
            .baseUrl("https://api.groq.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqService::class.java)
    }
    
    private suspend fun callGroqAI(prompt: String): String {
        return try {
            val request = GroqRequest(
                model = "llama-3.1-8b-instant",
                messages = listOf(
                    GroqMessage(role = "user", content = prompt)
                ),
                max_tokens = 200,
                temperature = 0.7
            )
            
            val response = groqApi.createChatCompletion(
                authorization = "Bearer $GROQ_API_KEY",
                request = request
            )
            
            val result = response.choices?.firstOrNull()?.message?.content?.trim() 
                ?: "AI generated recommendation"
            result
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            throw e
        } catch (e: Exception) {
            throw e
        }
    }
    
    fun getRecommendedOutfits(
        temperature: Double,
        condition: String,
        allClothes: List<ClothesModel>
    ): List<ClothesModel> {
        val recommendedSeason = getSeasonByTemperature(temperature)
        val recommendedCategories = getCategoriesByWeather(temperature, condition)
        
        val filtered = allClothes.filter { clothes ->
            val matchesSeason = clothes.season.equals(recommendedSeason, ignoreCase = true) ||
                    clothes.season.isEmpty()
            val matchesCategory = recommendedCategories.any { category ->
                clothes.categoryName.contains(category, ignoreCase = true)
            }
            matchesSeason || matchesCategory
        }
        
        if (filtered.isNotEmpty()) {
            return filtered.take(6)
        }
        
        return allClothes.filter { clothes ->
            recommendedCategories.any { category ->
                clothes.categoryName.contains(category, ignoreCase = true)
            }
        }.take(6)
    }
    
    fun getSeasonByTemperature(temp: Double): String {
        return when {
            temp < 10 -> "Winter"
            temp < 20 -> "Spring"
            temp < 28 -> "Summer"
            else -> "Summer"
        }
    }

    fun getWeatherIcon(condition: String, icon: String): Int {
        return when {
            condition.contains("clear", ignoreCase = true) && icon.contains("d") -> R.drawable.sunny
            condition.contains("clear", ignoreCase = true) && icon.contains("n") -> R.drawable.clear_night
            condition.contains("cloud", ignoreCase = true) && condition.contains("few", ignoreCase = true) -> R.drawable.cloudy
            condition.contains("cloud", ignoreCase = true) -> R.drawable.cloudy
            condition.contains("rain", ignoreCase = true) && condition.contains("light", ignoreCase = true) -> R.drawable.rainy
            condition.contains("rain", ignoreCase = true) -> R.drawable.rainy
            condition.contains("thunder", ignoreCase = true) || condition.contains("storm", ignoreCase = true) -> R.drawable.stormy
            condition.contains("snow", ignoreCase = true) -> R.drawable.snowy
            condition.contains("mist", ignoreCase = true) || condition.contains("fog", ignoreCase = true) -> R.drawable.foggy
            else -> R.drawable.partly_cloudy
        }
    }

    fun getCategoriesByWeather(temp: Double, condition: String): List<String> {
        val categories = mutableListOf<String>()
        
        when {
            temp < 10 -> {
                categories.addAll(listOf("Jacket", "Sweater", "Coat", "Boots", "Outerwear"))
            }
            temp < 20 -> {
                categories.addAll(listOf("Tops", "Jeans", "Pants", "Shoes", "Shirt"))
            }
            else -> {
                categories.addAll(listOf("Tops", "Shorts", "Dress", "Sandals", "T-shirt"))
            }
        }
        
        if (condition.contains("rain", ignoreCase = true) ||
            condition.contains("drizzle", ignoreCase = true)) {
            categories.addAll(listOf("Jacket", "Boots", "Coat"))
        }
        
        return categories.distinct()
    }

    fun formatTemperature(temp: Double): String {
        return "${temp.roundToInt()}°C"
    }
    
    fun formatTemperatureFahrenheit(temp: Double): String {
        val fahrenheit = (temp * 9/5) + 32
        return "${fahrenheit.roundToInt()}°F"
    }
    
    suspend fun getAIRecommendations(
        temperature: Double,
        weatherCondition: String,
        allClothes: List<ClothesModel>
    ): String = withContext(Dispatchers.IO) {
        val colorCount = allClothes.groupingBy { it.color }.eachCount()
        val topColors = colorCount.entries.sortedByDescending { it.value }.take(3)
        val colorSummary = topColors.joinToString(", ") { "${it.key}(${it.value})" }
        
        val weatherCategory = when {
            temperature < 10 -> "cold (layering needed)"
            temperature < 20 -> "cool (light jacket weather)"
            temperature < 28 -> "warm (comfortable)"
            else -> "hot (breathable fabrics)"
        }
        
        try {
            val prompt = """
                You're a fashion stylist. Be brief (2-3 sentences).
                
                Wardrobe colors: $colorSummary
                Weather: ${temperature}°C, $weatherCondition ($weatherCategory)
                
                Give: 1) Color suggestion (what's missing), 2) Style tip for weather. Add one emoji.
            """.trimIndent()
            
            return@withContext callGroqAI(prompt)
        } catch (e: Exception) {
            val dominantColor = topColors.firstOrNull()?.key ?: "neutral"
            "Try adding warmer tones to complement your $dominantColor pieces! Perfect for ${temperature}°C weather."
        }
    }
    
    data class AIOutfitSelection(
        val selectedClothes: List<ClothesModel>,
        val explanation: String
    )
    
    suspend fun getAISelectedOutfits(
        temperature: Double,
        weatherCondition: String,
        allClothes: List<ClothesModel>
    ): AIOutfitSelection = withContext(Dispatchers.IO) {
        val season = getSeasonByTemperature(temperature)
        val categories = getCategoriesByWeather(temperature, weatherCondition)
        
        val scoredClothes = allClothes.map { clothes ->
            var score = 0
            
            if (clothes.season.equals(season, ignoreCase = true)) score += 10
            if (clothes.season.isEmpty()) score += 5
            
            if (categories.any { clothes.categoryName.contains(it, ignoreCase = true) }) score += 8
            
            if (weatherCondition.contains("rain", ignoreCase = true)) {
                if (clothes.categoryName.contains("jacket", ignoreCase = true) || 
                    clothes.categoryName.contains("boots", ignoreCase = true)) score += 5
            }
            
            val colorCount = allClothes.count { it.color == clothes.color }
            if (colorCount < 5) score += 3
            
            Pair(clothes, score)
        }.sortedByDescending { it.second }
        
        val selected = mutableListOf<ClothesModel>()
        val usedCategories = mutableSetOf<String>()
        
        for ((clothes, _) in scoredClothes) {
            if (selected.size >= 6) break
            if (usedCategories.count { it == clothes.categoryName } < 2) {
                selected.add(clothes)
                usedCategories.add(clothes.categoryName)
            }
        }
        
        if (selected.size < 6) {
            selected.addAll(scoredClothes.map { it.first }.filter { it !in selected }.take(6 - selected.size))
        }
        
        val explanation = try {
            val clothesList = selected.take(4).joinToString(", ") { it.clothesName }
            val prompt = """
                Briefly explain (1 sentence) why these clothes work for ${temperature}°C, $weatherCondition:
                $clothesList
                Add one emoji.
            """.trimIndent()
            
            callGroqAI(prompt)
        } catch (e: Exception) {
            val tempDesc = when {
                temperature < 10 -> "cold"
                temperature < 20 -> "cool"
                temperature < 28 -> "warm"
                else -> "hot"
            }
            "Perfect combo for ${tempDesc} ${temperature.roundToInt()}°C weather!"
        }
        
        AIOutfitSelection(
            selected.ifEmpty { allClothes.take(6) },
            explanation
        )
    }
}

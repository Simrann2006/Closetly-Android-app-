package com.example.closetly.utils

import com.example.closetly.AIRecommendationCache
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
    var aiCache: AIRecommendationCache? = null

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
        selectedClothes: List<ClothesModel>
    ): String = withContext(Dispatchers.IO) {
        if (selectedClothes.isEmpty()) {
            return@withContext "Add some clothes to your closet for outfit recommendations!"
        }
        
        val clothesList = selectedClothes.take(6).joinToString(", ") { it.clothesName }
        
        val weatherCategory = when {
            temperature < 10 -> "cold (layering needed)"
            temperature < 20 -> "cool (light jacket weather)"
            temperature < 28 -> "warm (comfortable)"
            else -> "hot (breathable fabrics)"
        }
        
        try {
            val prompt = """
                You're a fashion stylist. Create a brief outfit recommendation (2-3 sentences).
                
                For ${temperature}°C, $weatherCondition ($weatherCategory) weather, recommend an outfit using THESE specific clothes:
                $clothesList
                
                Explain how to combine them and why they work for this weather. Mention the clothes by name. Add one emoji.
            """.trimIndent()
            
            return@withContext callGroqAI(prompt)
        } catch (e: Exception) {
            "Layer $clothesList for the perfect ${temperature.roundToInt()}°C outfit! 👔"
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
        val scoredClothes = allClothes.map { clothes ->
            var score = 0
            
            if (clothes.season.equals(season, ignoreCase = true)) score += 10
            if (clothes.season.isEmpty()) score += 5

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
            val clothesList = selected.take(4).joinToString(", ") { "${it.clothesName} (${it.categoryName})" }
            val prompt = """
                Briefly explain (1 sentence) why these specific clothes from the user's closet work well together for ${temperature}°C, $weatherCondition:
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
            "Perfect combo from your closet for ${tempDesc} ${temperature.roundToInt()}°C weather!"
        }
        
        AIOutfitSelection(
            selected.ifEmpty { allClothes.take(6) },
            explanation
        )
    }
}

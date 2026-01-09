package com.example.closetly.repository

import com.example.closetly.BuildConfig
import com.example.closetly.model.WeatherData
import com.example.closetly.model.WeatherResponse
import com.example.closetly.model.ForecastResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class WeatherRepoImpl {
    
    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
        private val API_KEY = BuildConfig.WEATHER_API
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val weatherApi = retrofit.create(WeatherApiService::class.java)
    
    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
        callback: (Boolean, String, WeatherData?) -> Unit
    ) {
        try {
            val response = weatherApi.getCurrentWeather(latitude, longitude, API_KEY)
            
            if (response.isSuccessful && response.body() != null) {
                val weatherResponse = response.body()!!
                val weatherData = mapToWeatherData(weatherResponse)
                callback(true, "Weather fetched successfully", weatherData)
            } else {
                callback(false, "Failed to fetch weather: ${response.message()}", null)
            }
        } catch (e: Exception) {
            callback(false, "Error: ${e.message}", null)
        }
    }
    
    suspend fun getCurrentWeatherByCity(
        cityName: String,
        callback: (Boolean, String, WeatherData?) -> Unit
    ) {
        try {
            val response = weatherApi.getCurrentWeatherByCity(cityName, API_KEY)
            
            if (response.isSuccessful && response.body() != null) {
                val weatherResponse = response.body()!!
                val weatherData = mapToWeatherData(weatherResponse)
                callback(true, "Weather fetched successfully", weatherData)
            } else {
                callback(false, "Failed to fetch weather: ${response.message()}", null)
            }
        } catch (e: Exception) {
            callback(false, "Error: ${e.message}", null)
        }
    }
    
    suspend fun getForecast(
        latitude: Double,
        longitude: Double,
        callback: (Boolean, String, ForecastResponse?) -> Unit
    ) {
        try {
            val response = weatherApi.getForecast(latitude, longitude, API_KEY)
            
            if (response.isSuccessful && response.body() != null) {
                callback(true, "Forecast fetched successfully", response.body())
            } else {
                callback(false, "Failed to fetch forecast: ${response.message()}", null)
            }
        } catch (e: Exception) {
            callback(false, "Error: ${e.message}", null)
        }
    }
    
    private fun mapToWeatherData(response: WeatherResponse): WeatherData {
        return WeatherData(
            temperature = response.main?.temp ?: 0.0,
            feelsLike = response.main?.feelsLike ?: 0.0,
            condition = response.weather?.firstOrNull()?.main ?: "Unknown",
            description = response.weather?.firstOrNull()?.description ?: "No description",
            humidity = response.main?.humidity ?: 0,
            windSpeed = response.wind?.speed ?: 0.0,
            cityName = response.name ?: "Unknown",
            weatherIcon = response.weather?.firstOrNull()?.icon ?: "01d",
            timestamp = response.dt ?: System.currentTimeMillis() / 1000
        )
    }
}

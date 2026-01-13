package com.example.closetly.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("coord") val coord: Coord?,
    @SerializedName("weather") val weather: List<Weather>?,
    @SerializedName("base") val base: String?,
    @SerializedName("main") val main: Main?,
    @SerializedName("visibility") val visibility: Int?,
    @SerializedName("wind") val wind: Wind?,
    @SerializedName("clouds") val clouds: Clouds?,
    @SerializedName("dt") val dt: Long?,
    @SerializedName("sys") val sys: Sys?,
    @SerializedName("timezone") val timezone: Int?,
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("cod") val cod: Int?
)

data class Coord(
    @SerializedName("lon") val lon: Double?,
    @SerializedName("lat") val lat: Double?
)

data class Weather(
    @SerializedName("id") val id: Int?,
    @SerializedName("main") val main: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("icon") val icon: String?
)

data class Main(
    @SerializedName("temp") val temp: Double?,
    @SerializedName("feels_like") val feelsLike: Double?,
    @SerializedName("temp_min") val tempMin: Double?,
    @SerializedName("temp_max") val tempMax: Double?,
    @SerializedName("pressure") val pressure: Int?,
    @SerializedName("humidity") val humidity: Int?
)

data class Wind(
    @SerializedName("speed") val speed: Double?,
    @SerializedName("deg") val deg: Int?
)

data class Clouds(
    @SerializedName("all") val all: Int?
)

data class Sys(
    @SerializedName("type") val type: Int?,
    @SerializedName("id") val id: Int?,
    @SerializedName("country") val country: String?,
    @SerializedName("sunrise") val sunrise: Long?,
    @SerializedName("sunset") val sunset: Long?
)

data class ForecastResponse(
    @SerializedName("cod") val cod: String?,
    @SerializedName("message") val message: Int?,
    @SerializedName("cnt") val cnt: Int?,
    @SerializedName("list") val list: List<ForecastItem>?,
    @SerializedName("city") val city: City?
)

data class ForecastItem(
    @SerializedName("dt") val dt: Long?,
    @SerializedName("main") val main: Main?,
    @SerializedName("weather") val weather: List<Weather>?,
    @SerializedName("clouds") val clouds: Clouds?,
    @SerializedName("wind") val wind: Wind?,
    @SerializedName("visibility") val visibility: Int?,
    @SerializedName("pop") val pop: Double?,
    @SerializedName("dt_txt") val dtTxt: String?
)

data class City(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("coord") val coord: Coord?,
    @SerializedName("country") val country: String?,
    @SerializedName("population") val population: Int?,
    @SerializedName("timezone") val timezone: Int?,
    @SerializedName("sunrise") val sunrise: Long?,
    @SerializedName("sunset") val sunset: Long?
)

data class WeatherData(
    val temperature: Double,
    val feelsLike: Double,
    val condition: String,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val cityName: String,
    val weatherIcon: String,
    val timestamp: Long
)

package com.example.closetly.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.closetly.model.WeatherData
import com.example.closetly.repository.WeatherRepoImpl
import kotlinx.coroutines.launch

class WeatherViewModel(private val repo: WeatherRepoImpl) : ViewModel() {
    
    private val _weatherData = MutableLiveData<WeatherData?>()
    val weatherData: MutableLiveData<WeatherData?> get() = _weatherData
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: MutableLiveData<String?> get() = _error
    
    fun fetchWeatherByLocation(latitude: Double, longitude: Double) {
        _loading.postValue(true)
        _error.postValue(null)
        
        viewModelScope.launch {
            repo.getCurrentWeather(latitude, longitude) { success, message, data ->
                _loading.postValue(false)
                if (success && data != null) {
                    _weatherData.postValue(data)
                } else {
                    _error.postValue(message)
                }
            }
        }
    }
    
    fun fetchWeatherByCity(cityName: String) {
        _loading.postValue(true)
        _error.postValue(null)
        
        viewModelScope.launch {
            repo.getCurrentWeatherByCity(cityName) { success, message, data ->
                _loading.postValue(false)
                if (success && data != null) {
                    _weatherData.postValue(data)
                } else {
                    _error.postValue(message)
                }
            }
        }
    }
    
    fun refreshWeather(latitude: Double, longitude: Double) {
        fetchWeatherByLocation(latitude, longitude)
    }
}

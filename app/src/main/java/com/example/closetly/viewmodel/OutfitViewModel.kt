package com.example.closetly.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.closetly.model.OutfitModel
import com.example.closetly.repository.OutfitRepo

class OutfitViewModel(private val repo: OutfitRepo) : ViewModel() {

    private val _outfit = MutableLiveData<OutfitModel?>()
    val outfit: MutableLiveData<OutfitModel?> get() = _outfit

    private val _allOutfits = MutableLiveData<List<OutfitModel>?>()
    val allOutfits: MutableLiveData<List<OutfitModel>?> get() = _allOutfits

    private val _outfitsByDate = MutableLiveData<List<OutfitModel>?>()
    val outfitsByDate: MutableLiveData<List<OutfitModel>?> get() = _outfitsByDate

    private val _favoriteOutfits = MutableLiveData<List<OutfitModel>?>()
    val favoriteOutfits: MutableLiveData<List<OutfitModel>?> get() = _favoriteOutfits

    private val _loading = MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading

    fun addOutfit(model: OutfitModel, callback: (Boolean, String) -> Unit) {
        repo.addOutfit(model, callback)
    }

    fun editOutfit(model: OutfitModel, callback: (Boolean, String) -> Unit) {
        repo.editOutfit(model, callback)
    }

    fun deleteOutfit(outfitId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteOutfit(outfitId, callback)
    }

    fun getOutfitById(outfitId: String, callback: (Boolean, String, OutfitModel?) -> Unit) {
        repo.getOutfitById(outfitId) { success, msg, data ->
            if (success) {
                _outfit.postValue(data)
            }
            callback(success, msg, data)
        }
    }

    fun getAllOutfits(callback: (Boolean, String, List<OutfitModel>?) -> Unit) {
        _loading.postValue(true)
        repo.getAllOutfits { success, msg, data ->
            _loading.postValue(false)
            if (success) {
                _allOutfits.postValue(data)
            }
            callback(success, msg, data)
        }
    }

    fun getOutfitsByDate(date: String, callback: (Boolean, String, List<OutfitModel>?) -> Unit) {
        repo.getOutfitsByDate(date) { success, msg, data ->
            if (success) {
                _outfitsByDate.postValue(data)
            }
            callback(success, msg, data)
        }
    }

    fun getOutfitsByDateRange(
        startDate: String,
        endDate: String,
        callback: (Boolean, String, List<OutfitModel>?) -> Unit
    ) {
        repo.getOutfitsByDateRange(startDate, endDate, callback)
    }

    fun getFavoriteOutfits(callback: (Boolean, String, List<OutfitModel>?) -> Unit) {
        repo.getFavoriteOutfits { success, msg, data ->
            if (success) {
                _favoriteOutfits.postValue(data)
            }
            callback(success, msg, data)
        }
    }

    fun markAsWorn(outfitId: String, wornDate: String, callback: (Boolean, String) -> Unit) {
        repo.markAsWorn(outfitId, wornDate, callback)
    }

    fun toggleFavorite(outfit: OutfitModel, callback: (Boolean, String) -> Unit) {
        val updatedOutfit = outfit.copy(
            isFavorite = !outfit.isFavorite,
            updatedAt = System.currentTimeMillis()
        )
        editOutfit(updatedOutfit, callback)
    }
}

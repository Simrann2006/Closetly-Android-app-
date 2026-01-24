package com.example.closetly.repository

import com.example.closetly.model.OutfitModel

interface OutfitRepo {
    fun addOutfit(model: OutfitModel, callback: (Boolean, String) -> Unit)
    
    fun editOutfit(model: OutfitModel, callback: (Boolean, String) -> Unit)
    
    fun deleteOutfit(outfitId: String, callback: (Boolean, String) -> Unit)
    
    fun getOutfitById(outfitId: String, callback: (Boolean, String, OutfitModel?) -> Unit)
    
    fun getAllOutfits(callback: (Boolean, String, List<OutfitModel>?) -> Unit)
    
    fun getOutfitsByDate(date: String, callback: (Boolean, String, List<OutfitModel>?) -> Unit)
    
    fun getOutfitsByDateRange(startDate: String, endDate: String, callback: (Boolean, String, List<OutfitModel>?) -> Unit)
    
    fun getFavoriteOutfits(callback: (Boolean, String, List<OutfitModel>?) -> Unit)
    
    fun markAsWorn(outfitId: String, wornDate: String, callback: (Boolean, String) -> Unit)
}

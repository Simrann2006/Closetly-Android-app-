package com.example.closetly.repository

import com.example.closetly.model.ClothesModel

interface ClothesRepo {
    fun addClothes(model: ClothesModel, callback: (Boolean, String) -> Unit
    )

    fun editClothes(model: ClothesModel, callback: (Boolean, String) -> Unit
    )

    fun deleteClothes(clothesId: String, callback: (Boolean, String) -> Unit)

    fun getClothesById(clothesId: String,
                       callback: (Boolean, String, ClothesModel?) -> Unit)

    fun getAllClothes(callback: (Boolean, String, List<ClothesModel>?) -> Unit)

    fun getClothesByCategory(categoryId:String,callback: (Boolean, String, List<ClothesModel>?) -> Unit)
}
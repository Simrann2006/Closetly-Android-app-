package com.example.closetly.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.closetly.model.ClothesModel
import com.example.closetly.repository.ClothesRepo

class ClothesViewModel (val repo : ClothesRepo) : ViewModel() {
    fun addClothes(model: ClothesModel, callback: (Boolean, String) -> Unit
    ){
        repo.addClothes(model, callback)
    }

    fun editClothes(model: ClothesModel, callback: (Boolean, String) -> Unit
    ){
        repo.editClothes(model,callback)
    }

    fun deleteClothes(clothesId: String, callback: (Boolean, String) -> Unit){
        repo.deleteClothes(clothesId,callback)
    }

    private val _clothes = MutableLiveData<ClothesModel?>()
    val clothes : MutableLiveData<ClothesModel?> get() = _clothes

    private val _allClothes = MutableLiveData<List<ClothesModel>?>()
    val allClothes : MutableLiveData<List<ClothesModel>?> get() = _allClothes

    private val _loading = MutableLiveData<Boolean>()
    val loading : MutableLiveData<Boolean> get() = _loading

    fun getClothesById(clothesId: String,
                       callback: (Boolean, String, ClothesModel?) -> Unit){
        repo.getClothesById(clothesId){
                success,msg,data->
            if(success){
                _clothes.postValue(data)
            }
        }
    }

    fun getAllClothes(callback: (Boolean, String, List<ClothesModel>?) -> Unit){
        _loading.postValue(true)
        repo.getAllClothes{
                success,msg,data->
            if(success){
                _loading.postValue(false)
                _allClothes.postValue(data)
            }
        }
    }

    private val _allClothesCategory = MutableLiveData<List<ClothesModel>?>()
    val allClothesCategory : MutableLiveData<List<ClothesModel>?> get() = _allClothesCategory

    fun getClothesByCategory(categoryId:String,callback: (Boolean, String, List<ClothesModel>?) -> Unit){
        repo.getClothesByCategory(categoryId){
                success,msg,data->
            if(success){
                _allClothesCategory.postValue(data)
            }
        }
    }
}
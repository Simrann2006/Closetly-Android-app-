package com.example.closetly.repository

import com.example.closetly.model.ClothesModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.toMap

class ClothesRepoImpl : ClothesRepo {

    val database : FirebaseDatabase = FirebaseDatabase.getInstance()

    val ref : DatabaseReference = database.getReference("Clothess")

    override fun addClothes(
        model: ClothesModel,
        callback: (Boolean, String) -> Unit
    ) {
        val id = ref.push().key.toString()
        model.clothesId = id
        ref.child(id).setValue(model).addOnCompleteListener {
            if(it.isSuccessful){
                callback(true,"Clothes added successfully")
            }else{
                callback(false,"${it.exception?.message}")
            }
        }
    }

    override fun editClothes(
        model: ClothesModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(model.clothesId).updateChildren(model.toMap()).addOnCompleteListener {
            if (it.isSuccessful){
                callback(true, "Updated successfully")
            } else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun deleteClothes(
        clothesId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(clothesId).removeValue().addOnCompleteListener {
            if (it.isSuccessful){
                callback(true, "Deleted successfully")
            } else{
                callback(false, "${it.exception?.message}")
            }
        }
    }

    override fun getClothesById(
        clothesId: String,
        callback: (Boolean, String, ClothesModel?) -> Unit
    ) {
        ref.child(clothesId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val clothes = snapshot.getValue(ClothesModel::class.java)
                    if(clothes != null){
                        callback(true,"Clothes fetched", clothes)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false,error.message,null)
            }
        })
    }

    override fun getAllClothes(callback: (Boolean, String, List<ClothesModel>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var allClothes = mutableListOf<ClothesModel>()
                    for(data in snapshot.children){
                        var clothes = data.getValue(ClothesModel::class.java)
                        if(clothes != null){
                            allClothes.add(clothes)
                        }
                    }

                    callback(true,"Clothes fetched",allClothes)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false,error.message,null)
            }
        })
    }

    override fun getClothesByCategory(
        categoryId: String,
        callback: (Boolean, String, List<ClothesModel>?) -> Unit
    ) {
        ref.orderByChild("categoryId").equalTo(categoryId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var allClothes = mutableListOf<ClothesModel>()
                    for(data in snapshot.children){
                        var clothes = data.getValue(ClothesModel::class.java)
                        if(clothes != null){
                            allClothes.add(clothes)
                        }
                    }

                    callback(true,"Clothes fetched",allClothes)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false,error.message,emptyList())
            }

        })
    }
}
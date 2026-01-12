package com.example.closetly.viewmodel

import androidx.lifecycle.ViewModel
import com.example.closetly.model.ProductModel
import com.example.closetly.repository.ProductRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProductViewModel(val repo: ProductRepo) : ViewModel() {
    val _products = MutableStateFlow<List<ProductModel>>(emptyList())
    val products: StateFlow<List<ProductModel>> = _products

    fun loadProducts() {
        repo.getAllProducts { success, _, data ->
            if (success && data != null) {
                _products.value = data
            }
        }
    }

    fun addProduct(product: ProductModel, onResult: (Boolean, String) -> Unit) {
        repo.addProduct(product) { success, msg ->
            if (success) loadProducts()
            onResult(success, msg)
        }
    }

    fun editProduct(product: ProductModel, onResult: (Boolean, String) -> Unit) {
        repo.editProduct(product) { success, msg ->
            if (success) loadProducts()
            onResult(success, msg)
        }
    }

    fun deleteProduct(productId: String, onResult: (Boolean, String) -> Unit) {
        repo.deleteProduct(productId) { success, msg ->
            if (success) loadProducts()
            onResult(success, msg)
        }
    }

    fun getProductById(productId: String, onResult: (Boolean, String, ProductModel?) -> Unit) {
        repo.getProductById(productId) { success, msg, data ->
            onResult(success, msg, data)
        }
    }

    fun getUserProducts(userId: String, onResult: (List<ProductModel>) -> Unit) {
        repo.getUserProducts(userId) { success, _, data ->
            onResult(data ?: emptyList())
        }
    }
}
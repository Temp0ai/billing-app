package com.billing.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billing.app.data.entity.Product
import com.billing.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    val allProducts: StateFlow<List<Product>> = productRepository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = productRepository.getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = productRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStockValue: StateFlow<Double> = productRepository.getTotalStockValue()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    fun searchProducts(query: String) {
        _searchQuery.value = query
    }

    fun filterByCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun getFilteredProducts(): Flow<List<Product>> {
        return combine(allProducts, _searchQuery, _selectedCategory) { products, query, category ->
            products.filter { product ->
                val matchesQuery = query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.hsnCode.contains(query, ignoreCase = true) ||
                    product.barcode.contains(query, ignoreCase = true)
                val matchesCategory = category == null || product.category == category
                matchesQuery && matchesCategory
            }
        }
    }

    fun addProduct(product: Product, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                productRepository.insertProduct(product)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            productRepository.updateProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
        }
    }

    fun updateStock(productId: Long, qty: Double) {
        viewModelScope.launch {
            productRepository.updateStock(productId, qty)
        }
    }
}

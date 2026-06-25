package com.billing.app.data.repository

import com.billing.app.data.dao.ProductDao
import com.billing.app.data.entity.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)

    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)

    fun getProductsByCategory(category: String): Flow<List<Product>> = productDao.getProductsByCategory(category)

    fun getLowStockProducts(): Flow<List<Product>> = productDao.getLowStockProducts()

    fun getAllCategories(): Flow<List<String>> = productDao.getAllCategories()

    fun getTotalStockValue(): Flow<Double?> = productDao.getTotalStockValue()

    fun getTotalRetailStockValue(): Flow<Double?> = productDao.getTotalRetailStockValue()

    fun getProductCount(): Flow<Int> = productDao.getProductCount()

    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)

    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    suspend fun updateStock(productId: Long, qty: Double) = productDao.updateStock(productId, qty)
}

package com.billing.app.data.dao

import androidx.room.*
import com.billing.app.data.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR hsnCode LIKE '%' || :query || '%' OR barcode = :query")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category AND isActive = 1")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE stockQuantity <= lowStockAlert AND isActive = 1")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT DISTINCT category FROM products WHERE category != '' ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT SUM(stockQuantity * purchasePrice) FROM products WHERE isActive = 1")
    fun getTotalStockValue(): Flow<Double?>

    @Query("SELECT SUM(salePrice * stockQuantity) FROM products WHERE isActive = 1")
    fun getTotalRetailStockValue(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET stockQuantity = stockQuantity + :qty WHERE id = :productId")
    suspend fun updateStock(productId: Long, qty: Double)

    @Query("SELECT * FROM products WHERE isSynced = 0")
    suspend fun getUnsyncedProducts(): List<Product>

    @Query("UPDATE products SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    fun getProductCount(): Flow<Int>
}

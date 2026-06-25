package com.billing.app.data.dao

import androidx.room.*
import com.billing.app.data.entity.InvoiceItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceItemDao {

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    fun getItemsByInvoice(invoiceId: Long): Flow<List<InvoiceItem>>

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsByInvoiceSync(invoiceId: Long): List<InvoiceItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InvoiceItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InvoiceItem>)

    @Update
    suspend fun updateItem(item: InvoiceItem)

    @Delete
    suspend fun deleteItem(item: InvoiceItem)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsByInvoice(invoiceId: Long)

    @Query("""
        SELECT productName, SUM(quantity) as totalQty, SUM(totalAmount) as totalAmount
        FROM invoice_items
        WHERE invoiceId IN (SELECT id FROM invoices WHERE invoiceDate BETWEEN :startDate AND :endDate)
        GROUP BY productName
        ORDER BY totalAmount DESC
    """)
    fun getTopSellingProducts(startDate: Long, endDate: Long): Flow<List<ProductSalesSummary>>
}

data class ProductSalesSummary(
    val productName: String,
    val totalQty: Double,
    val totalAmount: Double
)

package com.billing.app.data.dao

import androidx.room.*
import com.billing.app.data.entity.Invoice
import com.billing.app.data.entity.InvoiceStatus
import com.billing.app.data.entity.InvoiceType
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE invoiceType = :type ORDER BY createdAt DESC")
    fun getInvoicesByType(type: InvoiceType): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE status = :status ORDER BY createdAt DESC")
    fun getInvoicesByStatus(status: InvoiceStatus): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE partyId = :partyId ORDER BY createdAt DESC")
    fun getInvoicesByParty(partyId: Long): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: Long): Invoice?

    @Query("SELECT * FROM invoices WHERE invoiceNumber = :number LIMIT 1")
    suspend fun getInvoiceByNumber(number: String): Invoice?

    @Query("SELECT * FROM invoices WHERE invoiceDate BETWEEN :startDate AND :endDate ORDER BY invoiceDate DESC")
    fun getInvoicesByDateRange(startDate: Long, endDate: Long): Flow<List<Invoice>>

    @Query("SELECT SUM(grandTotal) FROM invoices WHERE invoiceDate BETWEEN :startDate AND :endDate AND invoiceType = 'TAX_INVOICE'")
    fun getTotalSalesInRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(grandTotal) FROM invoices WHERE invoiceType = 'TAX_INVOICE'")
    fun getTotalSales(): Flow<Double?>

    @Query("SELECT SUM(balanceAmount) FROM invoices WHERE status IN ('UNPAID', 'PARTIALLY_PAID', 'OVERDUE')")
    fun getTotalOutstanding(): Flow<Double?>

    @Query("SELECT SUM(totalCgst + totalSgst + totalIgst) FROM invoices WHERE invoiceDate BETWEEN :startDate AND :endDate AND invoiceType = 'TAX_INVOICE'")
    fun getTotalTaxInRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("""
        SELECT * FROM invoices
        WHERE status IN ('UNPAID', 'PARTIALLY_PAID')
        AND dueDate < :currentTime
    """)
    fun getOverdueInvoices(currentTime: Long = System.currentTimeMillis()): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE invoiceDate >= :todayStart ORDER BY createdAt DESC")
    fun getTodayInvoices(todayStart: Long): Flow<List<Invoice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Query("UPDATE invoices SET status = :status, amountPaid = :amountPaid, balanceAmount = :balance WHERE id = :invoiceId")
    suspend fun updatePaymentStatus(invoiceId: Long, status: InvoiceStatus, amountPaid: Double, balance: Double)

    @Query("SELECT * FROM invoices WHERE isSynced = 0")
    suspend fun getUnsyncedInvoices(): List<Invoice>

    @Query("UPDATE invoices SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM invoices")
    fun getInvoiceCount(): Flow<Int>
}

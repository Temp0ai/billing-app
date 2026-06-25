package com.billing.app.data.dao

import androidx.room.*
import com.billing.app.data.entity.Payment
import com.billing.app.data.entity.PaymentMode
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE invoiceId = :invoiceId ORDER BY paymentDate DESC")
    fun getPaymentsByInvoice(invoiceId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE partyId = :partyId ORDER BY paymentDate DESC")
    fun getPaymentsByParty(partyId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE paymentDate BETWEEN :startDate AND :endDate ORDER BY paymentDate DESC")
    fun getPaymentsByDateRange(startDate: Long, endDate: Long): Flow<List<Payment>>

    @Query("SELECT SUM(amount) FROM payments WHERE paymentDate BETWEEN :startDate AND :endDate")
    fun getTotalPaymentsInRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM payments WHERE paymentMode = :mode AND paymentDate BETWEEN :startDate AND :endDate")
    fun getTotalByPaymentMode(mode: PaymentMode, startDate: Long, endDate: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("SELECT * FROM payments WHERE isSynced = 0")
    suspend fun getUnsyncedPayments(): List<Payment>

    @Query("UPDATE payments SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)
}

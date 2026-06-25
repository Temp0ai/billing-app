package com.billing.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val description: String = "",
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val referenceNumber: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

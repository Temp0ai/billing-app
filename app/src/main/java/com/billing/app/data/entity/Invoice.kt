package com.billing.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val invoiceType: InvoiceType = InvoiceType.TAX_INVOICE,
    val partyId: Long,
    val partyName: String,
    val partyGstin: String = "",
    val partyAddress: String = "",
    val partyState: String = "",
    val invoiceDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L), // 30 days
    val subtotal: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val totalCgst: Double = 0.0,
    val totalSgst: Double = 0.0,
    val totalIgst: Double = 0.0,
    val totalCess: Double = 0.0,
    val totalTax: Double = 0.0,
    val roundOff: Double = 0.0,
    val grandTotal: Double = 0.0,
    val amountPaid: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val status: InvoiceStatus = InvoiceStatus.UNPAID,
    val notes: String = "",
    val terms: String = "",
    val placeOfSupply: String = "",
    val reverseCharge: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

enum class InvoiceType {
    TAX_INVOICE,
    PROFORMA,
    QUOTATION,
    DELIVERY_CHALLAN,
    CREDIT_NOTE,
    DEBIT_NOTE
}

enum class InvoiceStatus {
    DRAFT,
    UNPAID,
    PARTIALLY_PAID,
    PAID,
    OVERDUE,
    CANCELLED
}

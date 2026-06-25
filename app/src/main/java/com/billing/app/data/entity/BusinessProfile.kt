package com.billing.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey
    val id: Int = 1,
    val businessName: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val gstin: String = "",
    val pan: String = "",
    val bankName: String = "",
    val bankAccount: String = "",
    val bankIfsc: String = "",
    val bankBranch: String = "",
    val invoicePrefix: String = "INV",
    val invoiceStartNumber: Int = 1,
    val currentInvoiceNumber: Int = 1,
    val quotationPrefix: String = "QUO",
    val challanPrefix: String = "DC",
    val signaturePath: String = "",
    val logoPath: String = "",
    val termsAndConditions: String = "1. Payment due within 30 days.\n2. Goods once sold will not be returned.\n3. Subject to local jurisdiction.",
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

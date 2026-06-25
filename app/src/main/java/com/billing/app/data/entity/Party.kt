package com.billing.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parties")
data class Party(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val gstin: String = "",
    val pan: String = "",
    val partyType: PartyType = PartyType.CUSTOMER,
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

enum class PartyType {
    CUSTOMER,
    SUPPLIER,
    BOTH
}

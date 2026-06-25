package com.billing.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val hsnCode: String = "",
    val description: String = "",
    val category: String = "",
    val unit: String = "PCS", // PCS, KG, LTR, MTR, BOX, etc.
    val salePrice: Double = 0.0,
    val purchasePrice: Double = 0.0,
    val gstRate: GstRate = GstRate.GST_18,
    val cessRate: Double = 0.0,
    val discount: Double = 0.0,
    val stockQuantity: Double = 0.0,
    val lowStockAlert: Double = 5.0,
    val barcode: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

enum class GstRate(val rate: Double, val label: String) {
    GST_0(0.0, "0%"),
    GST_0_25(0.25, "0.25%"),
    GST_3(3.0, "3%"),
    GST_5(5.0, "5%"),
    GST_12(12.0, "12%"),
    GST_18(18.0, "18%"),
    GST_28(28.0, "28%");

    companion object {
        fun cgst(rate: GstRate) = rate.rate / 2
        fun sgst(rate: GstRate) = rate.rate / 2
        fun igst(rate: GstRate) = rate.rate
    }
}

package com.billing.app.util

import com.billing.app.data.entity.DiscountType
import com.billing.app.data.entity.InvoiceItem
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * GST Tax Calculator for Indian Tax Compliance
 *
 * Supports:
 * - CGST + SGST (Intra-state)
 * - IGST (Inter-state)
 * - Cess
 * - Reverse Charge
 * - HSN-wise tax breakup
 * - Round-off as per GST rules
 */
object GstCalculator {

    data class ItemTaxResult(
        val taxableAmount: Double,
        val cgst: Double,
        val sgst: Double,
        val igst: Double,
        val cess: Double,
        val totalTax: Double,
        val totalAmount: Double
    )

    data class InvoiceTotals(
        val subtotal: Double,
        val totalDiscount: Double,
        val totalCgst: Double,
        val totalSgst: Double,
        val totalIgst: Double,
        val totalCess: Double,
        val totalTax: Double,
        val roundOff: Double,
        val grandTotal: Double
    )

    data class HsnTaxSummary(
        val hsnCode: String,
        val description: String,
        val uom: String,
        val totalQty: Double,
        val totalValue: Double,
        val taxableValue: Double,
        val cgstRate: Double,
        val cgstAmount: Double,
        val sgstRate: Double,
        val sgstAmount: Double,
        val igstRate: Double,
        val igstAmount: Double
    )

    /**
     * Calculate tax for a single invoice item
     */
    fun calculateItemTax(
        quantity: Double,
        rate: Double,
        discount: Double = 0.0,
        discountType: DiscountType = DiscountType.PERCENTAGE,
        gstRate: Double,
        cessRate: Double = 0.0,
        isInterState: Boolean = false
    ): ItemTaxResult {
        val grossAmount = quantity * rate

        val discountAmount = when (discountType) {
            DiscountType.PERCENTAGE -> grossAmount * discount / 100
            DiscountType.FIXED -> discount * quantity
        }

        val taxableAmount = roundToTwo(grossAmount - discountAmount)
        val taxRate = gstRate + cessRate

        val cgst: Double
        val sgst: Double
        val igst: Double

        if (isInterState) {
            // Inter-state: IGST applies
            cgst = 0.0
            sgst = 0.0
            igst = roundToTwo(taxableAmount * gstRate / 100)
        } else {
            // Intra-state: CGST + SGST applies
            cgst = roundToTwo(taxableAmount * (gstRate / 2) / 100)
            sgst = roundToTwo(taxableAmount * (gstRate / 2) / 100)
            igst = 0.0
        }

        val cess = roundToTwo(taxableAmount * cessRate / 100)
        val totalTax = roundToTwo(cgst + sgst + igst + cess)
        val totalAmount = roundToTwo(taxableAmount + totalTax)

        return ItemTaxResult(
            taxableAmount = taxableAmount,
            cgst = cgst,
            sgst = sgst,
            igst = igst,
            cess = cess,
            totalTax = totalTax,
            totalAmount = totalAmount
        )
    }

    /**
     * Calculate invoice totals from all items
     */
    fun calculateInvoiceTotals(items: List<InvoiceItem>, roundOff: Double = 0.0): InvoiceTotals {
        val subtotal = items.sumOf { it.taxableAmount }
        val totalDiscount = items.sumOf {
            val gross = it.quantity * it.rate
            gross - it.taxableAmount
        }
        val totalCgst = items.sumOf { it.cgst }
        val totalSgst = items.sumOf { it.sgst }
        val totalIgst = items.sumOf { it.igst }
        val totalCess = items.sumOf { it.cess }
        val totalTax = totalCgst + totalSgst + totalIgst + totalCess

        val rawTotal = subtotal + totalTax
        val grandTotal = roundToTwo(rawTotal + roundOff)

        return InvoiceTotals(
            subtotal = roundToTwo(subtotal),
            totalDiscount = roundToTwo(totalDiscount),
            totalCgst = roundToTwo(totalCgst),
            totalSgst = roundToTwo(totalSgst),
            totalIgst = roundToTwo(totalIgst),
            totalCess = roundToTwo(totalCess),
            totalTax = roundToTwo(totalTax),
            roundOff = roundOff,
            grandTotal = grandTotal
        )
    }

    /**
     * Calculate round-off amount (nearest rupee as per GST rules)
     */
    fun calculateRoundOff(amount: Double): Double {
        val rounded = Math.round(amount).toDouble()
        return roundToTwo(rounded - amount)
    }

    /**
     * Generate HSN-wise tax summary for GSTR-1 filing
     */
    fun generateHsnSummary(items: List<InvoiceItem>, isInterState: Boolean): List<HsnTaxSummary> {
        return items.groupBy { it.hsnCode }
            .map { (hsnCode, groupItems) ->
                val firstItem = groupItems.first()
                HsnTaxSummary(
                    hsnCode = hsnCode,
                    description = firstItem.productName,
                    uom = firstItem.unit,
                    totalQty = groupItems.sumOf { it.quantity },
                    totalValue = groupItems.sumOf { it.quantity * it.rate },
                    taxableValue = groupItems.sumOf { it.taxableAmount },
                    cgstRate = if (!isInterState) firstItem.gstRate / 2 else 0.0,
                    cgstAmount = groupItems.sumOf { it.cgst },
                    sgstRate = if (!isInterState) firstItem.gstRate / 2 else 0.0,
                    sgstAmount = groupItems.sumOf { it.sgst },
                    igstRate = if (isInterState) firstItem.gstRate else 0.0,
                    igstAmount = groupItems.sumOf { it.igst }
                )
            }
    }

    /**
     * Validate GSTIN format
     * Format: 22AAAAA0000A1Z5
     * - First 2 digits: State code
     * - Next 10 digits: PAN
     * - 13th digit: Entity number
     * - 14th digit: Z (default)
     * - 15th digit: Check digit
     */
    fun isValidGstin(gstin: String): Boolean {
        val regex = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")
        return gstin.length == 17 && regex.matches(gstin.uppercase())
    }

    /**
     * Extract state code from GSTIN
     */
    fun getStateCodeFromGstin(gstin: String): String {
        return if (gstin.length >= 2) gstin.substring(0, 2) else ""
    }

    /**
     * Get state name from state code (Indian GST state codes)
     */
    fun getStateNameFromCode(code: String): String {
        return stateCodeMap[code] ?: "Unknown"
    }

    /**
     * Determine if transaction is inter-state based on supplier and recipient state codes
     */
    fun isInterState(supplierStateCode: String, recipientStateCode: String): Boolean {
        return supplierStateCode != recipientStateCode
    }

    private fun roundToTwo(value: Double): Double {
        return BigDecimal(value).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    // Indian GST State Codes
    val stateCodeMap = mapOf(
        "01" to "Jammu & Kashmir",
        "02" to "Himachal Pradesh",
        "03" to "Punjab",
        "04" to "Chandigarh",
        "05" to "Uttarakhand",
        "06" to "Haryana",
        "07" to "Delhi",
        "08" to "Rajasthan",
        "09" to "Uttar Pradesh",
        "10" to "Bihar",
        "11" to "Sikkim",
        "12" to "Arunachal Pradesh",
        "13" to "Nagaland",
        "14" to "Manipur",
        "15" to "Mizoram",
        "16" to "Tripura",
        "17" to "Meghalaya",
        "18" to "Assam",
        "19" to "West Bengal",
        "20" to "Jharkhand",
        "21" to "Odisha",
        "22" to "Chhattisgarh",
        "23" to "Madhya Pradesh",
        "24" to "Gujarat",
        "26" to "Dadra & Nagar Haveli and Daman & Diu",
        "27" to "Maharashtra",
        "29" to "Karnataka",
        "30" to "Goa",
        "31" to "Lakshadweep",
        "32" to "Kerala",
        "33" to "Tamil Nadu",
        "34" to "Puducherry",
        "35" to "Andaman & Nicobar Islands",
        "36" to "Telangana",
        "37" to "Andhra Pradesh",
        "38" to "Ladakh",
        "97" to "Other Territory"
    )
}

package com.billing.app.util

import java.text.NumberFormat
import java.util.*

object CurrencyUtils {

    private val indianFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun formatIndian(amount: Double): String {
        return indianFormat.format(amount)
    }

    fun formatIndianWithoutSymbol(amount: Double): String {
        return formatIndian(amount).replace("₹", "").trim()
    }

    fun formatIndianShort(amount: Double): String {
        return when {
            amount >= 10000000 -> "₹${formatWithTwoDecimal(amount / 10000000)} Cr"
            amount >= 100000 -> "₹${formatWithTwoDecimal(amount / 100000)} L"
            amount >= 1000 -> "₹${formatWithTwoDecimal(amount / 1000)} K"
            else -> formatIndian(amount)
        }
    }

    fun amountInWords(amount: Double): String {
        if (amount == 0.0) return "Zero Only"

        val rupees = amount.toLong()
        val paise = ((amount - rupees) * 100).toLong()

        val result = StringBuilder()

        if (rupees > 0) {
            result.append(convertToWords(rupees))
            result.append(" Rupees")
        }

        if (paise > 0) {
            if (rupees > 0) result.append(" and ")
            result.append(convertToWords(paise))
            result.append(" Paise")
        }

        result.append(" Only")
        return result.toString()
    }

    private fun convertToWords(num: Long): String {
        if (num == 0L) return "Zero"

        val ones = arrayOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen",
            "Eighteen", "Nineteen")
        val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

        fun convertGroup(n: Long): String {
            if (n == 0L) return ""
            if (n < 20) return ones[n.toInt()]
            if (n < 100) return tens[(n / 10).toInt()] + if (n % 10 != 0L) " ${ones[(n % 10).toInt()]}" else ""
            return ones[(n / 100).toInt()] + " Hundred" + if (n % 100 != 0L) " ${convertGroup(n % 100)}" else ""
        }

        val result = StringBuilder()
        var remaining = num

        if (remaining >= 10000000) {
            result.append(convertGroup(remaining / 10000000))
            result.append(" Crore ")
            remaining %= 10000000
        }
        if (remaining >= 100000) {
            result.append(convertGroup(remaining / 100000))
            result.append(" Lakh ")
            remaining %= 100000
        }
        if (remaining >= 1000) {
            result.append(convertGroup(remaining / 1000))
            result.append(" Thousand ")
            remaining %= 1000
        }
        if (remaining > 0) {
            result.append(convertGroup(remaining))
        }

        return result.toString().trim()
    }

    private fun formatWithTwoDecimal(value: Double): String {
        return String.format("%.2f", value)
    }
}

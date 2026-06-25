package com.billing.app.data.repository

import com.billing.app.data.dao.InvoiceDao
import com.billing.app.data.dao.InvoiceItemDao
import com.billing.app.data.dao.BusinessProfileDao
import com.billing.app.data.entity.*
import com.billing.app.data.dao.ProductSalesSummary
import com.billing.app.util.GstCalculator
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepository @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao,
    private val businessProfileDao: BusinessProfileDao
) {
    fun getAllInvoices(): Flow<List<Invoice>> = invoiceDao.getAllInvoices()

    fun getInvoicesByType(type: InvoiceType): Flow<List<Invoice>> = invoiceDao.getInvoicesByType(type)

    fun getInvoicesByStatus(status: InvoiceStatus): Flow<List<Invoice>> = invoiceDao.getInvoicesByStatus(status)

    fun getInvoicesByParty(partyId: Long): Flow<List<Invoice>> = invoiceDao.getInvoicesByParty(partyId)

    suspend fun getInvoiceById(id: Long): Invoice? = invoiceDao.getInvoiceById(id)

    fun getInvoicesByDateRange(startDate: Long, endDate: Long): Flow<List<Invoice>> =
        invoiceDao.getInvoicesByDateRange(startDate, endDate)

    fun getTotalSalesInRange(startDate: Long, endDate: Long): Flow<Double?> =
        invoiceDao.getTotalSalesInRange(startDate, endDate)

    fun getTotalSales(): Flow<Double?> = invoiceDao.getTotalSales()

    fun getTotalOutstanding(): Flow<Double?> = invoiceDao.getTotalOutstanding()

    fun getTotalTaxInRange(startDate: Long, endDate: Long): Flow<Double?> =
        invoiceDao.getTotalTaxInRange(startDate, endDate)

    fun getOverdueInvoices(): Flow<List<Invoice>> = invoiceDao.getOverdueInvoices()

    fun getTodayInvoices(todayStart: Long): Flow<List<Invoice>> = invoiceDao.getTodayInvoices(todayStart)

    fun getItemsByInvoice(invoiceId: Long): Flow<List<InvoiceItem>> = invoiceItemDao.getItemsByInvoice(invoiceId)

    fun getTopSellingProducts(startDate: Long, endDate: Long): Flow<List<ProductSalesSummary>> =
        invoiceItemDao.getTopSellingProducts(startDate, endDate)

    fun getInvoiceCount(): Flow<Int> = invoiceDao.getInvoiceCount()

    suspend fun createInvoice(invoice: Invoice, items: List<InvoiceItem>): Long {
        // Generate invoice number
        val profile = businessProfileDao.getProfileSync()
        val currentNum = profile?.currentInvoiceNumber ?: 1
        val prefix = profile?.invoicePrefix ?: "INV"
        val invoiceNumber = "$prefix-$currentNum"

        // Calculate taxes for each item
        val calculatedItems = items.map { item ->
            val isInterState = invoice.placeOfSupply != invoice.partyState
            val taxCalc = GstCalculator.calculateItemTax(
                quantity = item.quantity,
                rate = item.rate,
                discount = item.discount,
                discountType = item.discountType,
                gstRate = item.gstRate,
                isInterState = isInterState
            )
            item.copy(
                invoiceId = 0, // Will be set after insert
                taxableAmount = taxCalc.taxableAmount,
                cgst = taxCalc.cgst,
                sgst = taxCalc.sgst,
                igst = taxCalc.igst,
                totalAmount = taxCalc.totalAmount
            )
        }

        // Calculate invoice totals
        val totals = GstCalculator.calculateInvoiceTotals(calculatedItems, invoice.roundOff)

        val finalInvoice = invoice.copy(
            invoiceNumber = invoiceNumber,
            subtotal = totals.subtotal,
            totalDiscount = totals.totalDiscount,
            totalCgst = totals.totalCgst,
            totalSgst = totals.totalSgst,
            totalIgst = totals.totalIgst,
            totalTax = totals.totalTax,
            grandTotal = totals.grandTotal,
            balanceAmount = totals.grandTotal - invoice.amountPaid,
            status = if (invoice.amountPaid >= totals.grandTotal) InvoiceStatus.PAID
                    else if (invoice.amountPaid > 0) InvoiceStatus.PARTIALLY_PAID
                    else InvoiceStatus.UNPAID
        )

        val invoiceId = invoiceDao.insertInvoice(finalInvoice)

        // Insert items with correct invoiceId
        val itemsWithId = calculatedItems.map { it.copy(invoiceId = invoiceId) }
        invoiceItemDao.insertItems(itemsWithId)

        // Increment invoice number
        businessProfileDao.incrementInvoiceNumber()

        return invoiceId
    }

    suspend fun updateInvoice(invoice: Invoice) = invoiceDao.updateInvoice(invoice)

    suspend fun deleteInvoice(invoice: Invoice) = invoiceDao.deleteInvoice(invoice)

    suspend fun updatePaymentStatus(invoiceId: Long, status: InvoiceStatus, amountPaid: Double, balance: Double) =
        invoiceDao.updatePaymentStatus(invoiceId, status, amountPaid, balance)
}

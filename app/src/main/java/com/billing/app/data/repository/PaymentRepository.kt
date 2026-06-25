package com.billing.app.data.repository

import com.billing.app.data.dao.InvoiceDao
import com.billing.app.data.dao.PartyDao
import com.billing.app.data.dao.PaymentDao
import com.billing.app.data.entity.InvoiceStatus
import com.billing.app.data.entity.Payment
import com.billing.app.data.entity.PaymentMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentDao: PaymentDao,
    private val invoiceDao: InvoiceDao,
    private val partyDao: PartyDao
) {
    fun getAllPayments(): Flow<List<Payment>> = paymentDao.getAllPayments()

    fun getPaymentsByInvoice(invoiceId: Long): Flow<List<Payment>> = paymentDao.getPaymentsByInvoice(invoiceId)

    fun getPaymentsByParty(partyId: Long): Flow<List<Payment>> = paymentDao.getPaymentsByParty(partyId)

    fun getPaymentsByDateRange(startDate: Long, endDate: Long): Flow<List<Payment>> =
        paymentDao.getPaymentsByDateRange(startDate, endDate)

    fun getTotalPaymentsInRange(startDate: Long, endDate: Long): Flow<Double?> =
        paymentDao.getTotalPaymentsInRange(startDate, endDate)

    fun getTotalByPaymentMode(mode: PaymentMode, startDate: Long, endDate: Long): Flow<Double?> =
        paymentDao.getTotalByPaymentMode(mode, startDate, endDate)

    suspend fun recordPayment(payment: Payment) {
        // Insert payment
        paymentDao.insertPayment(payment)

        // Update invoice payment status
        val invoice = invoiceDao.getInvoiceById(payment.invoiceId)
        invoice?.let {
            val newAmountPaid = it.amountPaid + payment.amount
            val newBalance = it.grandTotal - newAmountPaid
            val newStatus = when {
                newBalance <= 0 -> InvoiceStatus.PAID
                newAmountPaid > 0 -> InvoiceStatus.PARTIALLY_PAID
                else -> InvoiceStatus.UNPAID
            }
            invoiceDao.updatePaymentStatus(it.id, newStatus, newAmountPaid, newBalance)
        }
    }

    suspend fun deletePayment(payment: Payment) {
        paymentDao.deletePayment(payment)

        // Recalculate invoice payment status
        val invoice = invoiceDao.getInvoiceById(payment.invoiceId)
        invoice?.let {
            val newAmountPaid = it.amountPaid - payment.amount
            val newBalance = it.grandTotal - newAmountPaid
            val newStatus = when {
                newBalance <= 0 -> InvoiceStatus.PAID
                newAmountPaid > 0 -> InvoiceStatus.PARTIALLY_PAID
                else -> InvoiceStatus.UNPAID
            }
            invoiceDao.updatePaymentStatus(it.id, newStatus, newAmountPaid, newBalance)
        }
    }
}

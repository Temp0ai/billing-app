package com.billing.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billing.app.data.dao.ProductSalesSummary
import com.billing.app.data.entity.Invoice
import com.billing.app.data.entity.Payment
import com.billing.app.data.repository.*
import com.billing.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val paymentRepository: PaymentRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val startOfMonth = DateUtils.getStartOfMonth()
    private val endOfMonth = DateUtils.getEndOfMonth()
    private val startOfYear = DateUtils.getStartOfYear()
    private val endOfYear = DateUtils.getEndOfYear()

    // Monthly Summary
    val monthlySales: StateFlow<Double> = invoiceRepository.getTotalSalesInRange(startOfMonth, endOfMonth)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyTax: StateFlow<Double> = invoiceRepository.getTotalTaxInRange(startOfMonth, endOfMonth)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyExpenses: StateFlow<Double> = expenseRepository.getTotalExpensesInRange(startOfMonth, endOfMonth)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyPayments: StateFlow<Double> = paymentRepository.getTotalPaymentsInRange(startOfMonth, endOfMonth)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Yearly Summary
    val yearlySales: StateFlow<Double> = invoiceRepository.getTotalSalesInRange(startOfYear, endOfYear)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val yearlyTax: StateFlow<Double> = invoiceRepository.getTotalTaxInRange(startOfYear, endOfYear)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val yearlyExpenses: StateFlow<Double> = expenseRepository.getTotalExpensesInRange(startOfYear, endOfYear)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Outstanding
    val totalOutstanding: StateFlow<Double> = invoiceRepository.getTotalOutstanding()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Top Selling Products
    val topProducts: StateFlow<List<ProductSalesSummary>> = invoiceRepository.getTopSellingProducts(startOfMonth, endOfMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Profit calculation
    val monthlyProfit: StateFlow<Double> = combine(monthlySales, monthlyExpenses) { sales, expenses ->
        sales - expenses
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // GST Report data
    fun getGstReport(startDate: Long, endDate: Long): Flow<GstReportData> {
        return combine(
            invoiceRepository.getTotalSalesInRange(startDate, endDate),
            invoiceRepository.getTotalTaxInRange(startDate, endDate)
        ) { sales, tax ->
            GstReportData(
                totalSales = sales ?: 0.0,
                totalTax = tax ?: 0.0,
                cgst = (tax ?: 0.0) / 2,
                sgst = (tax ?: 0.0) / 2,
                netSales = (sales ?: 0.0) - (tax ?: 0.0)
            )
        }
    }
}

data class GstReportData(
    val totalSales: Double,
    val totalTax: Double,
    val cgst: Double,
    val sgst: Double,
    val netSales: Double
)

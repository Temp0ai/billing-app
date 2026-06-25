package com.billing.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billing.app.data.entity.BusinessProfile
import com.billing.app.data.entity.Invoice
import com.billing.app.data.entity.Product
import com.billing.app.data.repository.*
import com.billing.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val partyRepository: PartyRepository,
    private val productRepository: ProductRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val startOfDay = DateUtils.getStartOfDay()
    private val endOfDay = DateUtils.getEndOfDay()
    private val startOfMonth = DateUtils.getStartOfMonth()
    private val endOfMonth = DateUtils.getEndOfMonth()

    val todaySales: StateFlow<Double> = invoiceRepository.getTotalSalesInRange(startOfDay, endOfDay)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlySales: StateFlow<Double> = invoiceRepository.getTotalSalesInRange(startOfMonth, endOfMonth)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalSales: StateFlow<Double> = invoiceRepository.getTotalSales()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalOutstanding: StateFlow<Double> = invoiceRepository.getTotalOutstanding()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyExpenses: StateFlow<Double> = expenseRepository.getTotalExpensesInRange(startOfMonth, endOfMonth)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val stockValue: StateFlow<Double> = productRepository.getTotalStockValue()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val partyCount: StateFlow<Int> = partyRepository.getPartyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val productCount: StateFlow<Int> = productRepository.getProductCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val invoiceCount: StateFlow<Int> = invoiceRepository.getInvoiceCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayInvoices: StateFlow<List<Invoice>> = invoiceRepository.getTodayInvoices(startOfDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overdueInvoices: StateFlow<List<Invoice>> = invoiceRepository.getOverdueInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = productRepository.getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

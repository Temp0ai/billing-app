package com.billing.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billing.app.data.entity.*
import com.billing.app.data.repository.InvoiceRepository
import com.billing.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    val allInvoices: StateFlow<List<Invoice>> = invoiceRepository.getAllInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedInvoice = MutableStateFlow<Invoice?>(null)
    val selectedInvoice: StateFlow<Invoice?> = _selectedInvoice

    private val _invoiceItems = MutableStateFlow<List<InvoiceItem>>(emptyList())
    val invoiceItems: StateFlow<List<InvoiceItem>> = _invoiceItems

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating

    fun loadInvoice(id: Long) {
        viewModelScope.launch {
            _selectedInvoice.value = invoiceRepository.getInvoiceById(id)
            invoiceRepository.getItemsByInvoice(id).collect {
                _invoiceItems.value = it
            }
        }
    }

    fun createInvoice(
        invoice: Invoice,
        items: List<InvoiceItem>,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isCreating.value = true
                val id = invoiceRepository.createInvoice(invoice, items)

                // Update stock for each item
                items.forEach { item ->
                    item.productId?.let { productId ->
                        productRepository.updateStock(productId, -item.quantity)
                    }
                }

                onSuccess(id)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create invoice")
            } finally {
                _isCreating.value = false
            }
        }
    }

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch {
            invoiceRepository.deleteInvoice(invoice)
        }
    }

    fun searchInvoices(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredInvoices(): Flow<List<Invoice>> {
        return combine(allInvoices, _searchQuery) { invoices, query ->
            if (query.isBlank()) invoices
            else invoices.filter {
                it.invoiceNumber.contains(query, ignoreCase = true) ||
                it.partyName.contains(query, ignoreCase = true)
            }
        }
    }
}

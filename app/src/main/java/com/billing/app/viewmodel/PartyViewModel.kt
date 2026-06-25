package com.billing.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billing.app.data.entity.Party
import com.billing.app.data.entity.PartyType
import com.billing.app.data.repository.PartyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartyViewModel @Inject constructor(
    private val partyRepository: PartyRepository
) : ViewModel() {

    val allParties: StateFlow<List<Party>> = partyRepository.getAllParties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalReceivable: StateFlow<Double> = partyRepository.getTotalReceivable()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPayable: StateFlow<Double> = partyRepository.getTotalPayable()
        .map { Math.abs(it ?: 0.0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filterType = MutableStateFlow<PartyType?>(null)
    val filterType: StateFlow<PartyType?> = _filterType

    fun searchParties(query: String) {
        _searchQuery.value = query
    }

    fun filterByType(type: PartyType?) {
        _filterType.value = type
    }

    fun getFilteredParties(): Flow<List<Party>> {
        return combine(allParties, _searchQuery, _filterType) { parties, query, type ->
            parties.filter { party ->
                val matchesQuery = query.isBlank() ||
                    party.name.contains(query, ignoreCase = true) ||
                    party.phone.contains(query) ||
                    party.gstin.contains(query, ignoreCase = true)
                val matchesType = type == null || party.partyType == type || party.partyType == PartyType.BOTH
                matchesQuery && matchesType
            }
        }
    }

    fun addParty(party: Party, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                partyRepository.insertParty(party)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun updateParty(party: Party) {
        viewModelScope.launch {
            partyRepository.updateParty(party)
        }
    }

    fun deleteParty(party: Party) {
        viewModelScope.launch {
            partyRepository.deleteParty(party)
        }
    }
}

package com.billing.app.data.repository

import com.billing.app.data.dao.PartyDao
import com.billing.app.data.entity.Party
import com.billing.app.data.entity.PartyType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartyRepository @Inject constructor(
    private val partyDao: PartyDao
) {
    fun getAllParties(): Flow<List<Party>> = partyDao.getAllParties()

    fun getPartiesByType(type: PartyType): Flow<List<Party>> = partyDao.getPartiesByType(type)

    suspend fun getPartyById(id: Long): Party? = partyDao.getPartyById(id)

    fun searchParties(query: String): Flow<List<Party>> = partyDao.searchParties(query)

    fun getTotalReceivable(): Flow<Double?> = partyDao.getTotalReceivable()

    fun getTotalPayable(): Flow<Double?> = partyDao.getTotalPayable()

    fun getPartyCount(): Flow<Int> = partyDao.getPartyCount()

    suspend fun insertParty(party: Party): Long = partyDao.insertParty(party)

    suspend fun updateParty(party: Party) = partyDao.updateParty(party)

    suspend fun deleteParty(party: Party) = partyDao.deleteParty(party)
}

package com.billing.app.data.dao

import androidx.room.*
import com.billing.app.data.entity.Party
import com.billing.app.data.entity.PartyType
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {

    @Query("SELECT * FROM parties ORDER BY name ASC")
    fun getAllParties(): Flow<List<Party>>

    @Query("SELECT * FROM parties WHERE partyType = :type ORDER BY name ASC")
    fun getPartiesByType(type: PartyType): Flow<List<Party>>

    @Query("SELECT * FROM parties WHERE id = :id")
    suspend fun getPartyById(id: Long): Party?

    @Query("SELECT * FROM parties WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchParties(query: String): Flow<List<Party>>

    @Query("SELECT * FROM parties WHERE gstin = :gstin LIMIT 1")
    suspend fun getPartyByGstin(gstin: String): Party?

    @Query("SELECT SUM(currentBalance) FROM parties WHERE partyType = :type AND currentBalance > 0")
    fun getTotalReceivable(type: PartyType = PartyType.CUSTOMER): Flow<Double?>

    @Query("SELECT SUM(currentBalance) FROM parties WHERE partyType = :type AND currentBalance < 0")
    fun getTotalPayable(type: PartyType = PartyType.SUPPLIER): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: Party): Long

    @Update
    suspend fun updateParty(party: Party)

    @Delete
    suspend fun deleteParty(party: Party)

    @Query("SELECT * FROM parties WHERE isSynced = 0")
    suspend fun getUnsyncedParties(): List<Party>

    @Query("UPDATE parties SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM parties")
    fun getPartyCount(): Flow<Int>
}

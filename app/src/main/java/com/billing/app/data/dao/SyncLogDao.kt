package com.billing.app.data.dao

import androidx.room.*
import com.billing.app.data.entity.SyncLog
import com.billing.app.data.entity.SyncAction
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncLogDao {

    @Query("SELECT * FROM sync_log WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getPendingSyncLogs(): Flow<List<SyncLog>>

    @Query("SELECT * FROM sync_log WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getPendingSyncLogsSync(): List<SyncLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SyncLog)

    @Query("UPDATE sync_log SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("DELETE FROM sync_log WHERE isSynced = 1 AND timestamp < :before")
    suspend fun cleanupOldLogs(before: Long)

    @Query("SELECT COUNT(*) FROM sync_log WHERE isSynced = 0")
    fun getPendingCount(): Flow<Int>
}

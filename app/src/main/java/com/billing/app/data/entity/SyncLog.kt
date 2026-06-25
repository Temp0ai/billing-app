package com.billing.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_log")
data class SyncLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tableName: String,
    val recordId: Long,
    val action: SyncAction,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

enum class SyncAction {
    INSERT,
    UPDATE,
    DELETE
}

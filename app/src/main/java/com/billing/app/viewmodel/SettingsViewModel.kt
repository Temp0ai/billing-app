package com.billing.app.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billing.app.data.dao.BusinessProfileDao
import com.billing.app.data.entity.BusinessProfile
import com.billing.app.sync.googledrive.GoogleDriveSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val businessProfileDao: BusinessProfileDao,
    val driveSyncManager: GoogleDriveSyncManager
) : ViewModel() {

    val businessProfile: StateFlow<BusinessProfile?> = businessProfileDao.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    private val _lastBackupTime = MutableStateFlow<Long?>(null)
    val lastBackupTime: StateFlow<Long?> = _lastBackupTime

    val isSignedIn: Boolean
        get() = driveSyncManager.isSignedIn()

    val signedInEmail: String?
        get() = driveSyncManager.getSignedInEmail()

    fun saveProfile(profile: BusinessProfile) {
        viewModelScope.launch {
            businessProfileDao.insertProfile(profile)
        }
    }

    fun updateProfile(profile: BusinessProfile) {
        viewModelScope.launch {
            businessProfileDao.updateProfile(profile)
        }
    }

    fun getSignInIntent(): Intent = driveSyncManager.getSignInIntent()

    fun onSignInResult(success: Boolean) {
        if (success) {
            viewModelScope.launch {
                driveSyncManager.initDriveService()
                loadLastBackupTime()
            }
        }
    }

    fun backupToDrive() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing("Backing up...")
            val result = driveSyncManager.backupDatabase()
            _syncStatus.value = if (result.isSuccess) {
                loadLastBackupTime()
                SyncStatus.Success(result.getOrDefault("Backup complete"))
            } else {
                SyncStatus.Error(result.exceptionOrNull()?.message ?: "Backup failed")
            }
        }
    }

    fun restoreFromDrive() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing("Restoring...")
            val result = driveSyncManager.restoreDatabase()
            _syncStatus.value = if (result.isSuccess) {
                SyncStatus.Success(result.getOrDefault("Restore complete. Restart app."))
            } else {
                SyncStatus.Error(result.exceptionOrNull()?.message ?: "Restore failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            driveSyncManager.signOut()
        }
    }

    private fun loadLastBackupTime() {
        viewModelScope.launch {
            _lastBackupTime.value = driveSyncManager.getLastBackupTime()
        }
    }
}

sealed class SyncStatus {
    object Idle : SyncStatus()
    data class Syncing(val message: String) : SyncStatus()
    data class Success(val message: String) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}

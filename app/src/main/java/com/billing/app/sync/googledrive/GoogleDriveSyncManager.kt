package com.billing.app.sync.googledrive

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveSyncManager @Inject constructor(
    private val context: Context
) {
    private var driveService: Drive? = null
    private val syncFolderName = "BillingApp_Backup"
    private val databaseFileName = "billing_database"
    private val TAG = "DriveSync"

    companion object {
        const val REQUEST_CODE_SIGN_IN = 400
        val REQUIRED_SCOPES = listOf(Scope(DriveScopes.DRIVE_FILE))
    }

    /**
     * Get sign-in intent for Google Sign-In
     */
    fun getSignInIntent(): Intent {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        val client = GoogleSignIn.getClient(context, signInOptions)
        return client.signInIntent
    }

    /**
     * Initialize Drive service after sign-in
     */
    suspend fun initDriveService(): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                Log.e(TAG, "No signed-in account found")
                return@withContext false
            }

            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account

            driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("BillingApp")
                .build()

            Log.d(TAG, "Drive service initialized for ${account.email}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init Drive service", e)
            false
        }
    }

    /**
     * Check if user is signed in
     */
    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    /**
     * Get signed-in user email
     */
    fun getSignedInEmail(): String? {
        return GoogleSignIn.getLastSignedInAccount(context)?.email
    }

    /**
     * Sign out
     */
    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val client = GoogleSignIn.getClient(context, signInOptions)
            client.signOut().addOnCompleteListener {
                driveService = null
                Log.d(TAG, "Signed out successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
        }
    }

    /**
     * Backup database to Google Drive
     */
    suspend fun backupDatabase(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext Result.failure(Exception("Drive not initialized"))

            // Get or create app folder
            val folderId = getOrCreateFolder(service, syncFolderName)

            // Get local database file
            val dbFile = context.getDatabasePath(databaseFileName)
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Database file not found"))
            }

            // Check if file exists on Drive
            val existingFileId = findFile(service, databaseFileName, folderId)

            val fileMetadata = File().apply {
                name = databaseFileName
                parents = listOf(folderId)
            }

            val fileContent = com.google.api.client.http.FileContent(
                "application/x-sqlite3",
                dbFile
            )

            if (existingFileId != null) {
                // Update existing file
                service.files().update(existingFileId, fileMetadata, fileContent).execute()
                Log.d(TAG, "Database backup updated on Drive")
            } else {
                // Create new file
                service.files().create(fileMetadata, fileContent).execute()
                Log.d(TAG, "Database backup created on Drive")
            }

            // Also backup WAL and SHM files if they exist
            val walFile = File(dbFile.absolutePath + "-wal")
            val shmFile = File(dbFile.absolutePath + "-shm")

            if (walFile.exists()) {
                uploadFile(service, walFile, "${databaseFileName}-wal", folderId)
            }
            if (shmFile.exists()) {
                uploadFile(service, shmFile, "${databaseFileName}-shm", folderId)
            }

            Result.success("Backup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            Result.failure(e)
        }
    }

    /**
     * Restore database from Google Drive
     */
    suspend fun restoreDatabase(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext Result.failure(Exception("Drive not initialized"))

            val folderId = findFile(service, syncFolderName)
                ?: return@withContext Result.failure(Exception("No backup folder found on Drive"))

            val fileId = findFile(service, databaseFileName, folderId)
                ?: return@withContext Result.failure(Exception("No backup file found on Drive"))

            // Download file
            val outputStream = FileOutputStream(context.getDatabasePath(databaseFileName))
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            outputStream.close()

            Log.d(TAG, "Database restored from Drive")
            Result.success("Restore completed successfully. Please restart the app.")
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            Result.failure(e)
        }
    }

    /**
     * Get last backup timestamp
     */
    suspend fun getLastBackupTime(): Long? = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext null
            val folderId = findFile(service, syncFolderName) ?: return@withContext null
            val fileId = findFile(service, databaseFileName, folderId) ?: return@withContext null

            val file = service.files().get(fileId).setFields("modifiedTime").execute()
            file.modifiedTime?.value
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get last backup time", e)
            null
        }
    }

    private fun getOrCreateFolder(service: Drive, folderName: String): String {
        val existing = findFile(service, folderName)
        if (existing != null) return existing

        val metadata = File().apply {
            name = folderName
            mimeType = "application/vnd.google-apps.folder"
        }
        return service.files().create(metadata).setFields("id").execute().id
    }

    private fun findFile(service: Drive, fileName: String, parentId: String? = null): String? {
        var query = "name = '$fileName' and trashed = false"
        if (parentId != null) {
            query += " and '$parentId' in parents"
        }

        val result = service.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        return result.files?.firstOrNull()?.id
    }

    private fun uploadFile(service: Drive, file: java.io.File, name: String, parentId: String) {
        val metadata = File().apply {
            this.name = name
            parents = listOf(parentId)
        }
        val content = com.google.api.client.http.FileContent("application/octet-stream", file)
        service.files().create(metadata, content).execute()
    }
}

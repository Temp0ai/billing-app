package com.billing.app.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.billing.app.ui.theme.*
import com.billing.app.util.DateUtils
import com.billing.app.viewmodel.SettingsViewModel
import com.billing.app.viewmodel.SyncStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val profile by viewModel.businessProfile.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val lastBackupTime by viewModel.lastBackupTime.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = TextOnPrimary,
                    navigationIconColor = TextOnPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Business Profile Section
            Text("Business Profile", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    profile?.let { p ->
                        SettingsItem("Business Name", p.businessName)
                        SettingsItem("GSTIN", p.gstin.ifEmpty { "Not set" })
                        SettingsItem("Phone", p.phone.ifEmpty { "Not set" })
                        SettingsItem("Email", p.email.ifEmpty { "Not set" })
                        SettingsItem("Address", if (p.address.isNotEmpty()) "${p.address}, ${p.city}, ${p.state}" else "Not set")
                        SettingsItem("Invoice Prefix", p.invoicePrefix)
                    } ?: run {
                        Text("No profile set up yet", color = TextSecondary)
                    }
                }
            }

            // Google Drive Sync Section
            Text("Google Drive Backup", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (viewModel.isSignedIn) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudDone, "Connected", tint = Success)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Connected", fontWeight = FontWeight.Bold, color = Success)
                                Text(viewModel.signedInEmail ?: "", fontSize = 12.sp, color = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (lastBackupTime != null) {
                            Text(
                                "Last backup: ${DateUtils.formatDateTime(lastBackupTime!!)}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Sync Status
                        when (syncStatus) {
                            is SyncStatus.Syncing -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text((syncStatus as SyncStatus.Syncing).message, color = Info)
                                }
                            }
                            is SyncStatus.Success -> {
                                Text((syncStatus as SyncStatus.Success).message, color = Success)
                            }
                            is SyncStatus.Error -> {
                                Text((syncStatus as SyncStatus.Error).message, color = Error)
                            }
                            else -> {}
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.backupToDrive() },
                                modifier = Modifier.weight(1f),
                                enabled = syncStatus !is SyncStatus.Syncing,
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Icon(Icons.Default.Backup, "Backup", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Backup")
                            }
                            OutlinedButton(
                                onClick = { viewModel.restoreFromDrive() },
                                modifier = Modifier.weight(1f),
                                enabled = syncStatus !is SyncStatus.Syncing
                            ) {
                                Icon(Icons.Default.Restore, "Restore", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { viewModel.signOut() },
                            colors = ButtonDefaults.textButtonColors(contentColor = Error)
                        ) {
                            Text("Disconnect Google Drive")
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CloudOff, "Not Connected", modifier = Modifier.size(48.dp), tint = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Not connected to Google Drive", color = TextSecondary)
                            Text("Backup your data to Google Drive", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { /* Launch Google Sign-In */ },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Icon(Icons.Default.Login, "Sign In")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connect Google Drive")
                            }
                        }
                    }
                }
            }

            // App Info
            Text("About", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsItem("App Name", "Billing App")
                    SettingsItem("Version", "1.0.0")
                    SettingsItem("Developer", "Built with ❤️")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

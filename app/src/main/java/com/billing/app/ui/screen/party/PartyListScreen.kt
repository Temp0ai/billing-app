package com.billing.app.ui.screen.party

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.billing.app.data.entity.Party
import com.billing.app.data.entity.PartyType
import com.billing.app.ui.navigation.Screen
import com.billing.app.ui.theme.*
import com.billing.app.util.CurrencyUtils
import com.billing.app.viewmodel.PartyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyListScreen(
    navController: NavController,
    viewModel: PartyViewModel = hiltViewModel()
) {
    val parties by viewModel.getFilteredParties().collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val totalReceivable by viewModel.totalReceivable.collectAsState()
    val totalPayable by viewModel.totalPayable.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parties") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AddParty.route) }) {
                        Icon(Icons.Default.PersonAdd, "Add Party")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = TextOnPrimary,
                    navigationIconColor = TextOnPrimary,
                    actionIconColor = TextOnPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Balance Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Receivable", fontSize = 12.sp, color = Success)
                        Text(CurrencyUtils.formatIndian(totalReceivable), fontWeight = FontWeight.Bold, color = Success)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Payable", fontSize = 12.sp, color = Error)
                        Text(CurrencyUtils.formatIndian(totalPayable), fontWeight = FontWeight.Bold, color = Error)
                    }
                }
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchParties(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search parties...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                singleLine = true
            )

            // Filter Chips
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == null,
                    onClick = { viewModel.filterByType(null) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filterType == PartyType.CUSTOMER,
                    onClick = { viewModel.filterByType(PartyType.CUSTOMER) },
                    label = { Text("Customers") }
                )
                FilterChip(
                    selected = filterType == PartyType.SUPPLIER,
                    onClick = { viewModel.filterByType(PartyType.SUPPLIER) },
                    label = { Text("Suppliers") }
                )
            }

            // Party List
            if (parties.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.People, "No Parties", modifier = Modifier.size(64.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No parties found", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(parties) { party ->
                        PartyCard(party = party)
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun PartyCard(party: Party) {
    val typeColor = when (party.partyType) {
        PartyType.CUSTOMER -> Primary
        PartyType.SUPPLIER -> Warning
        PartyType.BOTH -> Info
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = typeColor.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    party.partyType.name.first().toString(),
                    modifier = Modifier.padding(8.dp),
                    color = typeColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(party.name, fontWeight = FontWeight.Bold)
                if (party.phone.isNotEmpty()) Text(party.phone, fontSize = 12.sp, color = TextSecondary)
                if (party.gstin.isNotEmpty()) Text("GSTIN: ${party.gstin}", fontSize = 12.sp, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                val balance = party.currentBalance
                Text(
                    CurrencyUtils.formatIndian(Math.abs(balance)),
                    fontWeight = FontWeight.Bold,
                    color = if (balance >= 0) Success else Error
                )
                Text(
                    if (balance >= 0) "To Receive" else "To Pay",
                    fontSize = 10.sp,
                    color = if (balance >= 0) Success else Error
                )
            }
        }
    }
}

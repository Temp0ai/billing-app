package com.billing.app.ui.screen.invoice

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
import com.billing.app.data.entity.Invoice
import com.billing.app.data.entity.InvoiceStatus
import com.billing.app.ui.navigation.Screen
import com.billing.app.ui.theme.*
import com.billing.app.util.CurrencyUtils
import com.billing.app.util.DateUtils
import com.billing.app.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoices by viewModel.getFilteredInvoices().collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoices") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.CreateInvoice.route) }) {
                        Icon(Icons.Default.Add, "New Invoice")
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchInvoices(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search invoices...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                singleLine = true
            )

            if (invoices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Description,
                            "No Invoices",
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No invoices yet", color = TextSecondary)
                        Text("Tap + to create your first invoice", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(invoices) { invoice ->
                        InvoiceCard(
                            invoice = invoice,
                            onClick = {
                                navController.navigate(Screen.InvoiceDetail.createRoute(invoice.id))
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun InvoiceCard(invoice: Invoice, onClick: () -> Unit) {
    val statusColor = when (invoice.status) {
        InvoiceStatus.PAID -> Success
        InvoiceStatus.UNPAID -> Error
        InvoiceStatus.PARTIALLY_PAID -> Warning
        InvoiceStatus.OVERDUE -> Error
        InvoiceStatus.DRAFT -> TextSecondary
        InvoiceStatus.CANCELLED -> TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        invoice.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(invoice.partyName, color = TextSecondary, fontSize = 14.sp)
                    Text(DateUtils.formatDate(invoice.invoiceDate), color = TextSecondary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        CurrencyUtils.formatIndian(invoice.grandTotal),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (invoice.balanceAmount > 0) {
                        Text(
                            "Balance: ${CurrencyUtils.formatIndian(invoice.balanceAmount)}",
                            color = Error,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

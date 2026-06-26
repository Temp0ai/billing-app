package com.billing.app.ui.screen.invoice

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
import com.billing.app.data.entity.InvoiceItem
import com.billing.app.data.entity.InvoiceStatus
import com.billing.app.ui.theme.*
import com.billing.app.util.CurrencyUtils
import com.billing.app.util.DateUtils
import com.billing.app.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    navController: NavController,
    invoiceId: Long,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val invoice by viewModel.selectedInvoice.collectAsState()
    val items by viewModel.invoiceItems.collectAsState()

    LaunchedEffect(invoiceId) {
        viewModel.loadInvoice(invoiceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(invoice?.invoiceNumber ?: "Invoice") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share/Print */ }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                    IconButton(onClick = { /* Download PDF */ }) {
                        Icon(Icons.Default.PictureAsPdf, "PDF")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = TextOnPrimary,
                    navigationIconContentColor = TextOnPrimary,
                    actionIconContentColor = TextOnPrimary
                )
            )
        }
    ) { padding ->
        invoice?.let { inv ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Status Card
                item {
                    val statusColor = when (inv.status) {
                        InvoiceStatus.PAID -> Success
                        InvoiceStatus.UNPAID -> Error
                        InvoiceStatus.PARTIALLY_PAID -> Warning
                        InvoiceStatus.OVERDUE -> Error
                        else -> TextSecondary
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(inv.invoiceType.name.replace("_", " "), color = TextSecondary)
                            }
                            Surface(
                                color = statusColor,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    inv.status.name,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = TextOnPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Party Details
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Bill To", fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 12.sp)
                            Text(inv.partyName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (inv.partyGstin.isNotEmpty()) Text("GSTIN: ${inv.partyGstin}", fontSize = 14.sp)
                            if (inv.partyAddress.isNotEmpty()) Text(inv.partyAddress, fontSize = 14.sp, color = TextSecondary)
                        }
                    }
                }

                // Date Info
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Invoice Date", fontSize = 12.sp, color = TextSecondary)
                            Text(DateUtils.formatDate(inv.invoiceDate), fontWeight = FontWeight.Medium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Due Date", fontSize = 12.sp, color = TextSecondary)
                            Text(DateUtils.formatDate(inv.dueDate), fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Items Table
                item {
                    Text("Items", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Header
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("#", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Item", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Qty", modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Rate", modifier = Modifier.width(60.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Amount", modifier = Modifier.width(70.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            items(items) { item ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                    Text("${items.indexOf(item) + 1}", modifier = Modifier.width(30.dp), fontSize = 12.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontSize = 12.sp)
                                        if (item.gstRate > 0) Text("GST: ${item.gstRate}%", fontSize = 10.sp, color = TextSecondary)
                                    }
                                    Text("${item.quantity}", modifier = Modifier.width(50.dp), fontSize = 12.sp)
                                    Text(CurrencyUtils.formatIndian(item.rate), modifier = Modifier.width(60.dp), fontSize = 12.sp)
                                    Text(CurrencyUtils.formatIndian(item.totalAmount), modifier = Modifier.width(70.dp), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Totals
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            TotalRow("Subtotal", CurrencyUtils.formatIndian(inv.subtotal))
                            if (inv.totalCgst > 0) {
                                TotalRow("CGST", CurrencyUtils.formatIndian(inv.totalCgst))
                                TotalRow("SGST", CurrencyUtils.formatIndian(inv.totalSgst))
                            }
                            if (inv.totalIgst > 0) {
                                TotalRow("IGST", CurrencyUtils.formatIndian(inv.totalIgst))
                            }
                            if (inv.roundOff != 0.0) {
                                TotalRow("Round Off", CurrencyUtils.formatIndian(inv.roundOff))
                            }
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(CurrencyUtils.formatIndian(inv.grandTotal), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary)
                            }
                        }
                    }
                }

                // Payment Info
                if (inv.amountPaid > 0 || inv.balanceAmount > 0) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Payment Status", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                TotalRow("Amount Paid", CurrencyUtils.formatIndian(inv.amountPaid))
                                TotalRow("Balance", CurrencyUtils.formatIndian(inv.balanceAmount))
                            }
                        }
                    }
                }

                // Notes
                if (inv.notes.isNotEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Notes", fontWeight = FontWeight.Bold)
                                Text(inv.notes, color = TextSecondary)
                            }
                        }
                    }
                }

                // Action Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Record Payment */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Payment, "Payment", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record Payment")
                        }
                        Button(
                            onClick = { /* Generate PDF */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, "PDF", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share PDF")
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun TotalRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary)
        Text(value)
    }
}

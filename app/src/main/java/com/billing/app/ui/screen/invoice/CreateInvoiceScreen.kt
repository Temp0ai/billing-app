package com.billing.app.ui.screen.invoice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.billing.app.data.entity.*
import com.billing.app.ui.theme.*
import com.billing.app.util.CurrencyUtils
import com.billing.app.util.GstCalculator
import com.billing.app.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    navController: NavController,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    var partyName by remember { mutableStateOf("") }
    var partyGstin by remember { mutableStateOf("") }
    var partyAddress by remember { mutableStateOf("") }
    var partyState by remember { mutableStateOf("") }
    var placeOfSupply by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(InvoiceType.TAX_INVOICE) }

    val items = remember { mutableStateListOf<InvoiceItemData>() }
    var showAddItemDialog by remember { mutableStateOf(false) }
    val isCreating by viewModel.isCreating.collectAsState()

    // Calculate totals
    val subtotal = items.sumOf { it.quantity * it.rate }
    val totalTax = items.sumOf {
        val isInterState = placeOfSupply != partyState && placeOfSupply.isNotEmpty()
        GstCalculator.calculateItemTax(it.quantity, it.rate, it.discount, DiscountType.PERCENTAGE, it.gstRate, isInterState = isInterState).totalTax
    }
    val roundOff = GstCalculator.calculateRoundOff(subtotal + totalTax)
    val grandTotal = subtotal + totalTax + roundOff

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Invoice") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = TextOnPrimary,
                    navigationIconContentColor = TextOnPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Invoice Type Selector
            item {
                Text("Invoice Type", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InvoiceType.values().take(3).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.replace("_", " ")) }
                        )
                    }
                }
            }

            // Party Details
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Customer Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = partyName,
                            onValueChange = { partyName = it },
                            label = { Text("Customer Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = partyGstin,
                            onValueChange = { partyGstin = it.uppercase() },
                            label = { Text("GSTIN") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = partyGstin.isNotEmpty() && !GstCalculator.isValidGstin(partyGstin)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = partyAddress,
                            onValueChange = { partyAddress = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = partyState,
                                onValueChange = { partyState = it },
                                label = { Text("State") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = placeOfSupply,
                                onValueChange = { placeOfSupply = it },
                                label = { Text("Place of Supply") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Items Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Items", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    FilledTonalButton(onClick = { showAddItemDialog = true }) {
                        Icon(Icons.Default.Add, "Add Item", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item")
                    }
                }
            }

            if (items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No items added yet", color = TextSecondary)
                        }
                    }
                }
            } else {
                items(items.toList()) { item ->
                    ItemCard(
                        item = item,
                        isInterState = placeOfSupply != partyState && placeOfSupply.isNotEmpty(),
                        onRemove = { items.remove(item) }
                    )
                }
            }

            // Totals
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal"); Text(CurrencyUtils.formatIndian(subtotal))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Tax"); Text(CurrencyUtils.formatIndian(totalTax))
                        }
                        if (roundOff != 0.0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Round Off"); Text(CurrencyUtils.formatIndian(roundOff))
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(CurrencyUtils.formatIndian(grandTotal), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(CurrencyUtils.amountInWords(grandTotal), fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            // Notes
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        val invoice = Invoice(
                            invoiceNumber = "INV-${System.currentTimeMillis()}",
                            partyId = 0,
                            partyName = partyName,
                            partyGstin = partyGstin,
                            partyAddress = partyAddress,
                            partyState = partyState,
                            placeOfSupply = placeOfSupply,
                            notes = notes,
                            invoiceType = selectedType,
                            roundOff = roundOff
                        )

                        val invoiceItems = items.map {
                            InvoiceItem(
                                invoiceId = 0,
                                productName = it.productName,
                                hsnCode = it.hsnCode,
                                unit = it.unit,
                                quantity = it.quantity,
                                rate = it.rate,
                                discount = it.discount,
                                gstRate = it.gstRate
                            )
                        }

                        viewModel.createInvoice(
                            invoice = invoice,
                            items = invoiceItems,
                            onSuccess = { navController.popBackStack() },
                            onError = { /* Show error */ }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = partyName.isNotBlank() && items.isNotEmpty() && !isCreating,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TextOnPrimary)
                    } else {
                        Text("Save Invoice", fontSize = 16.sp)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        AddItemDialog(
            onDismiss = { showAddItemDialog = false },
            onAdd = { item ->
                items.add(item)
                showAddItemDialog = false
            }
        )
    }
}

data class InvoiceItemData(
    val productName: String,
    val hsnCode: String = "",
    val unit: String = "PCS",
    val quantity: Double = 1.0,
    val rate: Double = 0.0,
    val discount: Double = 0.0,
    val gstRate: Double = 18.0
)

@Composable
fun ItemCard(item: InvoiceItemData, isInterState: Boolean, onRemove: () -> Unit) {
    val taxResult = GstCalculator.calculateItemTax(
        item.quantity, item.rate, item.discount, DiscountType.PERCENTAGE, item.gstRate, isInterState = isInterState
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, fontWeight = FontWeight.Bold)
                Text("HSN: ${item.hsnCode} | ${item.quantity} ${item.unit} × ${CurrencyUtils.formatIndian(item.rate)}", fontSize = 12.sp, color = TextSecondary)
                if (isInterState) {
                    Text("IGST ${item.gstRate}%: ${CurrencyUtils.formatIndian(taxResult.igst)}", fontSize = 12.sp, color = IgstColor)
                } else {
                    Text("CGST ${item.gstRate/2}% + SGST ${item.gstRate/2}%", fontSize = 12.sp, color = TextSecondary)
                }
                Text(CurrencyUtils.formatIndian(taxResult.totalAmount), fontWeight = FontWeight.Bold, color = Primary)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, "Remove", tint = Error)
            }
        }
    }
}

@Composable
fun AddItemDialog(onDismiss: () -> Unit, onAdd: (InvoiceItemData) -> Unit) {
    var name by remember { mutableStateOf("") }
    var hsn by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("PCS") }
    var qty by remember { mutableStateOf("1") }
    var rate by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("0") }
    var gstRate by remember { mutableStateOf("18") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = hsn,
                    onValueChange = { hsn = it },
                    label = { Text("HSN Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it },
                        label = { Text("Qty") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Rate (₹) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = discount,
                        onValueChange = { discount = it },
                        label = { Text("Discount %") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = gstRate,
                        onValueChange = { gstRate = it },
                        label = { Text("GST %") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && rate.isNotBlank()) {
                        onAdd(
                            InvoiceItemData(
                                productName = name,
                                hsnCode = hsn,
                                unit = unit,
                                quantity = qty.toDoubleOrNull() ?: 1.0,
                                rate = rate.toDoubleOrNull() ?: 0.0,
                                discount = discount.toDoubleOrNull() ?: 0.0,
                                gstRate = gstRate.toDoubleOrNull() ?: 18.0
                            )
                        )
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

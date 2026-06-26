package com.billing.app.ui.screen.reports

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
import com.billing.app.ui.theme.*
import com.billing.app.util.CurrencyUtils
import com.billing.app.util.DateUtils
import com.billing.app.viewmodel.ReportsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val monthlySales by viewModel.monthlySales.collectAsState()
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsState()
    val monthlyProfit by viewModel.monthlyProfit.collectAsState()
    val monthlyTax by viewModel.monthlyTax.collectAsState()
    val yearlySales by viewModel.yearlySales.collectAsState()
    val yearlyExpenses by viewModel.yearlyExpenses.collectAsState()
    val totalOutstanding by viewModel.totalOutstanding.collectAsState()
    val topProducts by viewModel.topProducts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
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

            // Current Month Header
            item {
                Text(
                    DateUtils.getIndianFinancialYear(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text("Current Financial Year", color = TextSecondary, fontSize = 14.sp)
            }

            // Monthly Summary
            item {
                Text("This Month", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReportCard(
                        modifier = Modifier.weight(1f),
                        title = "Sales",
                        value = CurrencyUtils.formatIndian(monthlySales),
                        color = SalesColor,
                        icon = Icons.Default.TrendingUp
                    )
                    ReportCard(
                        modifier = Modifier.weight(1f),
                        title = "Expenses",
                        value = CurrencyUtils.formatIndian(monthlyExpenses),
                        color = ExpenseColor,
                        icon = Icons.Default.TrendingDown
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReportCard(
                        modifier = Modifier.weight(1f),
                        title = "Profit",
                        value = CurrencyUtils.formatIndian(monthlyProfit),
                        color = if (monthlyProfit >= 0) ProfitColor else Error,
                        icon = Icons.Default.AccountBalance
                    )
                    ReportCard(
                        modifier = Modifier.weight(1f),
                        title = "GST Collected",
                        value = CurrencyUtils.formatIndian(monthlyTax),
                        color = IgstColor,
                        icon = Icons.Default.Receipt
                    )
                }
            }

            // Yearly Summary
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Yearly Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Sales"); Text(CurrencyUtils.formatIndian(yearlySales), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Expenses"); Text(CurrencyUtils.formatIndian(yearlyExpenses), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Profit", fontWeight = FontWeight.Bold)
                            Text(
                                CurrencyUtils.formatIndian(yearlySales - yearlyExpenses),
                                fontWeight = FontWeight.Bold,
                                color = if (yearlySales - yearlyExpenses >= 0) ProfitColor else Error
                            )
                        }
                    }
                }
            }

            // Outstanding
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = OutstandingColor.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Outstanding", color = OutstandingColor)
                            Text(
                                CurrencyUtils.formatIndian(totalOutstanding),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = OutstandingColor
                            )
                        }
                        Icon(Icons.Default.Warning, "Outstanding", tint = OutstandingColor, modifier = Modifier.size(32.dp))
                    }
                }
            }

            // GST Summary
            item {
                Text("GST Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CGST"); Text(CurrencyUtils.formatIndian(monthlyTax / 2))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SGST"); Text(CurrencyUtils.formatIndian(monthlyTax / 2))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total GST", fontWeight = FontWeight.Bold)
                            Text(CurrencyUtils.formatIndian(monthlyTax), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Top Selling Products
            if (topProducts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Top Selling Products (This Month)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                items(topProducts.take(5)) { product ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(product.productName, fontWeight = FontWeight.Medium)
                                Text("Qty: ${product.totalQty}", fontSize = 12.sp, color = TextSecondary)
                            }
                            Text(CurrencyUtils.formatIndian(product.totalAmount), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Export Options
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Export", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { /* Export GST Report */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Description, "GST", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GST Report")
                    }
                    OutlinedButton(
                        onClick = { /* Export P&L */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Assessment, "P&L", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("P&L Report")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun ReportCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, title, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = color, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
        }
    }
}

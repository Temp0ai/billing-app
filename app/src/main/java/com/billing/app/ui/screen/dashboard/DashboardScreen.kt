package com.billing.app.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.billing.app.ui.navigation.Screen
import com.billing.app.ui.theme.*
import com.billing.app.util.CurrencyUtils
import com.billing.app.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val todaySales by viewModel.todaySales.collectAsState()
    val monthlySales by viewModel.monthlySales.collectAsState()
    val totalOutstanding by viewModel.totalOutstanding.collectAsState()
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsState()
    val stockValue by viewModel.stockValue.collectAsState()
    val partyCount by viewModel.partyCount.collectAsState()
    val productCount by viewModel.productCount.collectAsState()
    val invoiceCount by viewModel.invoiceCount.collectAsState()
    val overdueInvoices by viewModel.overdueInvoices.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("📊 Dashboard", fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = TextOnPrimary,
                    actionIconColor = TextOnPrimary
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateInvoice.route) },
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, "Create Invoice", tint = TextOnPrimary)
            }
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

            // Quick Stats Row
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        StatCard(
                            title = "Today's Sales",
                            value = CurrencyUtils.formatIndian(todaySales),
                            icon = Icons.Default.TrendingUp,
                            color = SalesColor
                        )
                    }
                    item {
                        StatCard(
                            title = "This Month",
                            value = CurrencyUtils.formatIndian(monthlySales),
                            icon = Icons.Default.CalendarMonth,
                            color = Primary
                        )
                    }
                    item {
                        StatCard(
                            title = "Outstanding",
                            value = CurrencyUtils.formatIndian(totalOutstanding),
                            icon = Icons.Default.AccountBalance,
                            color = OutstandingColor
                        )
                    }
                    item {
                        StatCard(
                            title = "Expenses",
                            value = CurrencyUtils.formatIndian(monthlyExpenses),
                            icon = Icons.Default.Receipt,
                            color = ExpenseColor
                        )
                    }
                }
            }

            // Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Parties",
                        value = "$partyCount",
                        icon = Icons.Default.People,
                        onClick = { navController.navigate(Screen.PartyList.route) }
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Products",
                        value = "$productCount",
                        icon = Icons.Default.Inventory,
                        onClick = { navController.navigate(Screen.Inventory.route) }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Invoices",
                        value = "$invoiceCount",
                        icon = Icons.Default.Description,
                        onClick = { navController.navigate(Screen.InvoiceList.route) }
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Stock Value",
                        value = CurrencyUtils.formatIndianShort(stockValue),
                        icon = Icons.Default.Warehouse,
                        onClick = { navController.navigate(Screen.Inventory.route) }
                    )
                }
            }

            // Overdue Invoices Alert
            if (overdueInvoices.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, "Warning", tint = Error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "${overdueInvoices.size} Overdue Invoices",
                                    fontWeight = FontWeight.Bold,
                                    color = Error
                                )
                                Text(
                                    "Total: ${CurrencyUtils.formatIndian(overdueInvoices.sumOf { it.balanceAmount })}",
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Low Stock Alert
            if (lowStockProducts.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Inventory2, "Low Stock", tint = Warning)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "${lowStockProducts.size} Products Low in Stock",
                                    fontWeight = FontWeight.Bold,
                                    color = Warning
                                )
                                Text(
                                    lowStockProducts.take(3).joinToString(", ") { it.name },
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        label = "New Invoice",
                        icon = Icons.Default.NoteAdd,
                        color = Primary,
                        onClick = { navController.navigate(Screen.CreateInvoice.route) }
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        label = "Add Product",
                        icon = Icons.Default.AddBox,
                        color = Secondary,
                        onClick = { navController.navigate(Screen.AddProduct.route) }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        label = "Add Party",
                        icon = Icons.Default.PersonAdd,
                        color = Success,
                        onClick = { navController.navigate(Screen.AddParty.route) }
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        label = "Reports",
                        icon = Icons.Default.Assessment,
                        color = Info,
                        onClick = { navController.navigate(Screen.Reports.route) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(icon, title, tint = TextOnPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = TextOnPrimary.copy(alpha = 0.8f), fontSize = 12.sp)
            Text(value, color = TextOnPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, title, tint = Primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(title, color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, label, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, color = color, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Dashboard, "Dashboard") },
            label = { Text("Home") },
            selected = false,
            onClick = { navController.navigate(Screen.Dashboard.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Description, "Invoices") },
            label = { Text("Invoices") },
            selected = false,
            onClick = { navController.navigate(Screen.InvoiceList.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Inventory, "Inventory") },
            label = { Text("Stock") },
            selected = false,
            onClick = { navController.navigate(Screen.Inventory.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.People, "Parties") },
            label = { Text("Parties") },
            selected = false,
            onClick = { navController.navigate(Screen.PartyList.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Assessment, "Reports") },
            label = { Text("Reports") },
            selected = false,
            onClick = { navController.navigate(Screen.Reports.route) }
        )
    }
}

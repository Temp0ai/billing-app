package com.billing.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.billing.app.ui.screen.dashboard.DashboardScreen
import com.billing.app.ui.screen.invoice.InvoiceListScreen
import com.billing.app.ui.screen.invoice.CreateInvoiceScreen
import com.billing.app.ui.screen.invoice.InvoiceDetailScreen
import com.billing.app.ui.screen.inventory.InventoryScreen
import com.billing.app.ui.screen.inventory.AddProductScreen
import com.billing.app.ui.screen.party.PartyListScreen
import com.billing.app.ui.screen.party.AddPartyScreen
import com.billing.app.ui.screen.reports.ReportsScreen
import com.billing.app.ui.screen.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object InvoiceList : Screen("invoices")
    object CreateInvoice : Screen("create_invoice")
    object InvoiceDetail : Screen("invoice/{id}") {
        fun createRoute(id: Long) = "invoice/$id"
    }
    object Inventory : Screen("inventory")
    object AddProduct : Screen("add_product")
    object PartyList : Screen("parties")
    object AddParty : Screen("add_party")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }

        composable(Screen.InvoiceList.route) {
            InvoiceListScreen(navController)
        }

        composable(Screen.CreateInvoice.route) {
            CreateInvoiceScreen(navController)
        }

        composable(
            route = Screen.InvoiceDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0
            InvoiceDetailScreen(navController, invoiceId = id)
        }

        composable(Screen.Inventory.route) {
            InventoryScreen(navController)
        }

        composable(Screen.AddProduct.route) {
            AddProductScreen(navController)
        }

        composable(Screen.PartyList.route) {
            PartyListScreen(navController)
        }

        composable(Screen.AddParty.route) {
            AddPartyScreen(navController)
        }

        composable(Screen.Reports.route) {
            ReportsScreen(navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
    }
}

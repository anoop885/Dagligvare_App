package no.uio.ifi.in2000.dagligvareapp.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import no.uio.ifi.in2000.dagligvareapp.presentation.cart.CartScreen
import no.uio.ifi.in2000.dagligvareapp.presentation.cart.CartViewModel
import no.uio.ifi.in2000.dagligvareapp.presentation.deals.DealsScreen
import no.uio.ifi.in2000.dagligvareapp.presentation.search.SearchScreen
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String, val label: String) {
    object Search : Screen("search", "Søk")
    object Deals : Screen("deals", "Tilbud")
    object Cart : Screen("cart", "Handlekurv")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val cartViewModel: CartViewModel = koinViewModel()
    val cartSummary by cartViewModel.cartSummary.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val navItems = listOf(Screen.Search, Screen.Deals, Screen.Cart)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                Screen.Search -> Icon(Icons.Filled.Search, contentDescription = screen.label)
                                Screen.Deals -> Icon(Icons.Filled.Star, contentDescription = screen.label)
                                Screen.Cart -> BadgedBox(
                                    badge = {
                                        val count = cartSummary?.totalItems ?: 0
                                        if (count > 0) Badge { Text(count.toString()) }
                                    }
                                ) {
                                    Icon(Icons.Filled.ShoppingCart, contentDescription = screen.label)
                                }
                            }
                        },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Search.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Search.route) {
                SearchScreen(cartViewModel = cartViewModel, snackbarHostState = snackbarHostState)
            }
            composable(Screen.Deals.route) {
                DealsScreen(cartViewModel = cartViewModel)
            }
            composable(Screen.Cart.route) {
                CartScreen(viewModel = cartViewModel)
            }
        }
    }
}

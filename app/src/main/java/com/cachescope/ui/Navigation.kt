package com.cachescope.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cachescope.ui.analytics.AnalyticsScreen
import com.cachescope.ui.benchmark.BenchmarkScreen
import com.cachescope.ui.compare.CompareScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Benchmark : Screen("benchmark", "Benchmark", Icons.Default.Speed)
    data object Analytics : Screen("analytics", "Analytics", Icons.Default.BarChart)
    data object Compare   : Screen("compare",   "Race",      Icons.Default.PlayArrow)
}

val bottomNavScreens = listOf(Screen.Benchmark, Screen.Analytics, Screen.Compare)

@Composable
fun CacheScopeApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavScreens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = selected,
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
            startDestination = Screen.Benchmark.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Benchmark.route) { BenchmarkScreen() }
            composable(Screen.Analytics.route) { AnalyticsScreen() }
            composable(Screen.Compare.route)   { CompareScreen() }
        }
    }
}

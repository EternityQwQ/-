package com.thermalfaker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thermalfaker.app.ui.screens.DashboardScreen
import com.thermalfaker.app.ui.screens.InfoScreen
import com.thermalfaker.app.ui.screens.LogScreen
import com.thermalfaker.app.ui.screens.MainScreen

@Composable
fun ThermalFakerNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToInfo = { navController.navigate(Screen.Info.route) },
                onNavigateToDashboard = { navController.navigate(Screen.Dashboard.route) }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Info.route) {
            InfoScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogs = { navController.navigate(Screen.Logs.route) }
            )
        }
        composable(Screen.Logs.route) {
            LogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

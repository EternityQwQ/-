package com.thermalfaker.app.ui.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Dashboard : Screen("dashboard")
    data object Info : Screen("info")
}

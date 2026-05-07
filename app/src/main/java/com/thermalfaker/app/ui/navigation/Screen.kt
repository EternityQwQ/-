package com.thermalfaker.app.ui.navigation

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Info : Screen("info")
}

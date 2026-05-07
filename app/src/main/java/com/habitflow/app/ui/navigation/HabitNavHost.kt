package com.habitflow.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.habitflow.app.ui.screens.AddEditHabitScreen
import com.habitflow.app.ui.screens.HabitDetailScreen
import com.habitflow.app.ui.screens.HabitListScreen

@Composable
fun HabitNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HabitList.route
    ) {
        composable(Screen.HabitList.route) {
            HabitListScreen(
                onNavigateToAddHabit = {
                    navController.navigate(Screen.AddHabit.route)
                },
                onNavigateToDetail = { habitId ->
                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
                }
            )
        }

        composable(Screen.AddHabit.route) {
            AddEditHabitScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditHabit.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType }
            )
        ) {
            AddEditHabitScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.HabitDetail.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: 0L
            HabitDetailScreen(
                habitId = habitId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditHabit.createRoute(id))
                }
            )
        }
    }
}

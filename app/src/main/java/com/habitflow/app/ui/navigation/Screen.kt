package com.habitflow.app.ui.navigation

sealed class Screen(val route: String) {
    data object HabitList : Screen("habit_list")
    data object AddHabit : Screen("add_habit")
    data object EditHabit : Screen("edit_habit/{habitId}") {
        fun createRoute(habitId: Long) = "edit_habit/$habitId"
    }
    data object HabitDetail : Screen("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
}

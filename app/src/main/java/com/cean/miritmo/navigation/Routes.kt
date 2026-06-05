package com.cean.miritmo.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Habits : Screen("habits")
    object AddHabit : Screen("add_habit")
    object ManageHabit : Screen("manage_habit/{habitId}") {
        fun createRoute(habitId: String) = "manage_habit/$habitId"
    }
    object Progress : Screen("progress")
    object Profile : Screen("profile")
    object Timer : Screen("timer/{habitId}") {
        fun createRoute(habitId: String) = "timer/$habitId"
    }
    object Search : Screen("search")
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
}

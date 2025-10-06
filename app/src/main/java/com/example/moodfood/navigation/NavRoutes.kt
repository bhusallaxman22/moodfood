package com.example.moodfood.navigation

sealed class NavRoute(val route: String) {
    data object Login : NavRoute("login")
    data object Signup : NavRoute("signup")
    data object Home : NavRoute("home")
    data object Progress : NavRoute("progress")
    data object Trends : NavRoute("trends")
    data object Mindfulness : NavRoute("mindfulness")
    data object Profile : NavRoute("profile")
}

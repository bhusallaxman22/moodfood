package com.example.moodfood.navigation

sealed class NavRoute(val route: String) {
    data object Onboarding : NavRoute("onboarding")
    data object Tutorial : NavRoute("tutorial")
    data object Home : NavRoute("home")
    data object Progress : NavRoute("progress")
    data object Trends : NavRoute("trends")
    data object Mindfulness : NavRoute("mindfulness")
    data object Profile : NavRoute("profile")
}

package com.example.moodfood.navigation

sealed class NavRoute(val route: String) {
    data object Onboarding : NavRoute("onboarding")
    data object Login : NavRoute("login")
    data object Signup : NavRoute("signup")
    data object Tutorial : NavRoute("tutorial")
    data object Home : NavRoute("home")
    data object Recipes : NavRoute("Recipes")
    data object SuggestionDetail : NavRoute("suggestion_detail")
    data object Progress : NavRoute("progress")
    data object Trends : NavRoute("trends")
    data object Mindfulness : NavRoute("mindfulness")
    data object Profile : NavRoute("profile")
    data object TermsAndConditions : NavRoute("terms_and_conditions")
    data object HelpAndSupport : NavRoute("help_and_support")
    data object ChangePassword : NavRoute("change_password")
    data object PrivacySettings : NavRoute("privacy_settings")
    data object FoodPreferences : NavRoute("food_preferences")
    data object FoodPreferencesSetup : NavRoute("food_preferences_setup")
}

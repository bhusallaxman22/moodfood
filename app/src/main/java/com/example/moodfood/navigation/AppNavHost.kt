package com.example.moodfood.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moodfood.ui.components.BottomBar
import com.example.moodfood.ui.auth.OnboardingScreen
import com.example.moodfood.ui.auth.LoginScreen
import com.example.moodfood.ui.auth.SignupScreen
import com.example.moodfood.ui.onboarding.TutorialScreen
import com.example.moodfood.ui.screens.*
import com.example.moodfood.ui.suggestion.SuggestionDetailScreen
import com.example.moodfood.ui.legal.TermsAndConditionsScreen
import com.example.moodfood.ui.legal.HelpAndSupportScreen
import com.example.moodfood.ui.legal.PrivacySettingsScreen
import com.example.moodfood.ui.profile.ChangePasswordScreen

@Composable
fun AppNavHost(
    startDestination: String,
    onOnboardingComplete: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    val showBottomBar = when (destination?.route) {
        NavRoute.Home.route,
        NavRoute.Recipes.route,
        NavRoute.Progress.route,
        NavRoute.Trends.route,
        NavRoute.Mindfulness.route,
        NavRoute.Profile.route -> true
        else -> false
    }

    // THIS IS THE NAVIGATION BAR.

    Scaffold(
        bottomBar = { if (showBottomBar) BottomBar(navController) }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(inner)
        ) {
            composable(NavRoute.Onboarding.route) {
                OnboardingScreen(navController = navController)
            }
            composable(NavRoute.Login.route) {
                LoginScreen(
                    onSignInSuccess = {
                        onOnboardingComplete()
                        navController.navigate(NavRoute.Home.route) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSignup = {
                        navController.navigate(NavRoute.Signup.route)
                    }
                )
            }
            composable(NavRoute.Signup.route) {
                SignupScreen(
                    onSignUpSuccess = {
                        onOnboardingComplete()
                        navController.navigate(NavRoute.Home.route) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable(NavRoute.Tutorial.route) {
                TutorialScreen(
                    onFinish = {
                        onOnboardingComplete()
                        navController.navigate(NavRoute.Home.route) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(NavRoute.Home.route) { HomeScreen(navController) }
            composable(NavRoute.Recipes.route) { RecipesScreen(navController) }
            composable(NavRoute.SuggestionDetail.route) { SuggestionDetailScreen(navController) }
            composable(NavRoute.Progress.route) { ProgressScreen() }
            composable(NavRoute.Trends.route) { TrendsScreen() }
            composable(NavRoute.Mindfulness.route) { MindfulnessScreen() }
            composable(NavRoute.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    onSignOut = {
                        // Clear user session and navigate to login
                        navController.navigate(NavRoute.Login.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(NavRoute.TermsAndConditions.route) {
                TermsAndConditionsScreen(navController = navController)
            }
            composable(NavRoute.HelpAndSupport.route) {
                HelpAndSupportScreen(navController = navController)
            }
            composable(NavRoute.PrivacySettings.route) {
                PrivacySettingsScreen(navController = navController)
            }
            composable(NavRoute.ChangePassword.route) {
                ChangePasswordScreen(navController = navController)
            }
        }
    }
}

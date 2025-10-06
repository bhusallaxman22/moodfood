package com.example.moodfood.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moodfood.ui.components.BottomBar
import com.example.moodfood.ui.auth.LoginScreen
import com.example.moodfood.ui.auth.SignupScreen
import com.example.moodfood.ui.screens.*

@Composable
fun AppNavHost(
    startDestination: String,
    onAuthSuccess: () -> Unit,
    onSignOut: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    val showBottomBar = when (destination?.route) {
        NavRoute.Home.route,
        NavRoute.Progress.route,
        NavRoute.Trends.route,
        NavRoute.Mindfulness.route,
        NavRoute.Profile.route -> true
        else -> false
    }

    Scaffold(
        bottomBar = { if (showBottomBar) BottomBar(navController) }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(inner)
        ) {
            composable(NavRoute.Login.route) {
                LoginScreen(
                    onSignInSuccess = {
                        onAuthSuccess()
                        navController.navigate(NavRoute.Home.route) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSignup = { navController.navigate(NavRoute.Signup.route) }
                )
            }
            composable(NavRoute.Signup.route) {
                SignupScreen(
                    onSignUpSuccess = {
                        onAuthSuccess()
                        navController.navigate(NavRoute.Home.route) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = { navController.navigate(NavRoute.Login.route) }
                )
            }
            composable(NavRoute.Home.route) { HomeScreen(navController) }
            composable(NavRoute.Progress.route) { ProgressScreen() }
            composable(NavRoute.Trends.route) { TrendsScreen() }
            composable(NavRoute.Mindfulness.route) { MindfulnessScreen() }
            composable(NavRoute.Profile.route) { 
                ProfileScreen(onSignOut = onSignOut) 
            }
        }
    }
}

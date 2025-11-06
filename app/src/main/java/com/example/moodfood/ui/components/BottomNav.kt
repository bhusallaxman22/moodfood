package com.example.moodfood.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItemDefaults.colors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.moodfood.R
import com.example.moodfood.navigation.NavRoute

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem(NavRoute.Home.route, "Home", Icons.Filled.Home),
        BottomNavItem(NavRoute.Recipes.route, "Recipes", Icons.Filled.List),
        BottomNavItem(NavRoute.Progress.route, "Progress", Icons.Filled.AutoGraph),
        BottomNavItem(NavRoute.Trends.route, "Trends", Icons.Filled.QueryStats),
        BottomNavItem(NavRoute.Mindfulness.route, "Mindful", Icons.Filled.Psychology),
        BottomNavItem(NavRoute.Profile.route, "Profile", Icons.Filled.AccountCircle),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination.isTopLevelDestination(item.route),
                onClick = {
                    if (!currentDestination.isTopLevelDestination(item.route)) {
                        navController.navigate(item.route) {
                            popUpTo(NavRoute.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { androidx.compose.material3.Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors()
            )
        }
    }
}

private fun NavDestination?.isTopLevelDestination(route: String): Boolean =
    this?.hierarchy?.any { it.route == route } == true

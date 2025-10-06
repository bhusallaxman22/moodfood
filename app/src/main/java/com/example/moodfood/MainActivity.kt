package com.example.moodfood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.moodfood.data.auth.AuthRepository
import com.example.moodfood.navigation.AppNavHost
import com.example.moodfood.navigation.NavRoute
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.moodfood.ui.theme.MoodFoodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoodFoodTheme {
                val authRepo = AuthRepository(this) //The security manager
                
                val isAuthenticated by authRepo.isAuthenticated().collectAsState(initial = false)
                
                // Determine start destination based on authentication state only
                val startDestination = when {
                    !isAuthenticated -> NavRoute.Login.route //Not logged in? -> go to login
                    else -> NavRoute.Home.route //Authenticated? -> go directly to main app
                }
                
                AppNavHost(
                    startDestination = startDestination, // where to start
                    onAuthSuccess = {
                        // when user logs in, this triggers
                        // the app automatically updates because of the Flow
                    },
                    onSignOut = {
                        lifecycleScope.launch {
                            authRepo.signOut()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MoodFoodTheme {
        Greeting("Android")
    }
}
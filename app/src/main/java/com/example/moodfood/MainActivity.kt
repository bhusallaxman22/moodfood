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
import com.example.moodfood.data.SettingsRepository
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
            // 1. Initialize the repository and state directly here
            val repo = SettingsRepository(this@MainActivity)
            val done by repo.onboardingDone.collectAsState(initial = false)
            val darkModeEnabled by repo.darkModeEnabled.collectAsState(initial = false)

            // 2. Use the standard MoodFoodTheme (which is imported)
            // Pass the repository value to the darkTheme parameter
            MoodFoodTheme(
                darkTheme = darkModeEnabled
            ) {
                val start = if (done) NavRoute.Home.route else NavRoute.Onboarding.route
                AppNavHost(
                    startDestination = start,
                    onOnboardingComplete = {
                        lifecycleScope.launch { repo.setOnboardingDone(true) }
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

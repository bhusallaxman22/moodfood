package com.example.moodfood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moodfood.ui.home.SuggestionCache
import com.example.moodfood.ui.recipes.RecipesViewModel
import com.example.moodfood.navigation.NavRoute

@Composable
fun RecipesScreen(
    navController: NavController,
    viewModel: RecipesViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Saved", "Favorites")
    
    val allRecipes by viewModel.allRecipes.collectAsState()
    val savedRecipes by viewModel.savedRecipes.collectAsState()
    val favoriteRecipes by viewModel.favoriteRecipes.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Header
        Text(
            text = "📚 My Recipes",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                )
            }
        }
        
        // Content
        val currentList = when (selectedTab) {
            0 -> allRecipes
            1 -> savedRecipes
            2 -> favoriteRecipes
            else -> emptyList()
        }
        
        if (currentList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = when (selectedTab) {
                            0 -> "🍽️"
                            1 -> "📌"
                            2 -> "❤️"
                            else -> "📚"
                        },
                        fontSize = 64.sp
                    )
                    Text(
                        text = when (selectedTab) {
                            0 -> "No Recipes Yet"
                            1 -> "No Saved Recipes"
                            2 -> "No Favorite Recipes"
                            else -> "No Recipes"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = when (selectedTab) {
                            0 -> "Get personalized suggestions from the Home screen"
                            1 -> "Saved recipes will appear here"
                            2 -> "Your favorite recipes will appear here"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                currentList.forEach { recipe ->
                    RecipeCard(
                        name = recipe.name,
                        mood = recipe.mood,
                        timestamp = recipe.timestamp,
                        onClick = {
                            viewModel.loadRecipe(recipe, navController)
                        }
                    )
                }
                
                // Bottom spacing for nav bar
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun RecipeCard(
    name: String,
    mood: String,
    timestamp: Long,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji based on mood
            Text(
                text = when (mood.lowercase()) {
                    "happy" -> "😊"
                    "calm" -> "😌"
                    "low" -> "😔"
                    "anxious" -> "😰"
                    "stressed" -> "😫"
                    "irritable" -> "😤"
                    "fatigued" -> "🥱"
                    else -> "🍽️"
                },
                fontSize = 32.sp
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "For $mood mood",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                
                Text(
                    text = formatTimestamp(timestamp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> "${diff / 604_800_000}w ago"
    }
}

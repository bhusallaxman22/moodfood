package com.example.moodfood.ui.suggestion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import com.example.moodfood.ui.home.HomeViewModel
import com.example.moodfood.ui.home.SuggestionCache
import com.example.moodfood.data.models.NutritionSuggestion
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionDetailScreen(
    navController: NavController,
    viewModel: SuggestionDetailViewModel = viewModel()
) {
    val suggestion = SuggestionCache.currentSuggestion
    val suggestionId = SuggestionCache.currentSuggestionId
    val state by viewModel.state.collectAsState()
    
    // Load status when screen loads
    LaunchedEffect(suggestionId) {
        suggestionId?.let {
            viewModel.loadSuggestionStatus(it)
        }
    }
    
    Log.d("SuggestionDetail", "Loaded suggestion: ${suggestion?.meal?.name ?: "null"} (ID: $suggestionId)")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = suggestion?.meal?.name ?: "Nutrition Suggestion",
                        maxLines = 1
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (suggestionId != null) {
                        // Favorite button
                        IconButton(
                            onClick = { viewModel.toggleFavorite(suggestionId) }
                        ) {
                            Icon(
                                imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (state.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (state.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Save button
                        IconButton(
                            onClick = { viewModel.toggleSaved(suggestionId) }
                        ) {
                            Icon(
                                imageVector = if (state.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = if (state.isSaved) "Remove from saved" else "Save recipe",
                                tint = if (state.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        suggestion?.let { suggestion ->
            SuggestionDetailContent(
                suggestion = suggestion,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            )
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("No suggestion available")
                    Text(
                        "Please go back and generate a suggestion first",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionDetailContent(
    suggestion: NutritionSuggestion,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero section with title and emoji
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = suggestion.emoji,
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = suggestion.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Meal Information
        SectionCard(
            title = "🍽️ Meal Details",
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = suggestion.meal.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = suggestion.meal.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = suggestion.meal.prepTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Ingredients
        if (suggestion.ingredients.isNotEmpty()) {
            SectionCard(
                title = "🥗 Ingredients & Benefits",
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                suggestion.ingredients.forEach { ingredient ->
                    IngredientItem(ingredient = ingredient)
                    if (ingredient != suggestion.ingredients.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
        
        // Timing
        SectionCard(
            title = "⏰ Best Time to Eat",
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = suggestion.timing.`when`,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = suggestion.timing.why,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Preparation Steps
        if (suggestion.preparation.isNotEmpty()) {
            SectionCard(
                title = "👨‍🍳 Preparation Steps",
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            ) {
                suggestion.preparation.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Card(
                            modifier = Modifier.size(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (index < suggestion.preparation.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
        
        // Tips
        if (suggestion.tips.isNotEmpty()) {
            SectionCard(
                title = "💡 Pro Tips",
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            ) {
                suggestion.tips.forEach { tip ->
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (tip != suggestion.tips.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // Nutrition Information
        SectionCard(
            title = "📊 Nutrition Information",
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Text(
                text = suggestion.nutrition.calories,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            if (suggestion.nutrition.mainNutrients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Key Nutrients:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = suggestion.nutrition.mainNutrients.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    color: androidx.compose.ui.graphics.Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun IngredientItem(
    ingredient: com.example.moodfood.data.models.Ingredient
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = ingredient.emoji,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = ingredient.benefit,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

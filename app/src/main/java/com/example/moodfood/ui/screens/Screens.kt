package com.example.moodfood.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moodfood.data.db.AppDatabase
import com.example.moodfood.data.db.SuggestionDao
import com.example.moodfood.ui.home.HomeViewModel
import com.example.moodfood.data.models.NutritionSuggestion
import com.example.moodfood.data.db.SuggestionEntity
import com.example.moodfood.data.models.Ingredient
import com.example.moodfood.data.models.Meal
import com.example.moodfood.data.models.Nutrition
import com.example.moodfood.data.models.Timing
import com.example.moodfood.navigation.NavRoute
import com.example.moodfood.ui.home.HomeUiState
import com.example.moodfood.ui.home.SuggestionCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(navController: NavController) {
    val vm: HomeViewModel = viewModel()
    val state by vm.state.collectAsState()
    var showAdvanced by remember { mutableStateOf(false) }

    val moods = listOf(
        "😀 Happy", "🙂 Calm", "😔 Low", "😬 Anxious", "😤 Irritable", "😩 Stressed", "🥱 Fatigued"
    )
    val positiveMoods = listOf("😀 Happy", "🙂 Calm", "😌 Content", "💪 Energetic")
    val symptomsAll = listOf("Anxious", "Stressed", "Fatigued", "Low", "Irritable", "Brain Fog", "Restless")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "MoodFood",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "How are you feeling today?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Current Mood Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎭",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Current Mood",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                EmojiChips(items = moods, selected = state.mood, onSelect = vm::setMood)
            }
        }

        // Goal Mood Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎯",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Goal Mood",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                EmojiChips(items = positiveMoods, selected = state.goal, onSelect = vm::setGoal)
            }
        }

        // Advanced Options
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { showAdvanced = !showAdvanced }) {
                        Text(
                            if (showAdvanced) "▼ Hide Advanced Options" else "▶ Show Advanced Options",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                if (showAdvanced) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Additional Symptoms",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SymptomChips(
                        all = symptomsAll,
                        selected = state.symptoms.toSet(),
                        onToggle = { tag ->
                            val next = state.symptoms.toMutableList().also {
                                if (it.contains(tag)) it.remove(tag) else it.add(tag)
                            }
                            vm.setSymptoms(next)
                        }
                    )
                }
            }
        }

        // Get Suggestion Button
        Button(
            onClick = { vm.getSuggestion(navController) },
            enabled = state.mood.isNotBlank() && !state.loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (state.loading) "Generating..." else "✨ Get Personalized Suggestion",
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (state.loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }

        state.error?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Add bottom spacing for nav bar
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomChips(all: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        all.forEach { tag ->
            val chosen = tag in selected
            FilterChip(
                selected = chosen,
                onClick = { onToggle(tag) },
                label = {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = chosen,
                    borderColor = if (chosen) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    borderWidth = if (chosen) 2.dp else 1.dp
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmojiChips(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { label ->
            val chosen = label == selected
            val emoji = label.split(" ").firstOrNull() ?: ""
            val text = label.substringAfter(" ").trim()

            ElevatedCard(
                onClick = { onSelect(label) },
                modifier = Modifier
                    .defaultMinSize(minWidth = 100.dp)
                    .then(
                        if (chosen) Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.medium
                        ) else Modifier
                    ),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = if (chosen) 8.dp else 2.dp
                ),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (chosen)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 32.sp
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal,
                        color = if (chosen)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionPreviewCard(
    suggestion: NutritionSuggestion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = suggestion.emoji,
                    style = MaterialTheme.typography.headlineMedium
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suggestion.meal.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = suggestion.meal.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏱️ ${suggestion.meal.prepTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "Tap to view details →",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RecentSuggestionCard(
    entity: SuggestionEntity,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val date = dateFormat.format(Date(entity.timestamp))

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon/Emoji Section
            Card(
                modifier = Modifier.size(56.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🍽️",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            // Content Section
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${entity.mood} → ${entity.goal ?: "better"}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "Tap to view details →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun NutritionSuggestionCard(suggestion: NutritionSuggestion) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title and emoji - always visible
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(suggestion.emoji, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    suggestion.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (isExpanded) "▼" else "▶",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Meal info - always visible
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(suggestion.meal.name, style = MaterialTheme.typography.titleMedium)
                    Text(suggestion.meal.description, style = MaterialTheme.typography.bodyMedium)
                    Text("Prep time: ${suggestion.meal.prepTime}", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Expandable content
            if (isExpanded) {
                // Ingredients
                if (suggestion.ingredients.isNotEmpty()) {
                    Text("Ingredients", style = MaterialTheme.typography.titleMedium)
                    suggestion.ingredients.forEach { ingredient ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(ingredient.emoji, modifier = Modifier.padding(end = 8.dp))
                            Column {
                                Text(ingredient.name, style = MaterialTheme.typography.bodyMedium)
                                Text(ingredient.benefit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    Text("No ingredients provided", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Timing
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Best Time", style = MaterialTheme.typography.titleSmall)
                        Text("${suggestion.timing.`when`} - ${suggestion.timing.why}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Preparation steps
                if (suggestion.preparation.isNotEmpty()) {
                    Text("Preparation", style = MaterialTheme.typography.titleMedium)
                    suggestion.preparation.forEachIndexed { index, step ->
                        Text("${index + 1}. $step", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Text("No preparation steps provided", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Tips
                if (suggestion.tips.isNotEmpty()) {
                    Text("Tips", style = MaterialTheme.typography.titleMedium)
                    suggestion.tips.forEach { tip ->
                        Text("• $tip", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Text("No tips provided", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Nutrition info
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Nutrition", style = MaterialTheme.typography.titleSmall)
                        Text(suggestion.nutrition.calories, style = MaterialTheme.typography.bodyMedium)
                        if (suggestion.nutrition.mainNutrients.isNotEmpty()) {
                            Text("Key nutrients: ${suggestion.nutrition.mainNutrients.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text("No nutrient information provided", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                // Collapsed state hint
                Text(
                    "Tap to see ingredients, preparation steps, and nutrition details",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SuggestionList(items: List<NutritionSuggestion>) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { suggestion ->
            NutritionSuggestionCard(suggestion)
        }
    }
}

@Composable
fun TrendsScreen() {
    CenterText("Trends")
}

@Composable
fun MindfulnessScreen() {
    CenterText("Mindfulness")
}

@Composable
private fun ProfileMenuItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFFF8FAFC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        androidx.compose.ui.graphics.Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = androidx.compose.ui.graphics.Color(0xFF2D3748)
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = androidx.compose.ui.graphics.Color(0xFF718096)
                    )
                )
            }
            
            Text(
                text = "→",
                fontSize = 20.sp,
                color = androidx.compose.ui.graphics.Color(0xFF718096)
            )
        }
    }
}

@Composable
private fun CenterText(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, style = MaterialTheme.typography.headlineMedium)
    }
}


fun parseNutritionSug(raw: String): NutritionSuggestion {
    // Extract content from OpenRouter response format
    val actualJson = if (raw.contains("\"choices\"")) {
        val responseObj = JSONObject(raw)
        val choices = responseObj.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.optJSONObject("message")
            message?.optString("content") ?: raw
        } else raw
    } else raw

    Log.d("OpenRouter", "Extracted JSON: $actualJson")

    // Clean the JSON response
    var cleanedText = actualJson.trim()

    // Remove markdown code blocks if present
    cleanedText = cleanedText.replace(Regex("```json\\s*"), "").replace(Regex("```\\s*$"), "")

    // Remove any potential explanatory text before/after JSON
    cleanedText = cleanedText.replace(Regex("^[^{]*"), "").replace(Regex("[^}]*$"), "")

    // Find JSON object boundaries
    val startIndex = cleanedText.indexOf('{')
    val lastIndex = cleanedText.lastIndexOf('}')

    if (startIndex != -1 && lastIndex != -1 && startIndex < lastIndex) {
        cleanedText = cleanedText.substring(startIndex, lastIndex + 1)
    }

    Log.d("OpenRouter", "Cleaned JSON: $cleanedText")

    val jsonObj = JSONObject(cleanedText)

    return NutritionSuggestion(
        title = jsonObj.optString("title", "Mood-Boosting Nutrition"),
        emoji = jsonObj.optString("emoji", "🌟"),
        meal = jsonObj.optJSONObject("meal")?.let { mealObj ->
            Meal(
                name = mealObj.optString("name", "Healthy Meal"),
                description = mealObj.optString("description", "A nutritious meal for your mood"),
                prepTime = mealObj.optString("prepTime", "10 minutes")
            )
        } ?: Meal("Healthy Meal", "A nutritious meal for your mood", "10 minutes"),
        ingredients = jsonObj.optJSONArray("ingredients")?.let { ingredientsArray ->
            (0 until ingredientsArray.length()).map { i ->
                val ingObj = ingredientsArray.getJSONObject(i)
                Ingredient(
                    name = ingObj.optString("name", "Healthy ingredient"),
                    benefit = ingObj.optString("benefit", "Good for your health"),
                    emoji = ingObj.optString("emoji", "🥗")
                )
            }
        } ?: emptyList(),
        timing = jsonObj.optJSONObject("timing")?.let { timingObj ->
            Timing(
                `when` = timingObj.optString("when", "Anytime"),
                why = timingObj.optString("why", "Good for your mood")
            )
        } ?: Timing("Anytime", "Good for your mood"),
        preparation = jsonObj.optJSONArray("preparation")?.let { prepArray ->
            (0 until prepArray.length()).map { prepArray.getString(it) }
        } ?: emptyList(),
        tips = jsonObj.optJSONArray("tips")?.let { tipsArray ->
            (0 until tipsArray.length()).map { tipsArray.getString(it) }
        } ?: emptyList(),
        nutrition = jsonObj.optJSONObject("nutrition")?.let { nutritionObj ->
            Nutrition(
                calories = nutritionObj.optString("calories", "300-400 calories"),
                mainNutrients = nutritionObj.optJSONArray("mainNutrients")?.let { nutrientsArray ->
                    (0 until nutrientsArray.length()).map { nutrientsArray.getString(it) }
                } ?: emptyList()
            )
        } ?: Nutrition("300-400 calories", emptyList())
    )
}

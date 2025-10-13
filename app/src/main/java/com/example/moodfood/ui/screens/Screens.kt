package com.example.moodfood.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moodfood.ui.home.HomeViewModel
import com.example.moodfood.data.models.NutritionSuggestion
import com.example.moodfood.data.db.SuggestionEntity
import com.example.moodfood.navigation.NavRoute
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(navController: NavController) {
    val vm: HomeViewModel = viewModel()
    val state by vm.state.collectAsState()
    var showAdvanced by remember { mutableStateOf(false) }

    val moods = listOf(
        "😀 happy", "🙂 calm", "😔 low", "😬 anxious", "😤 irritable", "😩 stressed", "🥱 fatigued"
    )
    val positiveMoods = listOf("😀 happy", "🙂 calm", "😌 content", "💪 energetic")
    val symptomsAll = listOf("anxious", "stressed", "fatigued", "low", "irritable", "brain fog", "restless")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Current mood", style = MaterialTheme.typography.titleMedium)
            EmojiChips(items = moods, selected = state.mood, onSelect = vm::setMood)
        }}
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Goal mood", style = MaterialTheme.typography.titleMedium)
            EmojiChips(items = positiveMoods, selected = state.goal, onSelect = vm::setGoal)
        }}
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { showAdvanced = !showAdvanced }) {
                Text(if (showAdvanced) "Hide advanced" else "Advanced")
            }
        }
    if (showAdvanced) {
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
        Button(onClick = { vm.getSuggestion(navController) }, enabled = state.mood.isNotBlank() && !state.loading, modifier = Modifier.align(Alignment.End)) {
            Text("Get suggestion")
        }
        if (state.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        
        // Suggestion preview card removed - now navigates directly to detail screen
        // when suggestion is loaded
        
        if (state.recent.isNotEmpty()) {
            Text("Recent suggestions", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.recent.forEach { entity ->
                    RecentSuggestionCard(
                        entity = entity,
                        onClick = {
                            // Try to parse and cache the suggestion for detail view
                            vm.loadRecentSuggestion(entity, navController)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SymptomChips(all: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        all.forEach { tag ->
            val chosen = tag in selected
            FilterChip(
                selected = chosen,
                onClick = { onToggle(tag) },
                label = { Text(tag) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmojiChips(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { label ->
            val chosen = label == selected
            FilterChip(
                selected = chosen,
                onClick = { onSelect(label) },
                label = { Text(label) }
            )
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
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "From ${entity.mood} to ${entity.goal ?: "feel better"}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "👁️",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Text(
                text = "Tap to view suggestion details",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
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
fun ProgressScreen() {
    CenterText("Progress")
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
fun ProfileScreen(
    navController: NavController? = null,
    onSignOut: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        
        // User info section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Account Settings", style = MaterialTheme.typography.titleMedium)
                Text("Manage your account preferences", style = MaterialTheme.typography.bodyMedium)
            }
        }
        
        // Settings options
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Settings", style = MaterialTheme.typography.titleMedium)
                
                OutlinedButton(
                    onClick = { /* TODO: Edit profile */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Profile")
                }
                
                OutlinedButton(
                    onClick = { /* TODO: Change password */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Change Password")
                }
                
                OutlinedButton(
                    onClick = { /* TODO: Privacy settings */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Privacy Settings")
                }
            }
        }
        
        // Logout button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Sign Out", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onSignOut()
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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

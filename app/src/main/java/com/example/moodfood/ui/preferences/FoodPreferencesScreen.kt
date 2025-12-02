package com.example.moodfood.ui.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.moodfood.data.preferences.*
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodPreferencesScreen(
    navController: NavController? = null,
    isFirstTimeSetup: Boolean = false,
    onSetupComplete: () -> Unit = {},
    viewModel: FoodPreferencesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableStateOf(0) }
    
    // Show success message
    LaunchedEffect(uiState.showSuccessMessage) {
        if (uiState.showSuccessMessage) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearSuccessMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (isFirstTimeSetup) "Food Preferences Setup" else "Food Preferences",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                if (!isFirstTimeSetup) {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )

        if (isFirstTimeSetup) {
            // Multi-step setup flow
            when (currentStep) {
                0 -> DietaryRestrictionsStep(
                    selected = uiState.selectedDietaryRestrictions,
                    onToggle = { viewModel.toggleDietaryRestriction(it) },
                    onNext = { currentStep = 1 }
                )
                1 -> AllergiesStep(
                    allergies = uiState.selectedAllergies,
                    onAdd = { viewModel.addAllergy(it) },
                    onRemove = { viewModel.removeAllergy(it) },
                    onNext = { currentStep = 2 },
                    onBack = { currentStep = 0 }
                )
                2 -> PreferencesStep(
                    spiceLevel = uiState.spiceLevel,
                    mealComplexity = uiState.mealComplexity,
                    budget = uiState.budget,
                    onSpiceLevelChange = { viewModel.updateSpiceLevel(it) },
                    onComplexityChange = { viewModel.updateMealComplexity(it) },
                    onBudgetChange = { viewModel.updateBudget(it) },
                    onNext = { currentStep = 3 },
                    onBack = { currentStep = 1 }
                )
                3 -> CuisinesStep(
                    selected = uiState.preferredCuisines,
                    onToggle = { viewModel.toggleCuisine(it) },
                    onFinish = {
                        viewModel.savePreferences {
                            onSetupComplete()
                        }
                    },
                    onBack = { currentStep = 2 },
                    isSaving = uiState.isSaving
                )
            }
        } else {
            // Full preferences screen
            FullPreferencesScreen(
                uiState = uiState,
                viewModel = viewModel,
                onSave = {
                    viewModel.savePreferences {
                        navController?.popBackStack()
                    }
                }
            )
        }
    }
}

@Composable
private fun DietaryRestrictionsStep(
    selected: List<String>,
    onToggle: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🥗 Dietary Restrictions",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        Text(
            text = "Select any dietary restrictions you follow",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        DietaryRestriction.values().forEach { restriction ->
            SelectableChip(
                label = restriction.label,
                isSelected = selected.contains(restriction.name.lowercase()),
                onClick = { onToggle(restriction.name.lowercase()) }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Next",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun AllergiesStep(
    allergies: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var allergyInput by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "⚠️ Allergies & Foods to Avoid",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        Text(
            text = "Add any food allergies or ingredients you want to avoid",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Input field
        OutlinedTextField(
            value = allergyInput,
            onValueChange = { allergyInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Add allergy or food to avoid") },
            placeholder = { Text("e.g., Peanuts, Shellfish, Soy") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (allergyInput.isNotBlank()) {
                            onAdd(allergyInput.trim())
                            allergyInput = ""
                        }
                    }
                ) {
                    Icon(Icons.Filled.Add, "Add")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (allergyInput.isNotBlank()) {
                        onAdd(allergyInput.trim())
                        allergyInput = ""
                    }
                }
            ),
            singleLine = true
        )
        
        // Display added allergies
        if (allergies.isNotEmpty()) {
            FlowRow(
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                allergies.forEach { allergy ->
                    RemovableChip(
                        label = allergy,
                        onRemove = { onRemove(allergy) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back")
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun PreferencesStep(
    spiceLevel: String,
    mealComplexity: String,
    budget: String,
    onSpiceLevelChange: (String) -> Unit,
    onComplexityChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "⚙️ Your Preferences",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        Text(
            text = "Tell us about your cooking style",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        
        // Spice Level
        PreferenceSectionTitle("🌶️ Spice Level")
        SpiceLevel.values().forEach { level ->
            SelectableChip(
                label = "${level.emoji} ${level.label}",
                isSelected = spiceLevel == level.name.lowercase(),
                onClick = { onSpiceLevelChange(level.name.lowercase()) }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Meal Complexity
        PreferenceSectionTitle("👨‍🍳 Meal Complexity")
        MealComplexity.values().forEach { complexity ->
            SelectableChip(
                label = "${complexity.label} - ${complexity.description}",
                isSelected = mealComplexity == complexity.name.lowercase(),
                onClick = { onComplexityChange(complexity.name.lowercase()) }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Budget
        PreferenceSectionTitle("💰 Budget")
        Budget.values().forEach { budgetLevel ->
            SelectableChip(
                label = "${budgetLevel.emoji} ${budgetLevel.label}",
                isSelected = budget == budgetLevel.name.lowercase(),
                onClick = { onBudgetChange(budgetLevel.name.lowercase()) }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back")
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun CuisinesStep(
    selected: List<String>,
    onToggle: (String) -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit,
    isSaving: Boolean
) {
    val cuisines = listOf(
        "Italian" to "🇮🇹",
        "Asian" to "🍜",
        "Mexican" to "🌮",
        "Mediterranean" to "🫒",
        "American" to "🍔",
        "Indian" to "🍛",
        "Chinese" to "🥡",
        "Japanese" to "🍱",
        "Thai" to "🌶️",
        "French" to "🇫🇷",
        "Greek" to "🇬🇷",
        "Middle Eastern" to "🥙"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🌍 Favorite Cuisines",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        Text(
            text = "Choose your preferred cuisines (optional)",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        cuisines.forEach { (cuisine, emoji) ->
            SelectableChip(
                label = "$emoji $cuisine",
                isSelected = selected.contains(cuisine.lowercase()),
                onClick = { onToggle(cuisine.lowercase()) }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving
            ) {
                Text("Back")
            }
            
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Finish")
                }
            }
        }
    }
}

@Composable
private fun FullPreferencesScreen(
    uiState: FoodPreferencesUiState,
    viewModel: FoodPreferencesViewModel,
    onSave: () -> Unit
) {
    var allergyInput by remember { mutableStateOf("") }
    var dislikedFoodInput by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dietary Restrictions
            PreferenceSectionTitle("🥗 Dietary Restrictions")
            FlowRow(
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                DietaryRestriction.values().forEach { restriction ->
                    FilterChip(
                        selected = uiState.selectedDietaryRestrictions.contains(restriction.name.lowercase()),
                        onClick = { viewModel.toggleDietaryRestriction(restriction.name.lowercase()) },
                        label = { Text(restriction.label) }
                    )
                }
            }
            
            // Allergies
            PreferenceSectionTitle("⚠️ Allergies")
            OutlinedTextField(
                value = allergyInput,
                onValueChange = { allergyInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Add allergy") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (allergyInput.isNotBlank()) {
                                viewModel.addAllergy(allergyInput.trim())
                                allergyInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Add, "Add")
                    }
                },
                singleLine = true
            )
            if (uiState.selectedAllergies.isNotEmpty()) {
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    uiState.selectedAllergies.forEach { allergy ->
                        RemovableChip(label = allergy, onRemove = { viewModel.removeAllergy(allergy) })
                    }
                }
            }
            
            // Disliked Foods
            PreferenceSectionTitle("👎 Foods to Avoid")
            OutlinedTextField(
                value = dislikedFoodInput,
                onValueChange = { dislikedFoodInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Add food to avoid") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (dislikedFoodInput.isNotBlank()) {
                                viewModel.addDislikedFood(dislikedFoodInput.trim())
                                dislikedFoodInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Add, "Add")
                    }
                },
                singleLine = true
            )
            if (uiState.dislikedFoods.isNotEmpty()) {
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    uiState.dislikedFoods.forEach { food ->
                        RemovableChip(label = food, onRemove = { viewModel.removeDislikedFood(food) })
                    }
                }
            }
            
            // Other preferences sections...
            PreferenceSectionTitle("🌶️ Spice Level")
            FlowRow(
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                SpiceLevel.values().forEach { level ->
                    FilterChip(
                        selected = uiState.spiceLevel == level.name.lowercase(),
                        onClick = { viewModel.updateSpiceLevel(level.name.lowercase()) },
                        label = { Text("${level.emoji} ${level.label}") }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
        
        // Save button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Preferences", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun PreferenceSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SelectableChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun RemovableChip(
    label: String,
    onRemove: () -> Unit
) {
    InputChip(
        selected = false,
        onClick = onRemove,
        label = { Text(label) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(18.dp)
            )
        }
    )
}

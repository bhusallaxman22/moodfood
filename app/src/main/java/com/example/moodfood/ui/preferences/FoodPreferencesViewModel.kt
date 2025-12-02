package com.example.moodfood.ui.preferences

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodfood.data.auth.AuthRepository
import com.example.moodfood.data.preferences.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

data class FoodPreferencesUiState(
    val preferences: FoodPreferences? = null,
    val selectedDietaryRestrictions: List<String> = emptyList(),
    val selectedAllergies: List<String> = emptyList(),
    val dislikedFoods: List<String> = emptyList(),
    val preferredCuisines: List<String> = emptyList(),
    val spiceLevel: String = "medium",
    val mealComplexity: String = "medium",
    val cookingTime: String = "30",
    val servings: Int = 1,
    val budget: String = "medium",
    val organicPreference: Boolean = false,
    val localPreference: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val showSuccessMessage: Boolean = false
)

class FoodPreferencesViewModel(app: Application) : AndroidViewModel(app) {
    private val preferencesRepository = FoodPreferencesRepository(app)
    private val authRepository = AuthRepository.get(app)
    
    private val _uiState = MutableStateFlow(FoodPreferencesUiState())
    val uiState: StateFlow<FoodPreferencesUiState> = _uiState.asStateFlow()
    
    init {
        loadPreferences()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    preferencesRepository.getUserPreferences().collect { prefs ->
                        if (prefs != null) {
                            _uiState.value = _uiState.value.copy(
                                preferences = prefs,
                                selectedDietaryRestrictions = prefs.dietaryRestrictions.split(",").filter { it.isNotBlank() },
                                selectedAllergies = prefs.allergies.split(",").filter { it.isNotBlank() },
                                dislikedFoods = prefs.dislikedFoods.split(",").filter { it.isNotBlank() },
                                preferredCuisines = prefs.preferredCuisines.split(",").filter { it.isNotBlank() },
                                spiceLevel = prefs.spiceLevel,
                                mealComplexity = prefs.mealComplexity,
                                cookingTime = prefs.cookingTime,
                                servings = prefs.servings,
                                budget = prefs.budget,
                                organicPreference = prefs.organicPreference,
                                localPreference = prefs.localPreference,
                                isLoading = false
                            )
                        } else {
                            // Create default preferences
                            val defaultPrefs = preferencesRepository.createDefaultPreferences(user.id)
                            _uiState.value = _uiState.value.copy(
                                preferences = defaultPrefs,
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FoodPreferencesVM", "Error loading preferences: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load preferences"
                )
            }
        }
    }
    
    fun toggleDietaryRestriction(restriction: String) {
        val current = _uiState.value.selectedDietaryRestrictions.toMutableList()
        if (current.contains(restriction)) {
            current.remove(restriction)
        } else {
            current.add(restriction)
        }
        _uiState.value = _uiState.value.copy(selectedDietaryRestrictions = current)
    }
    
    fun addAllergy(allergy: String) {
        if (allergy.isBlank()) return
        val current = _uiState.value.selectedAllergies.toMutableList()
        if (!current.contains(allergy)) {
            current.add(allergy)
            _uiState.value = _uiState.value.copy(selectedAllergies = current)
        }
    }
    
    fun removeAllergy(allergy: String) {
        val current = _uiState.value.selectedAllergies.toMutableList()
        current.remove(allergy)
        _uiState.value = _uiState.value.copy(selectedAllergies = current)
    }
    
    fun addDislikedFood(food: String) {
        if (food.isBlank()) return
        val current = _uiState.value.dislikedFoods.toMutableList()
        if (!current.contains(food)) {
            current.add(food)
            _uiState.value = _uiState.value.copy(dislikedFoods = current)
        }
    }
    
    fun removeDislikedFood(food: String) {
        val current = _uiState.value.dislikedFoods.toMutableList()
        current.remove(food)
        _uiState.value = _uiState.value.copy(dislikedFoods = current)
    }
    
    fun toggleCuisine(cuisine: String) {
        val current = _uiState.value.preferredCuisines.toMutableList()
        if (current.contains(cuisine)) {
            current.remove(cuisine)
        } else {
            current.add(cuisine)
        }
        _uiState.value = _uiState.value.copy(preferredCuisines = current)
    }
    
    fun updateSpiceLevel(level: String) {
        _uiState.value = _uiState.value.copy(spiceLevel = level)
    }
    
    fun updateMealComplexity(complexity: String) {
        _uiState.value = _uiState.value.copy(mealComplexity = complexity)
    }
    
    fun updateCookingTime(time: String) {
        _uiState.value = _uiState.value.copy(cookingTime = time)
    }
    
    fun updateServings(servings: Int) {
        _uiState.value = _uiState.value.copy(servings = servings.coerceIn(1, 10))
    }
    
    fun updateBudget(budget: String) {
        _uiState.value = _uiState.value.copy(budget = budget)
    }
    
    fun toggleOrganicPreference() {
        _uiState.value = _uiState.value.copy(organicPreference = !_uiState.value.organicPreference)
    }
    
    fun toggleLocalPreference() {
        _uiState.value = _uiState.value.copy(localPreference = !_uiState.value.localPreference)
    }
    
    fun savePreferences(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true)
                
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    val preferences = FoodPreferences(
                        userId = user.id,
                        dietaryRestrictions = _uiState.value.selectedDietaryRestrictions.joinToString(","),
                        allergies = _uiState.value.selectedAllergies.joinToString(","),
                        dislikedFoods = _uiState.value.dislikedFoods.joinToString(","),
                        preferredCuisines = _uiState.value.preferredCuisines.joinToString(","),
                        spiceLevel = _uiState.value.spiceLevel,
                        mealComplexity = _uiState.value.mealComplexity,
                        cookingTime = _uiState.value.cookingTime,
                        servings = _uiState.value.servings,
                        budget = _uiState.value.budget,
                        organicPreference = _uiState.value.organicPreference,
                        localPreference = _uiState.value.localPreference,
                        isSetupComplete = true,
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    preferencesRepository.savePreferences(preferences)
                    
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        showSuccessMessage = true,
                        preferences = preferences
                    )
                    
                    Log.d("FoodPreferencesVM", "Preferences saved successfully")
                    onComplete()
                }
            } catch (e: Exception) {
                Log.e("FoodPreferencesVM", "Error saving preferences: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to save preferences"
                )
            }
        }
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

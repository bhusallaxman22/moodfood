package com.example.moodfood.ui.recipes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.moodfood.BuildConfig
import com.example.moodfood.data.ai.OpenRouterClientFactory
import com.example.moodfood.data.ai.SuggestionsRepository
import com.example.moodfood.data.db.AppDatabase
import com.example.moodfood.data.db.SuggestionEntity
import com.example.moodfood.navigation.NavRoute
import com.example.moodfood.ui.home.SuggestionCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class RecipesViewModel(app: Application) : AndroidViewModel(app) {
    private val suggestionDao = AppDatabase.get(app).suggestionDao()
    
    private val _allRecipes = MutableStateFlow<List<SuggestionEntity>>(emptyList())
    val allRecipes: StateFlow<List<SuggestionEntity>> = _allRecipes.asStateFlow()
    
    private val _savedRecipes = MutableStateFlow<List<SuggestionEntity>>(emptyList())
    val savedRecipes: StateFlow<List<SuggestionEntity>> = _savedRecipes.asStateFlow()
    
    private val _favoriteRecipes = MutableStateFlow<List<SuggestionEntity>>(emptyList())
    val favoriteRecipes: StateFlow<List<SuggestionEntity>> = _favoriteRecipes.asStateFlow()
    
    init {
        loadRecipes()
    }
    
    private fun loadRecipes() {
        viewModelScope.launch {
            // Load all recipes
            suggestionDao.getAllSuggestions().collect { all ->
                _allRecipes.value = all
            }
        }
        
        viewModelScope.launch {
            // Load saved recipes
            suggestionDao.getSavedSuggestions().collect { saved ->
                _savedRecipes.value = saved
            }
        }
        
        viewModelScope.launch {
            // Load favorite recipes
            suggestionDao.getFavoriteSuggestions().collect { favorites ->
                _favoriteRecipes.value = favorites
            }
        }
    }
    
    fun loadRecipe(entity: SuggestionEntity, navController: NavController) {
        try {
            // Parse the stored JSON back to NutritionSuggestion
            val repo = SuggestionsRepository(
                OpenRouterClientFactory.create(BuildConfig.OPENROUTER_API_KEY),
                getApplication()
            )
            val suggestion = repo.parseNutritionSuggestion(entity.json)
            
            // Cache it and navigate
            SuggestionCache.currentSuggestion = suggestion
            SuggestionCache.currentSuggestionId = entity.id
            Log.d("RecipesViewModel", "Loaded recipe: ${suggestion.meal.name} (ID: ${entity.id})")
            navController.navigate(NavRoute.SuggestionDetail.route)
        } catch (e: Exception) {
            Log.e("RecipesViewModel", "Failed to load recipe: ${e.message}")
        }
    }
}

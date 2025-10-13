package com.example.moodfood.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.moodfood.BuildConfig
import com.example.moodfood.data.ai.OpenRouterClientFactory
import com.example.moodfood.data.ai.SuggestionsRepository
import com.example.moodfood.data.db.SuggestionEntity
import com.example.moodfood.data.models.NutritionSuggestion
import com.example.moodfood.navigation.NavRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log
import org.json.JSONObject

object SuggestionCache {
    var currentSuggestion: NutritionSuggestion? = null
}

data class HomeUiState(
    val mood: String = "",
    val goal: String = "",
    val symptoms: List<String> = emptyList(),
    val loading: Boolean = false,
    val suggestion: NutritionSuggestion? = null,
    val error: String? = null,
    val recent: List<SuggestionEntity> = emptyList(),
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun setMood(value: String) { _state.value = _state.value.copy(mood = value) }
    fun setGoal(value: String) { _state.value = _state.value.copy(goal = value) }
    fun setSymptoms(list: List<String>) { _state.value = _state.value.copy(symptoms = list) }

    fun getSuggestion() {
        val apiKey = BuildConfig.OPENROUTER_API_KEY
        Log.d("OpenRouter", "API key from BuildConfig: length=${apiKey.length}, value='${apiKey.take(20)}...'")
        if (apiKey.isBlank()) {
            _state.value = _state.value.copy(error = "Missing OpenRouter API key", loading = false)
            return
        }
        val ctx = getApplication<Application>()
        val service = OpenRouterClientFactory.create(apiKey)
        val repo = SuggestionsRepository(service, ctx)
        val s = _state.value
        viewModelScope.launch {
            try {
                _state.value = s.copy(loading = true, error = null)
                val suggestion = repo.getNutritionSuggestion(s.mood, s.goal.ifBlank { "feel better" })
                SuggestionCache.currentSuggestion = suggestion // Cache the suggestion
                Log.d("HomeViewModel", "Cached suggestion: ${suggestion.meal.name}")
                _state.value = _state.value.copy(loading = false, suggestion = suggestion)
                // update recent in background
                repo.recent().stateIn(viewModelScope).value.let { list ->
                    _state.value = _state.value.copy(recent = list)
                }
            } catch (t: Throwable) {
                val base = t.message ?: "Error"
                val hint = if (base.contains("401")) " (check API key, model access, and referer)" else ""
                _state.value = _state.value.copy(loading = false, error = base + hint)
            }
        }
    }

    fun loadRecentSuggestion(entity: SuggestionEntity, navController: NavController) {
        try {
            // Parse the stored JSON back to NutritionSuggestion
            val repo = SuggestionsRepository(
                OpenRouterClientFactory.create(BuildConfig.OPENROUTER_API_KEY),
                getApplication()
            )
            val suggestion = repo.parseNutritionSuggestion(entity.json)
            
            // Cache it and navigate
            SuggestionCache.currentSuggestion = suggestion
            Log.d("HomeViewModel", "Loaded recent suggestion: ${suggestion.meal.name}")
            navController.navigate(NavRoute.SuggestionDetail.route)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Failed to load recent suggestion: ${e.message}")
            _state.value = _state.value.copy(error = "Failed to load suggestion")
        }
    }
}
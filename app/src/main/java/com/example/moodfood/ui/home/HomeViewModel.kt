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
import com.example.moodfood.data.progress.ProgressRepository
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
    var currentSuggestionId: Long? = null
}

data class HomeUiState(
    val mood: String = "",
    val goal: String = "",
    val symptoms: List<String> = emptyList(),
    val loading: Boolean = false,
    val suggestion: NutritionSuggestion? = null,
    val error: String? = null
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val progressRepository = ProgressRepository(app)
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun setMood(value: String) { _state.value = _state.value.copy(mood = value) }
    fun setGoal(value: String) { _state.value = _state.value.copy(goal = value) }
    fun setSymptoms(list: List<String>) { _state.value = _state.value.copy(symptoms = list) }

    fun getSuggestion(navController: NavController? = null) {
        val apiKey = BuildConfig.OPENROUTER_API_KEY
        Log.d("OpenRouter", "API key from BuildConfig: length=${apiKey.length}, valid=${apiKey.isNotBlank()}")
        if (apiKey.isBlank()) {
            _state.value = _state.value.copy(error = "Missing OpenRouter API key. Please add it to .env file.", loading = false)
            return
        }
        val ctx = getApplication<Application>()
        val service = OpenRouterClientFactory.create(apiKey)
        val repo = SuggestionsRepository(service, ctx)
        val s = _state.value
        viewModelScope.launch {
            try {
                _state.value = s.copy(loading = true, error = null)
                val (suggestion, suggestionId) = repo.getNutritionSuggestion(s.mood, s.goal.ifBlank { "feel better" })
                SuggestionCache.currentSuggestion = suggestion
                SuggestionCache.currentSuggestionId = suggestionId
                _state.value = _state.value.copy(loading = false, suggestion = suggestion)
                
                logMoodEntry(s.mood, s.goal, s.symptoms)
                
                // Automatically navigate to detail screen if navController is provided
                navController?.navigate(NavRoute.SuggestionDetail.route)
            } catch (t: Throwable) {
                val base = t.message ?: "Error"
                val hint = if (base.contains("401")) " (check API key, model access, and referer)" else ""
                _state.value = _state.value.copy(loading = false, error = base + hint)
            }
        }
    }
    
    private fun logMoodEntry(mood: String, goal: String, symptoms: List<String>) {
        viewModelScope.launch {
            try {
                val moodEmoji = when (mood.lowercase()) {
                    "happy" -> "😊"
                    "sad" -> "😔"
                    "anxious" -> "😰"
                    "stressed" -> "😫"
                    "angry" -> "😠"
                    "calm" -> "😌"
                    "energetic" -> "😀"
                    "tired" -> "😴"
                    "content" -> "🙂"
                    else -> "😐"
                }
                
                // Calculate mood score based on typical positive/negative moods
                val moodScore = when (mood.lowercase()) {
                    "happy", "energetic", "content" -> 0.9f
                    "calm" -> 0.7f
                    "tired" -> 0.4f
                    "anxious", "stressed" -> 0.3f
                    "sad", "angry" -> 0.2f
                    else -> 0.5f
                }
                
                progressRepository.logMoodEntry(
                    mood = mood,
                    moodEmoji = moodEmoji,
                    goalMood = goal.takeIf { it.isNotBlank() },
                    symptoms = symptoms,
                    moodScore = moodScore
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to log mood entry: ${e.message}")
            }
        }
    }

    fun loadRecentSuggestion(entity: SuggestionEntity, navController: NavController) {
        try {
            val repo = SuggestionsRepository(
                OpenRouterClientFactory.create(BuildConfig.OPENROUTER_API_KEY),
                getApplication()
            )
            val suggestion = repo.parseNutritionSuggestion(entity.json)
            
            SuggestionCache.currentSuggestion = suggestion
            SuggestionCache.currentSuggestionId = entity.id
            navController.navigate(NavRoute.SuggestionDetail.route)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Failed to load recent suggestion: ${e.message}")
            _state.value = _state.value.copy(error = "Failed to load suggestion")
        }
    }
}

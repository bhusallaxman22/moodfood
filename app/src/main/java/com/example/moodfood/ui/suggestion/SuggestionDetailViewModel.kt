package com.example.moodfood.ui.suggestion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodfood.data.db.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

data class SuggestionDetailUiState(
    val isSaved: Boolean = false,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false
)

class SuggestionDetailViewModel(app: Application) : AndroidViewModel(app) {
    private val suggestionDao = AppDatabase.get(app).suggestionDao()
    
    private val _state = MutableStateFlow(SuggestionDetailUiState())
    val state: StateFlow<SuggestionDetailUiState> = _state.asStateFlow()
    
    fun loadSuggestionStatus(suggestionId: Long) {
        viewModelScope.launch {
            try {
                val suggestion = suggestionDao.getById(suggestionId)
                if (suggestion != null) {
                    _state.value = _state.value.copy(
                        isSaved = suggestion.isSaved,
                        isFavorite = suggestion.isFavorite
                    )
                    Log.d("SuggestionDetailVM", "Loaded status for ID $suggestionId: saved=${suggestion.isSaved}, favorite=${suggestion.isFavorite}")
                }
            } catch (e: Exception) {
                Log.e("SuggestionDetailVM", "Error loading suggestion status: ${e.message}")
            }
        }
    }
    
    fun toggleSaved(suggestionId: Long) {
        viewModelScope.launch {
            try {
                val newStatus = !_state.value.isSaved
                suggestionDao.updateSavedStatus(suggestionId, newStatus)
                _state.value = _state.value.copy(isSaved = newStatus)
                Log.d("SuggestionDetailVM", "Toggled saved for ID $suggestionId: $newStatus")
            } catch (e: Exception) {
                Log.e("SuggestionDetailVM", "Error toggling saved: ${e.message}")
            }
        }
    }
    
    fun toggleFavorite(suggestionId: Long) {
        viewModelScope.launch {
            try {
                val newStatus = !_state.value.isFavorite
                suggestionDao.updateFavoriteStatus(suggestionId, newStatus)
                _state.value = _state.value.copy(isFavorite = newStatus)
                Log.d("SuggestionDetailVM", "Toggled favorite for ID $suggestionId: $newStatus")
            } catch (e: Exception) {
                Log.e("SuggestionDetailVM", "Error toggling favorite: ${e.message}")
            }
        }
    }
}

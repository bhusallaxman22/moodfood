package com.example.moodfood.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodfood.data.auth.AuthRepository
import com.example.moodfood.data.progress.ProgressRepository
import com.example.moodfood.data.progress.UserPreferences
import com.example.moodfood.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

data class ProfileUiState(
    val userName: String = "User",
    val userEmail: String = "",
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val progressRepository = ProgressRepository(app)
    private val authRepository = AuthRepository.get(app)
    private val settingsRepository = SettingsRepository(app)
    
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()
    
    init {
        loadUserData()
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                // Get current user info
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    _state.value = _state.value.copy(
                        userName = user.username ?: "User",
                        userEmail = user.email
                    )
                }
                
                // Get dark mode setting
                settingsRepository.darkModeEnabled.collect { darkMode ->
                    _state.value = _state.value.copy(darkModeEnabled = darkMode)
                }
                
                // Get user preferences
                progressRepository.getUserPreferences().collect { preferences ->
                    if (preferences != null) {
                        _state.value = _state.value.copy(
                            notificationsEnabled = preferences.notificationsEnabled,
                            isLoading = false
                        )
                    } else {
                        // Create default preferences for new users
                        user?.let {
                            val defaultPrefs = UserPreferences(
                                userId = it.id,
                                notificationsEnabled = true,
                                darkModeEnabled = false
                            )
                            progressRepository.updatePreferences(defaultPrefs)
                        }
                        _state.value = _state.value.copy(isLoading = false)
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to load user data: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load profile data"
                )
            }
        }
    }
    
    fun toggleNotifications() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser() ?: return@launch
                val newValue = !_state.value.notificationsEnabled
                
                val preferences = UserPreferences(
                    userId = user.id,
                    notificationsEnabled = newValue,
                    darkModeEnabled = _state.value.darkModeEnabled
                )
                
                progressRepository.updatePreferences(preferences)
                _state.value = _state.value.copy(notificationsEnabled = newValue)
                
                Log.d("ProfileViewModel", "Notifications toggled: $newValue")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to toggle notifications: ${e.message}", e)
            }
        }
    }
    
    fun toggleDarkMode() {
        viewModelScope.launch {
            try {
                val newValue = !_state.value.darkModeEnabled
                
                // Persist to DataStore
                settingsRepository.setDarkModeEnabled(newValue)
                
                // Update UI state
                _state.value = _state.value.copy(darkModeEnabled = newValue)
                
                Log.d("ProfileViewModel", "Dark mode toggled: $newValue")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to toggle dark mode: ${e.message}", e)
            }
        }
    }
    
    suspend fun signOut(): Boolean {
        return try {
            authRepository.logout()
            true
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Sign out failed: ${e.message}", e)
            false
        }
    }
}

package com.example.moodfood.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodfood.data.auth.AuthRepository
import com.example.moodfood.data.auth.AuthResult
import com.example.moodfood.data.auth.UserEntity
import com.example.moodfood.data.auth.AuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUser: UserEntity? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSuccess: Boolean = false  // For compatibility with simple UI
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val authRepository = AuthRepository.get(app.applicationContext)
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        // Observe authentication state from repository
        viewModelScope.launch {
            authRepository.isAuthenticated.collect { isAuthenticated ->
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = isAuthenticated
                )
            }
        }
        
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(
                    currentUser = user
                )
            }
        }
    }
    
    // Email/Password Authentication
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )
                
                when (val result = authRepository.loginWithEmail(email, password)) {
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = result.user,
                            successMessage = "Welcome back, ${result.user.fullName}!",
                            isSuccess = true  // For compatibility with simple UI
                        )
                        Log.d("AuthViewModel", "Login successful for: ${result.user.email}")
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                        Log.e("AuthViewModel", "Login failed: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "An unexpected error occurred"
                )
                Log.e("AuthViewModel", "Login exception", e)
            }
        }
    }
    
    fun registerWithEmail(
        email: String,
        username: String,
        password: String,
        fullName: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )
                
                when (val result = authRepository.registerWithEmail(email, username, password, fullName, "", "", "", "")) {
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Account created successfully! Please log in.",
                            isSuccess = true  // For compatibility with simple UI
                        )
                        Log.d("AuthViewModel", "Registration successful for: ${result.user.email}")
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                        Log.e("AuthViewModel", "Registration failed: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "An unexpected error occurred"
                )
                Log.e("AuthViewModel", "Registration exception", e)
            }
        }
    }
    
    // Google Authentication
    fun loginWithGoogle() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )
                
                // In a real implementation, you would integrate with Google Sign-In
                // For now, we'll simulate with a mock user
                val mockEmail = "user@gmail.com"
                val mockName = "Google User"
                
                when (val result = authRepository.loginWithNativeAuth(
                    email = mockEmail,
                    fullName = mockName,
                    provider = AuthProvider.GOOGLE
                )) {
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = result.user,
                            successMessage = "Welcome, ${result.user.fullName}!"
                        )
                        Log.d("AuthViewModel", "Google login successful")
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                        Log.e("AuthViewModel", "Google login failed: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google sign-in failed"
                )
                Log.e("AuthViewModel", "Google login exception", e)
            }
        }
    }
    
    // Biometric Authentication
    fun loginWithBiometric() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )
                
                // In a real implementation, you would integrate with BiometricPrompt
                // For now, we'll simulate with a mock user
                val mockEmail = "biometric@example.com"
                val mockName = "Biometric User"
                
                when (val result = authRepository.loginWithNativeAuth(
                    email = mockEmail,
                    fullName = mockName,
                    provider = AuthProvider.BIOMETRIC
                )) {
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentUser = result.user,
                            successMessage = "Biometric authentication successful!"
                        )
                        Log.d("AuthViewModel", "Biometric login successful")
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                        Log.e("AuthViewModel", "Biometric login failed: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Biometric authentication failed"
                )
                Log.e("AuthViewModel", "Biometric login exception", e)
            }
        }
    }
    
    // Logout
    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = false,
                    currentUser = null,
                    successMessage = "Logged out successfully"
                )
                Log.d("AuthViewModel", "Logout successful")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Logout failed"
                )
                Log.e("AuthViewModel", "Logout exception", e)
            }
        }
    }
    
    // Clear messages
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
    
    // Update profile
    fun updateProfile(fullName: String? = null, profilePicture: String? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )
                
                when (val result = authRepository.updateUserProfile(
                    fullName = fullName,
                    profilePicture = profilePicture
                )) {
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentUser = result.user,
                            successMessage = "Profile updated successfully"
                        )
                        Log.d("AuthViewModel", "Profile updated successfully")
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                        Log.e("AuthViewModel", "Profile update failed: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Profile update failed"
                )
                Log.e("AuthViewModel", "Profile update exception", e)
            }
        }
    }
    
    // Password Reset Methods
    fun getSecurityQuestions(
        email: String,
        onResult: (Pair<String?, String?>?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val questions = authRepository.getUserSecurityQuestions(email)
                onResult(questions)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to get security questions", e)
                onResult(null)
            }
        }
    }
    
    fun verifySecurityAnswers(
        email: String,
        answer1: String,
        answer2: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val isValid = authRepository.verifySecurityAnswers(email, answer1, answer2)
                onResult(isValid)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to verify security answers", e)
                onResult(false)
            }
        }
    }
    
    fun resetPassword(
        email: String,
        newPassword: String,
        answer1: String,
        answer2: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                when (val result = authRepository.resetPassword(email, newPassword, answer1, answer2)) {
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Password reset successfully! Please log in."
                        )
                        onResult(true, null)
                    }
                    is AuthResult.Error -> {
                        onResult(false, result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Password reset exception", e)
                onResult(false, "An unexpected error occurred")
            }
        }
    }
    
    // Compatibility methods for simple UI (LoginScreen/SignupScreen from main)
    // These delegate to the enhanced methods above
    fun signIn(email: String, password: String) {
        loginWithEmail(email, password)
    }
    
    fun signUp(email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Passwords do not match"
            )
            return
        }
        
        // Extract username from email (before @)
        val username = email.substringBefore("@")
        // Use email as fullName if not provided
        val fullName = username.replaceFirstChar { it.uppercase() }
        
        registerWithEmail(email, username, password, fullName)
    }
    
    fun signUpWithSecurityQuestions(
        email: String,
        password: String,
        confirmPassword: String,
        securityQuestion1: String,
        securityAnswer1: String,
        securityQuestion2: String,
        securityAnswer2: String
    ) {
        viewModelScope.launch {
            try {
                if (password != confirmPassword) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Passwords do not match"
                    )
                    return@launch
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )
                
                // Extract username from email (before @)
                val username = email.substringBefore("@")
                // Use email as fullName if not provided
                val fullName = username.replaceFirstChar { it.uppercase() }
                
                when (val result = authRepository.registerWithEmail(
                    email = email,
                    username = username,
                    password = password,
                    fullName = fullName,
                    securityQuestion1 = securityQuestion1,
                    securityAnswer1 = securityAnswer1,
                    securityQuestion2 = securityQuestion2,
                    securityAnswer2 = securityAnswer2
                )) {
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Account created successfully! Please log in.",
                            isSuccess = true
                        )
                        Log.d("AuthViewModel", "Registration with security questions successful")
                    }
                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                        Log.e("AuthViewModel", "Registration failed: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "An unexpected error occurred"
                )
                Log.e("AuthViewModel", "Registration exception", e)
            }
        }
    }
}

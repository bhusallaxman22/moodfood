package com.example.moodfood.data.auth

import android.content.Context
import android.util.Log
import com.example.moodfood.data.db.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

class AuthRepository(private val context: Context) {
    private val userDao = AppDatabase.get(context).userDao()
    private val sessionDao = AppDatabase.get(context).userSessionDao()
    
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // Password hashing utilities
    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val saltedPassword = password + salt
        val hashedBytes = digest.digest(saltedPassword.toByteArray())
        return Base64.getEncoder().encodeToString(hashedBytes)
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return Base64.getEncoder().encodeToString(saltBytes)
    }

    private fun generateSessionId(): String {
        return UUID.randomUUID().toString()
    }

    // Registration
    suspend fun registerWithEmail(
        email: String,
        username: String,
        password: String,
        fullName: String
    ): AuthResult {
        try {
            // Check if user already exists
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return AuthResult.Error("User with this email already exists")
            }

            val existingUsername = userDao.getUserByUsername(username)
            if (existingUsername != null) {
                return AuthResult.Error("Username already taken")
            }

            // Validate input
            if (!isValidEmail(email)) {
                return AuthResult.Error("Invalid email format")
            }
            if (password.length < 6) {
                return AuthResult.Error("Password must be at least 6 characters")
            }
            if (username.length < 3) {
                return AuthResult.Error("Username must be at least 3 characters")
            }

            // Create user
            val salt = generateSalt()
            val hashedPassword = hashPassword(password, salt)
            
            val user = UserEntity(
                email = email,
                username = username,
                passwordHash = "$hashedPassword:$salt", // Store hash:salt format
                fullName = fullName,
                authProvider = AuthProvider.EMAIL_PASSWORD
            )

            val userId = userDao.insertUser(user)
            val createdUser = userDao.getUserById(userId)
                ?: return AuthResult.Error("Failed to create user")

            Log.d("AuthRepository", "User registered successfully: ${createdUser.email}")
            return AuthResult.Success(createdUser)

        } catch (e: Exception) {
            Log.e("AuthRepository", "Registration failed", e)
            return AuthResult.Error("Registration failed: ${e.message}")
        }
    }

    // Login with email/password
    suspend fun loginWithEmail(email: String, password: String): AuthResult {
        try {
            val user = userDao.getUserByEmail(email)
                ?: return AuthResult.Error("User not found")

            // Verify password
            val storedHash = user.passwordHash ?: return AuthResult.Error("Invalid login method")
            val (hash, salt) = storedHash.split(":").let { it[0] to it[1] }
            val inputHash = hashPassword(password, salt)

            if (hash != inputHash) {
                return AuthResult.Error("Invalid password")
            }

            // Update last login
            userDao.updateLastLogin(user.id)
            
            // Create session
            val sessionId = createSession(user.id)
            
            _currentUser.value = user
            _isAuthenticated.value = true

            Log.d("AuthRepository", "User logged in successfully: ${user.email}")
            return AuthResult.Success(user, sessionId)

        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed", e)
            return AuthResult.Error("Login failed: ${e.message}")
        }
    }

    // Native authentication (Google, Biometric)
    suspend fun loginWithNativeAuth(
        email: String,
        fullName: String,
        provider: AuthProvider
    ): AuthResult {
        try {
            var user = userDao.getUserByEmail(email)
            
            if (user == null) {
                // Create new user for native auth
                val username = generateUsernameFromEmail(email)
                user = UserEntity(
                    email = email,
                    username = username,
                    fullName = fullName,
                    authProvider = provider
                )
                val userId = userDao.insertUser(user)
                user = userDao.getUserById(userId)
                    ?: return AuthResult.Error("Failed to create user")
            } else {
                // Update last login
                userDao.updateLastLogin(user.id)
            }

            // Create session
            val sessionId = createSession(user.id)
            
            _currentUser.value = user
            _isAuthenticated.value = true

            Log.d("AuthRepository", "Native auth successful: ${user.email}")
            return AuthResult.Success(user, sessionId)

        } catch (e: Exception) {
            Log.e("AuthRepository", "Native auth failed", e)
            return AuthResult.Error("Authentication failed: ${e.message}")
        }
    }

    // Session management
    private suspend fun createSession(userId: Long): String {
        val sessionId = generateSessionId()
        val deviceId = getDeviceId()
        val expiresAt = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L) // 30 days
        
        val session = UserSessionEntity(
            sessionId = sessionId,
            userId = userId,
            deviceId = deviceId,
            expiresAt = expiresAt
        )
        
        sessionDao.insertSession(session)
        return sessionId
    }

    suspend fun validateSession(sessionId: String): Boolean {
        return try {
            val session = sessionDao.getActiveSession(sessionId)
            session != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun logout() {
        try {
            _currentUser.value?.let { user ->
                sessionDao.deactivateAllUserSessions(user.id)
            }
            _currentUser.value = null
            _isAuthenticated.value = false
            Log.d("AuthRepository", "User logged out successfully")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Logout failed", e)
        }
    }

    // Utility functions
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun generateUsernameFromEmail(email: String): String {
        val baseName = email.substringBefore("@").replace("[^a-zA-Z0-9]".toRegex(), "")
        val random = (1000..9999).random()
        return "${baseName}$random"
    }

    private fun getDeviceId(): String {
        // In production, use a proper device ID method
        return UUID.randomUUID().toString()
    }

    // Get current user info
    suspend fun getCurrentUser(): UserEntity? {
        return _currentUser.value
    }

    // Update user profile
    suspend fun updateUserProfile(
        fullName: String? = null,
        profilePicture: String? = null,
        preferences: String? = null
    ): AuthResult {
        return try {
            val currentUser = _currentUser.value ?: return AuthResult.Error("Not authenticated")
            
            val updatedUser = currentUser.copy(
                fullName = fullName ?: currentUser.fullName,
                profilePicture = profilePicture ?: currentUser.profilePicture,
                preferences = preferences ?: currentUser.preferences
            )
            
            userDao.updateUser(updatedUser)
            _currentUser.value = updatedUser
            
            Log.d("AuthRepository", "Profile updated successfully")
            AuthResult.Success(updatedUser)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Profile update failed", e)
            AuthResult.Error("Profile update failed: ${e.message}")
        }
    }

    // Cleanup expired sessions
    suspend fun cleanupSessions() {
        try {
            sessionDao.cleanupExpiredSessions()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Session cleanup failed", e)
        }
    }
}

sealed class AuthResult {
    data class Success(val user: UserEntity, val sessionId: String? = null) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

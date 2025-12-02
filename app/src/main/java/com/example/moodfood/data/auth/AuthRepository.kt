package com.example.moodfood.data.auth

import android.content.Context
import android.util.Log
import com.example.moodfood.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

class AuthRepository private constructor(private val context: Context) {
    private val userDao = AppDatabase.get(context).userDao()
    private val sessionDao = AppDatabase.get(context).userSessionDao()
    
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    init {
        repositoryScope.launch {
            restoreSession()
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null
        
        fun get(context: Context): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private suspend fun restoreSession() {
        try {
            val session = sessionDao.getMostRecentActiveSession()
            if (session != null) {
                val user = userDao.getUserById(session.userId)
                if (user != null) {
                    _currentUser.value = user
                    _isAuthenticated.value = true
                    Log.d("AuthRepository", "Session restored for user: ${user.email}")
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to restore session", e)
        }
    }

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
        fullName: String,
        securityQuestion1: String,
        securityAnswer1: String,
        securityQuestion2: String,
        securityAnswer2: String
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
            if (securityAnswer1.length < 2 || securityAnswer2.length < 2) {
                return AuthResult.Error("Security answers must be at least 2 characters")
            }

            // Create user with hashed password and security answers
            val salt = generateSalt()
            val hashedPassword = hashPassword(password, salt)
            
            val answer1Salt = generateSalt()
            val hashedAnswer1 = hashPassword(securityAnswer1.lowercase().trim(), answer1Salt)
            
            val answer2Salt = generateSalt()
            val hashedAnswer2 = hashPassword(securityAnswer2.lowercase().trim(), answer2Salt)
            
            val user = UserEntity(
                email = email,
                username = username,
                passwordHash = "$hashedPassword:$salt",
                fullName = fullName,
                authProvider = AuthProvider.EMAIL_PASSWORD,
                securityQuestion1 = securityQuestion1,
                securityAnswer1Hash = "$hashedAnswer1:$answer1Salt",
                securityQuestion2 = securityQuestion2,
                securityAnswer2Hash = "$hashedAnswer2:$answer2Salt"
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
            throw e
        }
    }
    
    // Alias for signOut (used by ProfileViewModel)
    suspend fun signOut() = logout()

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
    
    // Synchronous version for non-suspend contexts
    fun getCurrentUserSync(): UserEntity? {
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

    // Change password for currently authenticated email/password users
    suspend fun changePassword(currentPassword: String, newPassword: String): AuthResult {
        try {
            val currentUser = _currentUser.value ?: return AuthResult.Error("Not authenticated")
            if (currentUser.authProvider != AuthProvider.EMAIL_PASSWORD) {
                return AuthResult.Error("Password change not supported for this account type")
            }

            val storedHash = currentUser.passwordHash ?: return AuthResult.Error("No password set for this account")
            val (hash, salt) = storedHash.split(":").let { it[0] to it[1] }
            val inputHash = hashPassword(currentPassword, salt)

            if (hash != inputHash) {
                return AuthResult.Error("Current password incorrect")
            }

            if (newPassword.length < 6) {
                return AuthResult.Error("New password must be at least 6 characters")
            }

            val newSalt = generateSalt()
            val newHash = hashPassword(newPassword, newSalt)

            val updatedUser = currentUser.copy(passwordHash = "$newHash:$newSalt")
            userDao.updateUser(updatedUser)
            _currentUser.value = updatedUser

            Log.d("AuthRepository", "Password changed for user: ${currentUser.email}")
            return AuthResult.Success(updatedUser)

        } catch (e: Exception) {
            Log.e("AuthRepository", "Password change failed", e)
            return AuthResult.Error("Password change failed: ${e.message}")
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
    
    // Password Reset with Security Questions
    suspend fun getUserSecurityQuestions(email: String): Pair<String?, String?>? {
        return try {
            val user = userDao.getUserByEmail(email)
            if (user != null && user.authProvider == AuthProvider.EMAIL_PASSWORD) {
                Pair(user.securityQuestion1, user.securityQuestion2)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to get security questions", e)
            null
        }
    }
    
    suspend fun verifySecurityAnswers(
        email: String,
        answer1: String,
        answer2: String
    ): Boolean {
        return try {
            val user = userDao.getUserByEmail(email) ?: return false
            if (user.authProvider != AuthProvider.EMAIL_PASSWORD) return false
            
            val answer1Hash = user.securityAnswer1Hash ?: return false
            val answer2Hash = user.securityAnswer2Hash ?: return false
            
            // Verify first answer
            val (hash1, salt1) = answer1Hash.split(":").let { it[0] to it[1] }
            val inputHash1 = hashPassword(answer1.lowercase().trim(), salt1)
            
            // Verify second answer
            val (hash2, salt2) = answer2Hash.split(":").let { it[0] to it[1] }
            val inputHash2 = hashPassword(answer2.lowercase().trim(), salt2)
            
            hash1 == inputHash1 && hash2 == inputHash2
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to verify security answers", e)
            false
        }
    }
    
    suspend fun resetPassword(
        email: String,
        newPassword: String,
        answer1: String,
        answer2: String
    ): AuthResult {
        try {
            val user = userDao.getUserByEmail(email)
                ?: return AuthResult.Error("User not found")
                
            if (user.authProvider != AuthProvider.EMAIL_PASSWORD) {
                return AuthResult.Error("Password reset not supported for this account type")
            }
            
            // Verify security answers
            if (!verifySecurityAnswers(email, answer1, answer2)) {
                return AuthResult.Error("Security answers do not match")
            }
            
            // Validate new password
            if (newPassword.length < 6) {
                return AuthResult.Error("New password must be at least 6 characters")
            }
            
            // Update password
            val newSalt = generateSalt()
            val newHash = hashPassword(newPassword, newSalt)
            
            val updatedUser = user.copy(passwordHash = "$newHash:$newSalt")
            userDao.updateUser(updatedUser)
            
            Log.d("AuthRepository", "Password reset successfully for user: ${user.email}")
            return AuthResult.Success(updatedUser)
            
        } catch (e: Exception) {
            Log.e("AuthRepository", "Password reset failed", e)
            return AuthResult.Error("Password reset failed: ${e.message}")
        }
    }
}

sealed class AuthResult {
    data class Success(val user: UserEntity, val sessionId: String? = null) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
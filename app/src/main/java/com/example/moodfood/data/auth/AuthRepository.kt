package com.example.moodfood.data.auth

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.moodfood.data.db.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.UUID

private const val AUTH_DATASTORE_NAME = "auth_preferences"

val Context.authDataStore by preferencesDataStore(AUTH_DATASTORE_NAME)

class AuthRepository(private val context: Context) {
    private val userDao = AppDatabase.getInstance(context).userDao() //getting access to the database
    
    private object Keys {
        val currentUserId = stringPreferencesKey("current_user_id") // setting up a key to remember who's currently logged in
    }

    suspend fun signUp(email: String, password: String): Result<Unit> {
        Log.d("AuthRepository", "Starting sign up for email: $email")
        return try {
            // Check if user already exists
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return Result.failure(Exception("User with this email already exists"))
            }

            // Validate email format
            if (!isValidEmail(email)) {
                return Result.failure(Exception("Invalid email format"))
            }

            // Validate password
            if (password.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters"))
            }

            // Hash password
            val passwordHash = hashPassword(password)

            // Create user
            val user = UserEntity(
                email = email.lowercase().trim(), //test@example.com
                passwordHash = passwordHash         //abc123
            )

            // Save to database
            userDao.insertUser(user)
            Log.d("AuthRepository", "User inserted to database with ID: ${user.id}")

            // Save current user session
            context.authDataStore.edit { preferences ->
                preferences[Keys.currentUserId] = user.id
            }
            Log.d("AuthRepository", "User session saved to DataStore")

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        Log.d("AuthRepository", "Starting sign in for email: $email")
        return try {
            // Validate inputs
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Email and password are required"))
            }

            // Find user
            val user = userDao.getUserByEmail(email.lowercase().trim())
                ?: return Result.failure(Exception("User not found"))

            // Verify password
            val passwordHash = hashPassword(password)
            if (user.passwordHash != passwordHash) {
                return Result.failure(Exception("Invalid password"))
            }

            // Update last login
            val updatedUser = user.copy(lastLoginAt = System.currentTimeMillis())
            userDao.updateUser(updatedUser)

            // Save current user session
            context.authDataStore.edit { preferences ->
                preferences[Keys.currentUserId] = user.id
            }
            Log.d("AuthRepository", "User session saved to DataStore for sign in")

            Result.success(User(
                id = user.id,
                email = user.email,
                createdAt = user.createdAt,
                lastLoginAt = updatedUser.lastLoginAt
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        context.authDataStore.edit { preferences ->
            preferences.remove(Keys.currentUserId)
        }
    }
    // Who's currently logged in
    fun getCurrentUser(): Flow<User?> {
        return userDao.getCurrentUser().map { userEntity ->
            userEntity?.let { user ->
                User(
                    id = user.id,
                    email = user.email,
                    createdAt = user.createdAt,
                    lastLoginAt = user.lastLoginAt
                )
            }
        }
    }

    fun isAuthenticated(): Flow<Boolean> {
        return context.authDataStore.data.map { preferences ->
            preferences[Keys.currentUserId] != null
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

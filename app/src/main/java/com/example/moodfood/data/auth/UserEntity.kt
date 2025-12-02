package com.example.moodfood.data.auth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val email: String,
    val username: String,
    val passwordHash: String? = null, // Null for native auth users
    val fullName: String,
    val profilePicture: String? = null,
    val authProvider: AuthProvider,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val preferences: String = "{}", // JSON string for user preferences
    val securityQuestion1: String? = null,
    val securityAnswer1Hash: String? = null,
    val securityQuestion2: String? = null,
    val securityAnswer2Hash: String? = null
)

enum class AuthProvider {
    EMAIL_PASSWORD,
    GOOGLE,
    BIOMETRIC
}

@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey 
    val sessionId: String,
    val userId: Long,
    val deviceId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val isActive: Boolean = true
)

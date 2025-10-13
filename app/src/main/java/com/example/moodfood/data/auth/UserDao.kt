package com.example.moodfood.data.auth

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email AND isActive = 1 LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId AND isActive = 1 LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username AND isActive = 1 LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET lastLoginAt = :timestamp WHERE id = :userId")
    suspend fun updateLastLogin(userId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE users SET isActive = 0 WHERE id = :userId")
    suspend fun deactivateUser(userId: Long)

    @Query("SELECT * FROM users WHERE isActive = 1")
    fun getAllActiveUsers(): Flow<List<UserEntity>>
}

@Dao
interface UserSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UserSessionEntity)

    @Query("SELECT * FROM user_sessions WHERE sessionId = :sessionId AND isActive = 1 AND expiresAt > :currentTime LIMIT 1")
    suspend fun getActiveSession(sessionId: String, currentTime: Long = System.currentTimeMillis()): UserSessionEntity?

    @Query("SELECT * FROM user_sessions WHERE userId = :userId AND isActive = 1 AND expiresAt > :currentTime")
    suspend fun getActiveSessionsForUser(userId: Long, currentTime: Long = System.currentTimeMillis()): List<UserSessionEntity>

    @Query("UPDATE user_sessions SET isActive = 0 WHERE sessionId = :sessionId")
    suspend fun deactivateSession(sessionId: String)

    @Query("UPDATE user_sessions SET isActive = 0 WHERE userId = :userId")
    suspend fun deactivateAllUserSessions(userId: Long)

    @Query("DELETE FROM user_sessions WHERE expiresAt < :currentTime OR isActive = 0")
    suspend fun cleanupExpiredSessions(currentTime: Long = System.currentTimeMillis())
}

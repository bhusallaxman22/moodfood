package com.example.moodfood.data.progress

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    // Mood Entries
    @Insert
    suspend fun insertMoodEntry(entry: MoodEntry): Long
    
    @Query("SELECT * FROM mood_entries WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMoodEntries(userId: Long, limit: Int = 10): Flow<List<MoodEntry>>
    
    @Query("SELECT * FROM mood_entries WHERE userId = :userId AND date >= :startDate ORDER BY timestamp DESC")
    fun getMoodEntriesSince(userId: Long, startDate: String): Flow<List<MoodEntry>>
    
    @Query("SELECT COUNT(*) FROM mood_entries WHERE userId = :userId")
    suspend fun getTotalMoodEntries(userId: Long): Int
    
    @Query("SELECT AVG(moodScore) FROM mood_entries WHERE userId = :userId")
    suspend fun getAverageMoodScore(userId: Long): Float?
    
    @Query("SELECT * FROM mood_entries WHERE userId = :userId AND date = :date")
    suspend fun getMoodEntryByDate(userId: Long, date: String): MoodEntry?
    
    // Nutrition Logs
    @Insert
    suspend fun insertNutritionLog(log: NutritionLog): Long
    
    @Query("SELECT COUNT(*) FROM nutrition_logs WHERE userId = :userId")
    suspend fun getTotalNutritionLogs(userId: Long): Int
    
    @Query("SELECT COUNT(*) FROM nutrition_logs WHERE userId = :userId AND date >= :startDate")
    suspend fun getNutritionLogsSince(userId: Long, startDate: String): Int
    
    // Achievements
    @Insert
    suspend fun insertAchievement(achievement: AchievementEntity): Long
    
    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY unlockedAt DESC")
    fun getUserAchievements(userId: Long): Flow<List<AchievementEntity>>
    
    @Query("SELECT * FROM achievements WHERE userId = :userId AND achievementType = :type LIMIT 1")
    suspend fun getAchievementByType(userId: Long, type: String): AchievementEntity?
    
    // User Preferences
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreferences(preferences: UserPreferences)
    
    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    fun getUserPreferences(userId: Long): Flow<UserPreferences?>
    
    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    suspend fun getUserPreferencesOnce(userId: Long): UserPreferences?
    
    // Streaks calculation
    @Query("""
        SELECT COUNT(DISTINCT date) FROM mood_entries 
        WHERE userId = :userId 
        AND date >= :startDate 
        ORDER BY date DESC
    """)
    suspend fun getDaysWithEntries(userId: Long, startDate: String): Int
}

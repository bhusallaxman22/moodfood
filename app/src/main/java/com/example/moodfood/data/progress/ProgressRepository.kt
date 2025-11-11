package com.example.moodfood.data.progress

import android.content.Context
import com.example.moodfood.data.auth.AuthRepository
import com.example.moodfood.data.db.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ProgressRepository(private val context: Context) {
    private val db = AppDatabase.get(context)
    private val progressDao = db.progressDao()
    private val authRepository = AuthRepository.get(context)
    
    // Mood tracking
    suspend fun logMoodEntry(
        mood: String,
        moodEmoji: String,
        goalMood: String?,
        symptoms: List<String>,
        moodScore: Float
    ): Long {
        val user = authRepository.getCurrentUser() ?: throw Exception("User not logged in")
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        val entry = MoodEntry(
            userId = user.id,
            mood = mood,
            moodEmoji = moodEmoji,
            goalMood = goalMood,
            symptoms = symptoms.joinToString(","),
            date = today,
            moodScore = moodScore
        )
        
        val entryId = progressDao.insertMoodEntry(entry)
        
        // Check and unlock achievements
        checkAndUnlockAchievements(user.id)
        
        return entryId
    }
    
    suspend fun logNutrition(mealName: String, suggestionId: Long? = null): Long {
        val user = authRepository.getCurrentUser() ?: throw Exception("User not logged in")
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        val log = NutritionLog(
            userId = user.id,
            mealName = mealName,
            date = today,
            suggestionId = suggestionId
        )
        
        return progressDao.insertNutritionLog(log)
    }
    
    // Get stats
    fun getRecentMoodEntries(limit: Int = 10): Flow<List<MoodEntry>> {
        val userId = runCatching { 
            authRepository.getCurrentUserSync()?.id 
        }.getOrNull() ?: 0
        return progressDao.getRecentMoodEntries(userId, limit)
    }
    
    suspend fun getTotalSessions(): Int {
        val user = authRepository.getCurrentUser() ?: return 0
        return progressDao.getTotalMoodEntries(user.id)
    }
    
    suspend fun getAverageMood(): Pair<String, Float> {
        val user = authRepository.getCurrentUser() ?: return Pair("N/A", 0f)
        val avgScore = progressDao.getAverageMoodScore(user.id) ?: 0f
        
        val moodLabel = when {
            avgScore >= 0.8f -> "Happy"
            avgScore >= 0.6f -> "Content"
            avgScore >= 0.4f -> "Calm"
            avgScore >= 0.2f -> "Low"
            else -> "Very Low"
        }
        
        return Pair(moodLabel, avgScore)
    }
    
    suspend fun getCurrentStreak(): Int {
        val user = authRepository.getCurrentUser() ?: return 0
        
        // Calculate streak by checking consecutive days
        var streak = 0
        var currentDate = LocalDate.now()
        
        for (i in 0..30) { // Check up to 30 days back
            val dateStr = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val entry = progressDao.getMoodEntryByDate(user.id, dateStr)
            
            if (entry != null) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                break
            }
        }
        
        return streak
    }
    
    suspend fun getMealsTracked(): Int {
        val user = authRepository.getCurrentUser() ?: return 0
        return progressDao.getTotalNutritionLogs(user.id)
    }
    
    suspend fun getGoodDaysCount(): Int {
        val user = authRepository.getCurrentUser() ?: return 0
        val thirtyDaysAgo = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        val entries = progressDao.getMoodEntriesSince(user.id, thirtyDaysAgo).first()
        return entries.count { it.moodScore >= 0.6f }
    }
    
    suspend fun getWeeklyProgress(): List<Pair<String, Float>> {
        val user = authRepository.getCurrentUser() ?: return emptyList()
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value - 1L)
        
        val weekProgress = mutableListOf<Pair<String, Float>>()
        
        for (i in 0..6) {
            val date = startOfWeek.plusDays(i.toLong())
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val dayName = date.dayOfWeek.name.take(3).lowercase()
                .replaceFirstChar { it.uppercase() }
            
            val entry = progressDao.getMoodEntryByDate(user.id, dateStr)
            val progress = entry?.moodScore ?: 0f
            
            weekProgress.add(Pair(dayName, progress))
        }
        
        return weekProgress
    }
    
    // Achievements
    fun getUserAchievements(): Flow<List<AchievementEntity>> {
        val userId = runCatching { 
            authRepository.getCurrentUserSync()?.id 
        }.getOrNull() ?: 0
        return progressDao.getUserAchievements(userId)
    }
    
    private suspend fun checkAndUnlockAchievements(userId: Long) {
        val totalSessions = progressDao.getTotalMoodEntries(userId)
        val streak = getCurrentStreak()
        val mealsTracked = progressDao.getTotalNutritionLogs(userId)
        
        // First steps achievement
        if (totalSessions >= 1) {
            unlockAchievement(
                userId,
                "first_entry",
                "First Steps",
                "Complete your first mood entry",
                "🏆"
            )
        }
        
        // Week warrior
        if (streak >= 7) {
            unlockAchievement(
                userId,
                "week_streak",
                "Week Warrior",
                "Maintain a 7-day streak",
                "🔥"
            )
        }
        
        // Meal tracker
        if (mealsTracked >= 10) {
            unlockAchievement(
                userId,
                "meal_tracker",
                "Nutrition Pro",
                "Track 10 meals",
                "🥗"
            )
        }
        
        // Monthly champion
        if (totalSessions >= 30) {
            unlockAchievement(
                userId,
                "monthly_champion",
                "Consistency King",
                "30 mood entries logged",
                "💪"
            )
        }
    }
    
    private suspend fun unlockAchievement(
        userId: Long,
        type: String,
        title: String,
        description: String,
        icon: String
    ) {
        val existing = progressDao.getAchievementByType(userId, type)
        if (existing == null) {
            progressDao.insertAchievement(
                AchievementEntity(
                    userId = userId,
                    achievementType = type,
                    title = title,
                    description = description,
                    icon = icon,
                    isUnlocked = true
                )
            )
        }
    }
    
    // Preferences
    fun getUserPreferences(): Flow<UserPreferences?> {
        val userId = runCatching { 
            authRepository.getCurrentUserSync()?.id 
        }.getOrNull() ?: 0
        return progressDao.getUserPreferences(userId)
    }
    
    suspend fun updatePreferences(preferences: UserPreferences) {
        progressDao.insertOrUpdatePreferences(preferences)
    }
}

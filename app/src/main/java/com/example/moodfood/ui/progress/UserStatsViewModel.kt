package com.example.moodfood.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import android.util.Log
import com.example.moodfood.data.progress.ProgressRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class UserStats(
    val totalSessions: Int = 0,
    val averageMood: String = "N/A",
    val currentStreak: Int = 0,
    val mealsTracked: Int = 0,
    val goodDays: Int = 0,
    val goalsMet: Int = 0,
    val weeklyProgress: List<WeeklyProgressData> = emptyList(),
    val recentMoods: List<MoodData> = emptyList(),
    val achievements: List<Achievement> = emptyList()
)

data class WeeklyProgressData(
    val day: String,
    val progress: Float
)

data class MoodData(
    val date: String,
    val emoji: String,
    val mood: String
)

data class Achievement(
    val emoji: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = true
)

class UserStatsViewModel(app: Application) : AndroidViewModel(app) {
    private val progressRepository = ProgressRepository(app)
    
    private val _userStats = MutableStateFlow<UserStats?>(null)
    val userStats: StateFlow<UserStats?> = _userStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Automatically load stats when ViewModel is created
        loadUserStats()
    }

    fun loadUserStats() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Collect real-time data from repository
                combine(
                    progressRepository.getRecentMoodEntries(10),
                    progressRepository.getUserAchievements()
                ) { moodEntries, achievements ->
                    // Get aggregate stats
                    val totalSessions = progressRepository.getTotalSessions()
                    val (avgMood, _) = progressRepository.getAverageMood()
                    val currentStreak = progressRepository.getCurrentStreak()
                    val mealsTracked = progressRepository.getMealsTracked()
                    val goodDays = progressRepository.getGoodDaysCount()
                    val weeklyProgress = progressRepository.getWeeklyProgress()
                    
                    // Transform mood entries to display data
                    val recentMoods = moodEntries.map { entry ->
                        val entryDate = LocalDate.parse(entry.date, DateTimeFormatter.ISO_LOCAL_DATE)
                        val today = LocalDate.now()
                        val daysAgo = ChronoUnit.DAYS.between(entryDate, today).toInt()
                        
                        val dateLabel = when (daysAgo) {
                            0 -> "Today"
                            1 -> "Yesterday"
                            else -> "$daysAgo days ago"
                        }
                        
                        MoodData(
                            date = dateLabel,
                            emoji = entry.moodEmoji,
                            mood = entry.mood
                        )
                    }
                    
                    // Transform achievements to display data
                    val achievementsList = achievements.map { achievement ->
                        Achievement(
                            emoji = achievement.icon,
                            title = achievement.title,
                            description = achievement.description,
                            isUnlocked = achievement.isUnlocked
                        )
                    }
                    
                    // Transform weekly progress
                    val weeklyProgressList = weeklyProgress.map { (day, progress) ->
                        WeeklyProgressData(day, progress)
                    }
                    
                    UserStats(
                        totalSessions = totalSessions,
                        averageMood = avgMood,
                        currentStreak = currentStreak,
                        mealsTracked = mealsTracked,
                        goodDays = goodDays,
                        goalsMet = achievementsList.count { it.isUnlocked },
                        weeklyProgress = weeklyProgressList,
                        recentMoods = recentMoods,
                        achievements = achievementsList
                    )
                }.collect { stats ->
                    _userStats.value = stats
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                Log.e("UserStatsViewModel", "Failed to load user stats: ${e.message}", e)
                _isLoading.value = false
                
                // Show empty stats on error
                _userStats.value = UserStats()
            }
        }
    }
}

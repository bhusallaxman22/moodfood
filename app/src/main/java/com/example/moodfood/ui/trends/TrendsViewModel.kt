package com.example.moodfood.ui.trends

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodfood.data.progress.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class MoodTrendData(
    val date: String,
    val moodScore: Float,
    val mood: String
)

data class WeeklyTrendData(
    val weekLabel: String,
    val averageMood: Float,
    val entryCount: Int
)

data class MoodDistribution(
    val mood: String,
    val emoji: String,
    val count: Int,
    val percentage: Float
)

data class TrendsUiState(
    val isLoading: Boolean = false,
    val moodTrendData: List<MoodTrendData> = emptyList(),
    val weeklyTrends: List<WeeklyTrendData> = emptyList(),
    val moodDistribution: List<MoodDistribution> = emptyList(),
    val averageMoodLast7Days: Float = 0f,
    val averageMoodLast30Days: Float = 0f,
    val moodChange: Float = 0f, // Positive = improving, Negative = declining
    val topMood: String = "N/A",
    val totalEntries: Int = 0,
    val error: String? = null
)

class TrendsViewModel(app: Application) : AndroidViewModel(app) {
    private val progressRepository = ProgressRepository(app)
    
    private val _uiState = MutableStateFlow(TrendsUiState())
    val uiState: StateFlow<TrendsUiState> = _uiState.asStateFlow()

    init {
        loadTrendsData()
    }

    fun loadTrendsData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // Get mood entries for last 30 days
                val today = LocalDate.now()
                val thirtyDaysAgo = today.minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE)
                val sevenDaysAgo = today.minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)
                
                progressRepository.getMoodEntriesSince(thirtyDaysAgo).collect { entries ->
                    if (entries.isEmpty()) {
                        _uiState.value = TrendsUiState(
                            isLoading = false,
                            error = null
                        )
                        return@collect
                    }
                    
                    // Calculate mood trend data (last 30 days)
                    val moodTrendData = entries.map { entry ->
                        MoodTrendData(
                            date = entry.date,
                            moodScore = entry.moodScore,
                            mood = entry.mood
                        )
                    }.sortedBy { LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE) }
                    
                    // Calculate weekly trends
                    val weeklyTrends = calculateWeeklyTrends(entries)
                    
                    // Calculate mood distribution
                    val moodDistribution = calculateMoodDistribution(entries)
                    
                    // Calculate averages
                    val last7DaysEntries = entries.filter { entry ->
                        LocalDate.parse(entry.date, DateTimeFormatter.ISO_LOCAL_DATE)
                            .isAfter(today.minusDays(8))
                    }
                    val last30DaysEntries = entries
                    
                    val avg7Days = if (last7DaysEntries.isNotEmpty()) {
                        last7DaysEntries.map { it.moodScore }.average().toFloat()
                    } else 0f
                    
                    val avg30Days = if (last30DaysEntries.isNotEmpty()) {
                        last30DaysEntries.map { it.moodScore }.average().toFloat()
                    } else 0f
                    
                    // Calculate mood change (7-day vs 30-day average)
                    val moodChange = avg7Days - avg30Days
                    
                    // Get top mood
                    val topMood = moodDistribution.maxByOrNull { it.count }?.mood ?: "N/A"
                    
                    _uiState.value = TrendsUiState(
                        isLoading = false,
                        moodTrendData = moodTrendData,
                        weeklyTrends = weeklyTrends,
                        moodDistribution = moodDistribution,
                        averageMoodLast7Days = avg7Days,
                        averageMoodLast30Days = avg30Days,
                        moodChange = moodChange,
                        topMood = topMood,
                        totalEntries = entries.size,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("TrendsViewModel", "Failed to load trends data: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load trends: ${e.message}"
                )
            }
        }
    }
    
    private fun calculateWeeklyTrends(entries: List<com.example.moodfood.data.progress.MoodEntry>): List<WeeklyTrendData> {
        val today = LocalDate.now()
        val weeks = mutableListOf<WeeklyTrendData>()
        
        for (i in 0..3) { // Last 4 weeks
            val weekEnd = today.minusDays(i * 7L)
            val weekStart = weekEnd.minusDays(6)
            
            val weekEntries = entries.filter { entry ->
                val entryDate = LocalDate.parse(entry.date, DateTimeFormatter.ISO_LOCAL_DATE)
                !entryDate.isBefore(weekStart) && !entryDate.isAfter(weekEnd)
            }
            
            if (weekEntries.isNotEmpty()) {
                val avgMood = weekEntries.map { it.moodScore }.average().toFloat()
                weeks.add(
                    WeeklyTrendData(
                        weekLabel = if (i == 0) "This Week" else "${i} wk ago",
                        averageMood = avgMood,
                        entryCount = weekEntries.size
                    )
                )
            }
        }
        
        return weeks.reversed()
    }
    
    private fun calculateMoodDistribution(entries: List<com.example.moodfood.data.progress.MoodEntry>): List<MoodDistribution> {
        val moodCounts = entries.groupingBy { it.mood }.eachCount()
        val total = entries.size
        
        return moodCounts.map { (mood, count) ->
            MoodDistribution(
                mood = mood.replaceFirstChar { it.uppercase() },
                emoji = getMoodEmoji(mood),
                count = count,
                percentage = (count.toFloat() / total) * 100f
            )
        }.sortedByDescending { it.count }
    }
    
    private fun getMoodEmoji(mood: String): String {
        return when (mood.lowercase()) {
            "happy" -> "😊"
            "calm" -> "😌"
            "content" -> "🙂"
            "energetic" -> "💪"
            "low" -> "😔"
            "anxious" -> "😰"
            "stressed" -> "😩"
            "irritable" -> "😤"
            "fatigued" -> "🥱"
            else -> "😐"
        }
    }
    
    fun refreshData() {
        loadTrendsData()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

package com.example.moodfood.data.progress

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.moodfood.data.auth.UserEntity

@Entity(
    tableName = "mood_entries",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val mood: String,
    val moodEmoji: String,
    val goalMood: String?,
    val symptoms: String = "", // Comma-separated
    val timestamp: Long = System.currentTimeMillis(),
    val date: String, // yyyy-MM-dd format
    val moodScore: Float // 0.0 to 1.0 (low to high)
)

@Entity(
    tableName = "nutrition_logs",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class NutritionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val mealName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String, // yyyy-MM-dd format
    val suggestionId: Long? = null
)

@Entity(
    tableName = "achievements",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val achievementType: String,
    val title: String,
    val description: String,
    val icon: String,
    val unlockedAt: Long = System.currentTimeMillis(),
    val isUnlocked: Boolean = true
)

@Entity(
    tableName = "user_preferences"
)
data class UserPreferences(
    @PrimaryKey val userId: Long,
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val reminderTime: String? = null, // HH:mm format
    val weeklyGoal: Int = 5, // Sessions per week
    val privacyDataSharing: Boolean = false
)

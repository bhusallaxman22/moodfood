package com.example.moodfood.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps mood strings to Material Icons instead of emojis for a more professional UI
 */
object MoodIcons {
    fun getMoodIcon(mood: String): ImageVector {
        return when (mood.lowercase()) {
            "happy" -> Icons.Filled.SentimentSatisfied
            "calm" -> Icons.Filled.SelfImprovement
            "low", "sad" -> Icons.Filled.SentimentDissatisfied
            "anxious" -> Icons.Filled.Error
            "stressed" -> Icons.Filled.Warning
            "irritable" -> Icons.Filled.SentimentVeryDissatisfied
            "fatigued", "tired" -> Icons.Filled.Bedtime
            "content" -> Icons.Filled.FavoriteBorder
            "energetic" -> Icons.Filled.Bolt
            else -> Icons.Filled.SentimentNeutral
        }
    }

    fun getEmoji(mood: String): String {
        return when (mood.lowercase()) {
            "happy" -> "😊"
            "calm" -> "😌"
            "low", "sad" -> "😔"
            "anxious" -> "😰"
            "stressed" -> "😫"
            "irritable" -> "😤"
            "fatigued", "tired" -> "🥱"
            "content" -> "😌"
            "energetic" -> "💪"
            else -> "😐"
        }
    }

    // Section icons to replace emoji headers
    object Section {
        val Mood = Icons.Filled.Mood
        val Target = Icons.Filled.Flag
        val Settings = Icons.Filled.Settings
        val Food = Icons.Filled.Restaurant
        val Logout = Icons.Filled.Logout
        val Stats = Icons.Filled.Assessment
        val Trends = Icons.Filled.TrendingUp
        val Achievements = Icons.Filled.EmojiEvents
        val Mindfulness = Icons.Filled.SelfImprovement
        val Timer = Icons.Filled.Timer
        val Profile = Icons.Filled.Person
        val Notifications = Icons.Filled.Notifications
        val DarkMode = Icons.Filled.DarkMode
        val Privacy = Icons.Filled.Shield
        val Help = Icons.Filled.Help
        val Info = Icons.Filled.Info
        val Restaurant = Icons.Filled.Restaurant
        val Warning = Icons.Filled.Warning
        val Spicy = Icons.Filled.Whatshot
        val Chef = Icons.Filled.Restaurant
        val Money = Icons.Filled.AttachMoney
        val Public = Icons.Filled.Public
        val Nutrition = Icons.Filled.FoodBank
        val CookingSteps = Icons.Filled.ListAlt
    }
}

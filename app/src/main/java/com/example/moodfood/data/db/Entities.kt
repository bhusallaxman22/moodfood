package com.example.moodfood.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.moodfood.data.models.NutritionSuggestion

@Entity(tableName = "suggestions")
data class SuggestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val name: String,
    val mood: String,
    val goal: String?,
    val symptomsCsv: String,
    val json: String,
    val isSaved: Boolean = false,
    val isFavorite: Boolean = false
)

package com.example.moodfood.data.nutrition

import android.content.Context
import com.example.moodfood.data.models.MoodNutrition
import org.json.JSONObject
import java.io.IOException
import java.util.*
import android.util.Log

class MoodNutritionService(private val context: Context) {
    
    private var moodToNutritionMap: Map<String, MoodNutrition>? = null
    
    fun getMoodNutrition(mood: String): MoodNutrition? {
        if (moodToNutritionMap == null) {
            loadMoodNutritionData()
        }
        
        // Extract mood word from emoji format (e.g., "😀 happy" -> "happy")
        val moodWord = extractMoodWord(mood)
        Log.d("MoodNutrition", "Original mood: '$mood' -> Mapped to: '$moodWord'")
        Log.d("MoodNutrition", "Available keys: ${moodToNutritionMap?.keys}")
        
        return moodToNutritionMap?.get(moodWord)
    }
    
    private fun extractMoodWord(mood: String): String {
        // Handle emoji format like "😀 happy" -> "Happy"
        val cleanMood = if (mood.contains(" ")) {
            mood.substringAfter(" ").trim()
        } else {
            mood.trim()
        }
        
        // Map common variations to JSON keys
        return when (cleanMood.lowercase()) {
            "happy" -> "Happy"
            "calm" -> "Calm"
            "low" -> "Low"
            "sad" -> "Sad"
            "anxious" -> "Anxious"
            "stressed" -> "Stressed"
            "fatigued", "tired" -> "Fatigued"
            "energetic", "energized" -> "Energized"
            "content" -> "Happy" // Map content to happy
            "irritable" -> "Irritable"
            else -> {
                Log.w("MoodNutrition", "Unknown mood: '$cleanMood', defaulting to Happy")
                "Happy"
            }
        }
    }
    
    private fun loadMoodNutritionData() {
        try {
            val json = context.assets.open("mood-to-nutrition.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(json)
            val map = mutableMapOf<String, MoodNutrition>()
            
            jsonObject.keys().forEach { mood ->
                val moodData = jsonObject.getJSONObject(mood)
                val foods = moodData.getJSONArray("foods").let { array ->
                    (0 until array.length()).map { array.getString(it) }
                }
                val nutrients = moodData.getJSONArray("nutrients").let { array ->
                    (0 until array.length()).map { array.getString(it) }
                }
                map[mood] = MoodNutrition(foods, nutrients)
            }
            
            moodToNutritionMap = map
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

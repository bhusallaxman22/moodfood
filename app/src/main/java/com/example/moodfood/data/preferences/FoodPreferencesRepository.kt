package com.example.moodfood.data.preferences

import android.content.Context
import com.example.moodfood.data.auth.AuthRepository
import com.example.moodfood.data.db.AppDatabase
import kotlinx.coroutines.flow.Flow

class FoodPreferencesRepository(private val context: Context) {
    private val db = AppDatabase.get(context)
    private val preferencesDao = db.foodPreferencesDao()
    private val authRepository = AuthRepository.get(context)
    
    fun getUserPreferences(): Flow<FoodPreferences?> {
        val userId = authRepository.getCurrentUserSync()?.id ?: return kotlinx.coroutines.flow.flowOf(null)
        return preferencesDao.getPreferences(userId)
    }
    
    suspend fun getUserPreferencesOnce(): FoodPreferences? {
        val userId = authRepository.getCurrentUserSync()?.id ?: return null
        return preferencesDao.getPreferencesOnce(userId)
    }
    
    suspend fun savePreferences(preferences: FoodPreferences) {
        preferencesDao.insertOrUpdate(preferences)
    }
    
    suspend fun updateSetupComplete(isComplete: Boolean) {
        val userId = authRepository.getCurrentUserSync()?.id ?: return
        preferencesDao.updateSetupComplete(userId, isComplete)
    }
    
    suspend fun createDefaultPreferences(userId: Long): FoodPreferences {
        val defaultPreferences = FoodPreferences(
            userId = userId,
            dietaryRestrictions = "",
            allergies = "",
            dislikedFoods = "",
            preferredCuisines = "",
            spiceLevel = "medium",
            mealComplexity = "medium",
            cookingTime = "30",
            servings = 1,
            budget = "medium",
            organicPreference = false,
            localPreference = false,
            isSetupComplete = false
        )
        preferencesDao.insertOrUpdate(defaultPreferences)
        return defaultPreferences
    }
    
    suspend fun deletePreferences(userId: Long) {
        preferencesDao.deletePreferences(userId)
    }
    
    // Helper function to get dietary restrictions as list
    fun getDietaryRestrictionsList(preferences: FoodPreferences): List<String> {
        return if (preferences.dietaryRestrictions.isBlank()) {
            emptyList()
        } else {
            preferences.dietaryRestrictions.split(",").map { it.trim() }
        }
    }
    
    // Helper function to get allergies as list
    fun getAllergiesList(preferences: FoodPreferences): List<String> {
        return if (preferences.allergies.isBlank()) {
            emptyList()
        } else {
            preferences.allergies.split(",").map { it.trim() }
        }
    }
    
    // Helper function to get disliked foods as list
    fun getDislikedFoodsList(preferences: FoodPreferences): List<String> {
        return if (preferences.dislikedFoods.isBlank()) {
            emptyList()
        } else {
            preferences.dislikedFoods.split(",").map { it.trim() }
        }
    }
    
    // Helper function to get preferred cuisines as list
    fun getPreferredCuisinesList(preferences: FoodPreferences): List<String> {
        return if (preferences.preferredCuisines.isBlank()) {
            emptyList()
        } else {
            preferences.preferredCuisines.split(",").map { it.trim() }
        }
    }
}

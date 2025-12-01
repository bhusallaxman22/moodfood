package com.example.moodfood.data.preferences

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodPreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(preferences: FoodPreferences)
    
    @Query("SELECT * FROM food_preferences WHERE userId = :userId")
    fun getPreferences(userId: Long): Flow<FoodPreferences?>
    
    @Query("SELECT * FROM food_preferences WHERE userId = :userId")
    suspend fun getPreferencesOnce(userId: Long): FoodPreferences?
    
    @Query("UPDATE food_preferences SET isSetupComplete = :isComplete WHERE userId = :userId")
    suspend fun updateSetupComplete(userId: Long, isComplete: Boolean)
    
    @Query("DELETE FROM food_preferences WHERE userId = :userId")
    suspend fun deletePreferences(userId: Long)
}

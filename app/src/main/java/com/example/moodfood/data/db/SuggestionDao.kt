package com.example.moodfood.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SuggestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SuggestionEntity): Long

    @Update
    suspend fun update(entity: SuggestionEntity)

    @Query("SELECT * FROM suggestions")
    fun getAllSuggestions(): Flow<List<SuggestionEntity>>

    @Query("SELECT * FROM suggestions ORDER BY timestamp DESC LIMIT :limit")
    fun recent(limit: Int = 10): Flow<List<SuggestionEntity>>
    
    @Query("SELECT * FROM suggestions WHERE isSaved = 1 ORDER BY timestamp DESC")
    fun getSavedSuggestions(): Flow<List<SuggestionEntity>>
    
    @Query("SELECT * FROM suggestions WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteSuggestions(): Flow<List<SuggestionEntity>>
    
    @Query("SELECT * FROM suggestions WHERE id = :id")
    suspend fun getById(id: Long): SuggestionEntity?
    
    @Query("UPDATE suggestions SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateSavedStatus(id: Long, isSaved: Boolean)
    
    @Query("UPDATE suggestions SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
}

package com.example.moodfood.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SuggestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SuggestionEntity)

    @Query("SELECT * FROM suggestions")
    fun getAllSuggestions(): Flow<List<SuggestionEntity>>

    @Query("SELECT * FROM suggestions ORDER BY timestamp DESC LIMIT :limit")
    fun recent(limit: Int = 10): Flow<List<SuggestionEntity>>
}

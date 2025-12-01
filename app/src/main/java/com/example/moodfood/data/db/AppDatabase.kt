package com.example.moodfood.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.moodfood.data.auth.UserEntity
import com.example.moodfood.data.auth.UserSessionEntity
import com.example.moodfood.data.auth.UserDao
import com.example.moodfood.data.auth.UserSessionDao
import com.example.moodfood.data.progress.*

@Database(
    entities = [
        SuggestionEntity::class, 
        UserEntity::class, 
        UserSessionEntity::class,
        MoodEntry::class,
        NutritionLog::class,
        AchievementEntity::class,
        UserPreferences::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun suggestionDao(): SuggestionDao
    abstract fun userDao(): UserDao
    abstract fun userSessionDao(): UserSessionDao
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "moodfood.db"
            )
            .fallbackToDestructiveMigration() // Allow destructive migration during development
            .build()
            .also { INSTANCE = it }
        }
    }
}

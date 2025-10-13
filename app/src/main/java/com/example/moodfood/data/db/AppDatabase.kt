package com.example.moodfood.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.moodfood.data.auth.UserEntity
import com.example.moodfood.data.auth.UserSessionEntity
import com.example.moodfood.data.auth.UserDao
import com.example.moodfood.data.auth.UserSessionDao

@Database(
    entities = [SuggestionEntity::class, UserEntity::class, UserSessionEntity::class], 
    version = 2, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun suggestionDao(): SuggestionDao
    abstract fun userDao(): UserDao
    abstract fun userSessionDao(): UserSessionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "moodfood.db"
            ).build().also { INSTANCE = it }
        }
    }
}

package com.example.moodfood.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "settings"

val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val OnboardingDone = booleanPreferencesKey("onboarding_done")
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.OnboardingDone] ?: false
    }

    suspend fun setOnboardingDone(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OnboardingDone] = value
        }
    }
}

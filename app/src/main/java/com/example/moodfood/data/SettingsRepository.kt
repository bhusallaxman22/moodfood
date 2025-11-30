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
        val DarkModeEnabled = booleanPreferencesKey("dark_mode_enabled")
        val DataSharingEnabled = booleanPreferencesKey("data_sharing_enabled")
        val AnalyticsEnabled = booleanPreferencesKey("analytics_enabled")
        val ProfilePublic = booleanPreferencesKey("profile_public")
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.OnboardingDone] ?: false
    }

    val darkModeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DarkModeEnabled] ?: false
    }

    val dataSharingEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DataSharingEnabled] ?: false
    }

    val analyticsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.AnalyticsEnabled] ?: false
    }

    val profilePublic: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ProfilePublic] ?: false
    }

    suspend fun setOnboardingDone(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OnboardingDone] = value
        }
    }

    suspend fun setDarkModeEnabled(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DarkModeEnabled] = value
        }
    }

    suspend fun setDataSharingEnabled(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DataSharingEnabled] = value
        }
    }

    suspend fun setAnalyticsEnabled(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AnalyticsEnabled] = value
        }
    }

    suspend fun setProfilePublic(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ProfilePublic] = value
        }
    }
}

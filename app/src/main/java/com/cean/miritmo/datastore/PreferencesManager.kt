package com.cean.miritmo.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    
    companion object {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_SOUND_URI = stringPreferencesKey("notification_sound_uri")
        val USER_ID = stringPreferencesKey("user_id")
    }

    val isDarkModeFlow: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE]
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }

    val isNotificationsEnabledFlow: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED]
    }

    suspend fun setNotificationsEnabled(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = isEnabled
        }
    }

    val notificationSoundUriFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_SOUND_URI]
    }

    suspend fun setNotificationSoundUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri != null) {
                preferences[NOTIFICATION_SOUND_URI] = uri
            } else {
                preferences.remove(NOTIFICATION_SOUND_URI)
            }
        }
    }

    val userIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    suspend fun setUserId(userId: String?) {
        context.dataStore.edit { preferences ->
            if (userId != null) {
                preferences[USER_ID] = userId
            } else {
                preferences.remove(USER_ID)
            }
        }
    }
}

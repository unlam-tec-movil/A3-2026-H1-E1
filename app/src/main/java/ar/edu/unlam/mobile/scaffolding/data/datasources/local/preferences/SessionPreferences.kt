package ar.edu.unlam.mobile.scaffolding.data.datasources.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings_preferences")

class SessionPreferences(
    private val context: Context,
) {
    companion object {
        private val SESSION_TOKEN = stringPreferencesKey("session_token")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val sessionToken: Flow<String?> = context.dataStore.data.map { it[SESSION_TOKEN] }
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: false }

    suspend fun saveSessionToken(token: String) {
        context.dataStore.edit { it[SESSION_TOKEN] = token }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(SESSION_TOKEN)
        }
    }
}

package com.example.simplenote.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val KEY_ACCESS = stringPreferencesKey("access_token")
        private val KEY_REFRESH = stringPreferencesKey("refresh_token")
    }

    // 🔑 Save both tokens
    suspend fun saveTokens(access: String, refresh: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS] = access
            prefs[KEY_REFRESH] = refresh
        }
    }

    // 🔑 Save only access (when refreshing)
    suspend fun saveAccessToken(access: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS] = access
        }
    }

    // 🔑 Get current access token (null if missing)
    suspend fun getAccessToken(): String? {
        val prefs = context.dataStore.data.map { it[KEY_ACCESS] }.firstOrNull()
        return prefs
    }

    // 🔑 Get current refresh token (null if missing)
    suspend fun getRefreshToken(): String? {
        val prefs = context.dataStore.data.map { it[KEY_REFRESH] }.firstOrNull()
        return prefs
    }

    // 🔑 Flow access token (for reactive UI if needed)
    val accessTokenFlow: Flow<String?> =
        context.dataStore.data.map { it[KEY_ACCESS] }

    // 🔑 Clear tokens (on logout or invalid refresh)
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}

package com.example.simplenote.auth

import android.content.Context
import android.content.SharedPreferences

object TokenStore {
    private const val PREFS = "auth_prefs"
    private const val KEY_ACCESS = "access"
    private const val KEY_REFRESH = "refresh"

    private lateinit var prefs: SharedPreferences
    @Volatile private var cachedAccess: String? = null
    @Volatile private var cachedRefresh: String? = null

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        cachedAccess = prefs.getString(KEY_ACCESS, null)
        cachedRefresh = prefs.getString(KEY_REFRESH, null)
    }

    fun access(): String? = cachedAccess
    fun refresh(): String? = cachedRefresh

    fun saveTokens(access: String, refresh: String) {
        cachedAccess = access
        cachedRefresh = refresh
        prefs.edit().putString(KEY_ACCESS, access).putString(KEY_REFRESH, refresh).apply()
    }

    fun saveAccess(access: String) {
        cachedAccess = access
        prefs.edit().putString(KEY_ACCESS, access).apply()
    }

    fun clear() {
        cachedAccess = null
        cachedRefresh = null
        prefs.edit().clear().apply()
    }
}


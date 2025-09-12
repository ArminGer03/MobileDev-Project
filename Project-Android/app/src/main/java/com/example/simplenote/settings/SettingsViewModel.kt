package com.example.simplenote.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.auth.AuthState
import com.example.simplenote.network.TokenManager
import com.example.simplenote.network.UserInfoResponse
import kotlinx.coroutines.launch
import android.util.Log

data class SettingsUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val user: UserInfoResponse? = null
)

class SettingsViewModel(
    private val repo: SettingsRepository = SettingsRepository(),
    private val tokenManager: TokenManager
) : ViewModel() {

    var uiState = androidx.compose.runtime.mutableStateOf(SettingsUiState())
        private set

    fun loadUserInfo() {
        uiState.value = SettingsUiState(loading = true)
        viewModelScope.launch {
            try {
                val user = repo.getUserInfo()
                Log.i("SettingsVM", "✅ Loaded user info: ${user.email}")
                uiState.value = SettingsUiState(user = user)
            } catch (e: Exception) {
                Log.e("SettingsVM", "❌ Failed to load user info: ${e.message}", e)
                uiState.value = SettingsUiState(error = e.message ?: "Failed to load user info")

            }
        }
    }


    fun logout() {
        viewModelScope.launch { tokenManager.clear() }
        AuthState.triggerLogout()
    }

    fun clearError() {
        uiState.value = uiState.value.copy(error = null)
    }
}

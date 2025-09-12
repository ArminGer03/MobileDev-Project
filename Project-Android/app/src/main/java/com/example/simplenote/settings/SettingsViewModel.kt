package com.example.simplenote.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.network.UserInfoResponse
import kotlinx.coroutines.launch

data class SettingsUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val user: UserInfoResponse? = null,
    val loggedOut: Boolean = false
)

class SettingsViewModel(
    private val repo: SettingsRepository = SettingsRepository()
) : ViewModel() {

    var uiState = androidx.compose.runtime.mutableStateOf(SettingsUiState())
        private set

    fun loadUserInfo(accessToken: String) {
        uiState.value = SettingsUiState(loading = true)
        viewModelScope.launch {
            try {
                val user = repo.getUserInfo(accessToken)
                uiState.value = SettingsUiState(user = user)
            } catch (e: Exception) {
                uiState.value = SettingsUiState(error = e.message ?: "Failed to load user info")
            }
        }
    }

    fun logout() {
        // In a real app you might clear tokens from DataStore/SharedPrefs
        uiState.value = uiState.value.copy(loggedOut = true)
    }

    fun clearError() {
        uiState.value = uiState.value.copy(error = null)
    }


    fun clearLogout() {
        uiState.value = uiState.value.copy(loggedOut = false)
    }


}

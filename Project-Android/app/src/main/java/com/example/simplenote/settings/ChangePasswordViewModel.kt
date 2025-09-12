package com.example.simplenote.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class ChangePasswordUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class ChangePasswordViewModel(
    private val repo: ChangePasswordRepository = ChangePasswordRepository()
) : ViewModel() {

    var uiState = androidx.compose.runtime.mutableStateOf(ChangePasswordUiState())
        private set

    fun changePassword(accessToken: String, oldPassword: String, newPassword: String) {
        uiState.value = ChangePasswordUiState(loading = true)
        viewModelScope.launch {
            try {
                repo.changePassword(accessToken, oldPassword, newPassword)
                uiState.value = ChangePasswordUiState(success = true)
            } catch (e: Exception) {
                uiState.value = ChangePasswordUiState(
                    error = e.message ?: "Failed to change password"
                )
            }
        }
    }

    fun clearError() {
        uiState.value = uiState.value.copy(error = null)
    }

    fun reset() {
        uiState.value = ChangePasswordUiState()
    }
}

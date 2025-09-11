package com.example.simplenote.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.network.TokenResponse
import kotlinx.coroutines.launch

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val token: TokenResponse? = null
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    var uiState = androidx.compose.runtime.mutableStateOf(LoginUiState())
        private set

    fun login(username: String, password: String) {
        uiState.value = uiState.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val token = repo.login(username, password)
                uiState.value = LoginUiState(loading = false, token = token)
            } catch (e: Exception) {
                uiState.value = LoginUiState(
                    loading = false,
                    error = e.message ?: "Login failed"
                )
            }
        }
    }

    fun clearError() { uiState.value = uiState.value.copy(error = null) }
}

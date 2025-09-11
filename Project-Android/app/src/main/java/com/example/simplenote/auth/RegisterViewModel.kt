package com.example.simplenote.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.network.RegisterResponse
import kotlinx.coroutines.launch

data class RegisterUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: RegisterResponse? = null
)

class RegisterViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    var uiState = androidx.compose.runtime.mutableStateOf(RegisterUiState())
        private set

    fun register(username: String, password: String, email: String, first: String, last: String) {
        uiState.value = RegisterUiState(loading = true)
        viewModelScope.launch {
            try {
                val res = repo.register(username, password, email, first, last)
                uiState.value = RegisterUiState(loading = false, success = res)
            } catch (e: Exception) {
                uiState.value = RegisterUiState(loading = false, error = e.message ?: "Registration failed")
            }
        }
    }

    fun clearError() { uiState.value = uiState.value.copy(error = null) }
}

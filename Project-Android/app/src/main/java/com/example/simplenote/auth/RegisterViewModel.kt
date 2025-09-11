package com.example.simplenote.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplenote.network.RegisterResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ApiFieldError(val code: String?, val detail: String?, val attr: String?)
data class ApiErrorResponse(val type: String?, val errors: List<ApiFieldError>?)

data class RegisterUiState(
    val loading: Boolean = false,
    val error: String? = null,                           // non-field/global error
    val fieldErrors: Map<String, List<String>> = emptyMap(), // attr -> messages
    val success: RegisterResponse? = null
)

class RegisterViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val moshi by lazy {
        Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    }
    private val errAdapter by lazy { moshi.adapter(ApiErrorResponse::class.java) }

    var uiState = androidx.compose.runtime.mutableStateOf(RegisterUiState())
        private set

    fun register(username: String, password: String, email: String, first: String, last: String) {
        uiState.value = RegisterUiState(loading = true)
        viewModelScope.launch {
            try {
                val res = repo.register(username, password, email, first, last)
                uiState.value = RegisterUiState(loading = false, success = res)
            } catch (e: HttpException) {
                // Try to parse DRF-style validation error
                val raw = e.response()?.errorBody()?.string()
                val parsed = raw?.let { runCatching { errAdapter.fromJson(it) }.getOrNull() }
                if (parsed?.errors?.isNotEmpty() == true) {
                    val map = parsed.errors
                        .groupBy({ it.attr ?: "general" }) { it.detail ?: it.code ?: "Invalid value" }
                    uiState.value = RegisterUiState(loading = false, fieldErrors = map)
                } else {
                    uiState.value = RegisterUiState(loading = false, error = "Registration failed (${e.code()})")
                }
            } catch (e: Exception) {
                uiState.value = RegisterUiState(loading = false, error = e.message ?: "Registration failed")
            }
        }
    }

    fun clearError() { uiState.value = uiState.value.copy(error = null) }
    fun clearFieldErrorsFor(attr: String) {
        val cur = uiState.value.fieldErrors.toMutableMap()
        if (cur.remove(attr) != null) {
            uiState.value = uiState.value.copy(fieldErrors = cur)
        }
    }

    fun consumeSuccess() {
        uiState.value = uiState.value.copy(success = null)
    }
}

package com.example.simplenote.auth

import com.example.simplenote.network.ApiClient
import com.example.simplenote.network.LoginRequest
import com.example.simplenote.network.TokenResponse

class AuthRepository {
    suspend fun login(username: String, password: String): TokenResponse =
        ApiClient.api.login(LoginRequest(username, password))
}

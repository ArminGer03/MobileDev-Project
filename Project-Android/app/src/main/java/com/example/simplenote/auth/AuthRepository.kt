package com.example.simplenote.auth

import com.example.simplenote.network.*

class AuthRepository {
    suspend fun login(username: String, password: String): TokenResponse =
        ApiClient.api.login(LoginRequest(username, password))

    suspend fun register(
        username: String,
        password: String,
        email: String,
        first: String,
        last: String
    ): RegisterResponse =
        ApiClient.api.register(RegisterRequest(username, password, email, first, last))
}

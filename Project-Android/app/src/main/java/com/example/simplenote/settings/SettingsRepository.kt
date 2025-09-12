package com.example.simplenote.settings


import com.example.simplenote.network.ApiClient
import com.example.simplenote.network.UserInfoResponse

class SettingsRepository {
    suspend fun getUserInfo(accessToken: String): UserInfoResponse {
        return ApiClient.api.getUserInfo("Bearer $accessToken")
    }
}

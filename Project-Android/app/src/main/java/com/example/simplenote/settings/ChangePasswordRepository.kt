package com.example.simplenote.settings

import com.example.simplenote.network.ApiClient
import com.example.simplenote.network.ChangePasswordRequest

class ChangePasswordRepository {
    suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ) {
        ApiClient.api.changePassword(
            ChangePasswordRequest(
                oldPassword,
                newPassword,
            )
        )
    }
}

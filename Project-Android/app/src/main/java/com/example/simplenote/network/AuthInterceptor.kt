package com.example.simplenote.network

import android.util.Log
import com.example.simplenote.auth.AuthState
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val api: ApiService
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val accessToken = runBlocking { tokenManager.getAccessToken() }

        Log.d("AuthInterceptor", "→ Requesting ${request.url} with access=${accessToken?.take(10)}...")

        if (!accessToken.isNullOrBlank()) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        }

        val response = chain.proceed(request)

        Log.d("AuthInterceptor", "← Response ${response.code} for ${request.url}")

        if (response.code == 401) {
            Log.w("AuthInterceptor", "⚠️ Got 401 for ${request.url}, trying refresh...")

            response.close()
            val refreshToken = runBlocking { tokenManager.getRefreshToken() }
            Log.d("AuthInterceptor", "Stored refresh token=${refreshToken?.take(10)}...")

            if (refreshToken.isNullOrBlank()) {
                Log.e("AuthInterceptor", "❌ No refresh token → logging out")
                runBlocking { tokenManager.clear() }
                AuthState.triggerLogout()
                return response
            }

            try {
                val refreshResp = runBlocking { api.refresh(RefreshRequest(refreshToken)) }
                Log.i("AuthInterceptor", "✅ Refresh succeeded, new access=${refreshResp.access.take(10)}...")

                runBlocking { tokenManager.saveAccessToken(refreshResp.access) }

                // retry with new access
                val newRequest = request.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer ${refreshResp.access}")
                    .build()
                val newResponse = chain.proceed(newRequest)
                Log.d("AuthInterceptor", "↩️ Retried ${newRequest.url}, result=${newResponse.code}")

                if (newResponse.code == 401) {
                    Log.e("AuthInterceptor", "❌ Retry also 401 → clearing tokens & logout")
                    runBlocking { tokenManager.clear() }
                    AuthState.triggerLogout()
                }
                return newResponse
            } catch (e: Exception) {
                Log.e("AuthInterceptor", "❌ Refresh failed: ${e.message}", e)
                runBlocking { tokenManager.clear() }
                AuthState.triggerLogout()
                return response
            }
        }

        return response
    }
}

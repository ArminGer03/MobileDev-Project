package com.example.simplenote.network

import com.example.simplenote.auth.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response


class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val api: ApiService // your retrofit interface
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        // Attach access token
        val access = runBlocking { tokenManager.accessToken.firstOrNull() }
        access?.let {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $it")
                .build()
        }

        var response = chain.proceed(request)

        if (response.code == 401) {
            response.close()
            // try refresh
            val refresh = runBlocking { tokenManager.refreshToken.firstOrNull() }
            if (!refresh.isNullOrBlank()) {
                val newTokens =
                    runBlocking { api.refresh(RefreshRequest(refresh)) } // call your refresh endpoint
                if (newTokens.access.isNotBlank()) {
                        runBlocking {
                            tokenManager.saveTokens(newTokens.access, "")
                        }
                        // retry request with new token
                        val newRequest = request.newBuilder()
                            .header("Authorization", "Bearer ${newTokens.access}")
                            .build()
                        response = chain.proceed(newRequest)

                }
            }
        }
        return response
    }
}

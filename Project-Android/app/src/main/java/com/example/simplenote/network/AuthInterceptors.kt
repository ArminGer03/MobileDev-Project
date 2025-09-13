package com.example.simplenote.network

import com.example.simplenote.auth.TokenStore
import com.example.simplenote.auth.TokenStore.refresh
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/** Adds Authorization header if we have an access token. */
class AuthHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val access = TokenStore.access()
        return if (access.isNullOrBlank()) {
            chain.proceed(original)
        } else {
            val req = original.newBuilder()
                .header("Authorization", "Bearer $access")
                .build()
            chain.proceed(req)
        }
    }
}

/** Refreshes access token on 401 using the stored refresh token, then retries once. */
class TokenRefreshAuthenticator(
    private val baseUrl: String
) : Authenticator {

    // separate client WITHOUT our interceptors/authenticator to avoid loops
    private val refreshApi: ApiService by lazy {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .build()
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        val resp = refreshApi.refreshSync(RefreshRequest(refresh())).execute()
        if (!resp.isSuccessful) {
            TokenStore.clear()
            return null
        }
        val newAccess = resp.body()?.access ?: return null
        TokenStore.saveAccess(newAccess)
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .build()
    }
}

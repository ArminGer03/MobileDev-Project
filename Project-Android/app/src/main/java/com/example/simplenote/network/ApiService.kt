package com.example.simplenote.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Use 10.0.2.2 for Android emulator
private const val BASE_URL = "http://10.0.2.2:8000/"

data class LoginRequest(val username: String, val password: String)
data class TokenResponse(val access: String, val refresh: String)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val first_name: String,
    val last_name: String
)
data class RegisterResponse(
    val username: String,
    val email: String,
    val first_name: String,
    val last_name: String
)

interface ApiService {
    @POST("api/auth/token/")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("api/auth/register/")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse
}

object ApiClient {
    val api: ApiService by lazy {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logger).build()
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}

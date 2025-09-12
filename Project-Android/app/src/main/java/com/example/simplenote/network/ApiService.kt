package com.example.simplenote.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "http://10.0.2.2:8000/"

// -------- Auth --------
data class LoginRequest(val username: String, val password: String)
data class TokenResponse(val access: String, val refresh: String)
data class ChangePasswordRequest(val old_password: String, val new_password: String)
data class ChangePasswordResponse(val detail: String)
data class RefreshRequest(val refresh: String)
data class RefreshResponse(val access: String)


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

data class UserInfoResponse(
    val id: Long,
    val username: String,
    val email: String,
    val first_name: String,
    val last_name: String
)

// -------- Notes --------
data class CreateNoteRequest(val title: String, val description: String)
data class UpdateNoteRequest(val title: String? = null, val description: String? = null)

data class NoteDto(
    val id: Long,
    val title: String,
    val description: String,
    val created_at: String,
    val updated_at: String,
    val creator_name: String?,
    val creator_username: String?
)

data class PagedNotesResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<NoteDto>
)

interface ApiService {
    // Auth
    @POST("api/auth/change-password/")
    suspend fun changePassword(
        @Body body: ChangePasswordRequest,
        @Header("Authorization") auth: String
    ): ChangePasswordResponse

    @POST("api/auth/token/")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("api/auth/register/")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST("api/auth/token/refresh/")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse

    @POST("api/auth/userinfo/")
    suspend fun getUserInfo(@Header("Authorization") auth: String): UserInfoResponse

    // Notes
    @GET("api/notes/{id}/")
    suspend fun getNote(
        @Path("id") id: Long,
        @Header("Authorization") auth: String
    ): NoteDto

    @POST("api/notes/")
    suspend fun createNote(
        @Body body: CreateNoteRequest,
        @Header("Authorization") auth: String
    ): NoteDto

    @PATCH("api/notes/{id}/")
    suspend fun updateNote(
        @Path("id") id: Long,
        @Body body: UpdateNoteRequest,
        @Header("Authorization") auth: String
    ): NoteDto

    @DELETE("api/notes/{id}/")
    suspend fun deleteNote(
        @Path("id") id: Long,
        @Header("Authorization") auth: String
    )

    @GET("api/notes/")
    suspend fun listNotesPaged(
        @Header("Authorization") auth: String,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): PagedNotesResponse

    @GET("api/notes/filter")
    suspend fun filterNotesPaged(
        @Header("Authorization") auth: String,
        @Query("title") title: String? = null,
        @Query("description") description: String? = null,
        @Query("updated__gte") updatedGte: String? = null,
        @Query("updated__lte") updatedLte: String? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): PagedNotesResponse
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

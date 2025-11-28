package pt.ipt.weathersense.network

import pt.ipt.weathersense.models.AuthRequest
import pt.ipt.weathersense.models.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>
}
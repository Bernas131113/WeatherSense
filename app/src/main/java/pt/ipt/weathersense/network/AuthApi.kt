package pt.ipt.weathersense.network

import pt.ipt.weathersense.models.AuthRequest
import pt.ipt.weathersense.models.AuthResponse
import pt.ipt.weathersense.models.AddFavoriteRequest
import pt.ipt.weathersense.models.FavoritesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

interface AuthApi {
    @POST("/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("/add-favorite")
    suspend fun addFavorite(@Body request: AddFavoriteRequest): Response<AuthResponse>

    @GET("/favorites/{userId}")
    suspend fun getFavorites(@Path("userId") userId: String): Response<FavoritesResponse>

    @POST("/remove-favorite")
    suspend fun removeFavorite(@Body request: AddFavoriteRequest): Response<AuthResponse>

}
package pt.ipt.weathersense.network

import pt.ipt.weathersense.models.AuthRequest
import pt.ipt.weathersense.models.AuthResponse
import pt.ipt.weathersense.models.AddFavoriteRequest
import pt.ipt.weathersense.models.FavoritesResponse
import pt.ipt.weathersense.models.UpdatePasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

// Interface que define os endpoints da API REST
interface AuthApi {

    // Regista um novo utilizador.
    // @Body envia os dados (user + pass) no corpo do pedido HTTP
    @POST("/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    // Efetua o login.
    // 'suspend' indica que a função corre numa Coroutine (assíncrona)
    @POST("/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    // Adiciona uma cidade aos favoritos do utilizador
    @POST("/add-favorite")
    suspend fun addFavorite(@Body request: AddFavoriteRequest): Response<AuthResponse>

    // Obtém a lista de favoritos.
    // @Path substitui o {userId} no URL pelo valor real passado na função
    @GET("/favorites/{userId}")
    suspend fun getFavorites(@Path("userId") userId: String): Response<FavoritesResponse>

    // Remove uma cidade dos favoritos
    @POST("/remove-favorite")
    suspend fun removeFavorite(@Body request: AddFavoriteRequest): Response<AuthResponse>

    // Atualiza a palavra-passe do utilizador
    @POST("/update-password")
    suspend fun updatePassword(@Body request: UpdatePasswordRequest): Response<AuthResponse>
}
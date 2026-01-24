package pt.ipt.weathersense.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://weather-auth-backend-ea1b.onrender.com/"

    val instance: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Adiciona o conversor Gson para transformar automaticamente o JSON da API em objetos Kotlin
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}
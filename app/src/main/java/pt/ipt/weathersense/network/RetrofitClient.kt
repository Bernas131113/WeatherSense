package pt.ipt.weathersense.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 10.0.2.2 é o IP especial que o emulador Android usa para aceder ao localhost do pc
    //se quiser testar no telemovel, tenho de mudar o ip para o ip do meu pc
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val instance: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}
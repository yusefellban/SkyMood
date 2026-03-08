package iti.yousef.skymood.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


public object RetrofitClient {

    private const val BASE_URL = "https://api.openweathermap.org/"

    /** Lazily builds the Retrofit instance with Gson converter */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }
}

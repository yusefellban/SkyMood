package iti.yousef.skymood.data.remote
import iti.yousef.skymood.data.model.ForecastResponse

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    /**
     * Fetches the 5-day forecast for a given location.
     * @param lat Latitude of the location
     * @param lon Longitude of the location
     * @param units Measurement units: "metric", "imperial", or "standard" (Kelvin)
     * @param lang Language code for descriptions: "en", "ar", etc.
     * @param appId API key for authentication
     */
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en",
        @Query("appid") appId: String
    ): ForecastResponse
}

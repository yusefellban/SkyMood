package iti.yousef.skymood.data.remote

import iti.yousef.skymood.data.model.ForecastResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Retrofit implementation of WeatherRemoteDataSource that transforms suspend calls into Flow.
 */
class RetrofitWeatherRemoteDataSource(
    private val apiService: WeatherApiService
) : WeatherRemoteDataSource {
    override fun getForecast(
        lat: Double,
        lon: Double,
        units: String,
        lang: String,
        appId: String
    ): Flow<ForecastResponse> = flow {
        val response = apiService.getForecast(
            lat = lat,
            lon = lon,
            units = units,
            lang = lang,
            appId = appId
        )
        emit(response)
    }
}

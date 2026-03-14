package iti.yousef.skymood.data.remote

import iti.yousef.skymood.data.model.ForecastResponse
import kotlinx.coroutines.flow.Flow

/**
 * Interface for remote weather data operations.
 */
interface WeatherRemoteDataSource {
    fun getForecast(
        lat: Double,
        lon: Double,
        units: String,
        lang: String,
        appId: String
    ): Flow<ForecastResponse>
}

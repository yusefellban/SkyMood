package iti.yousef.skymood.data.repository

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.gson.Gson
import iti.yousef.skymood.data.local.FavoriteLocationEntity
import iti.yousef.skymood.data.local.ForecastEntity
import iti.yousef.skymood.data.local.WeatherDao
import iti.yousef.skymood.data.utils.NetworkHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import iti.yousef.skymood.data.model.ForecastResponse
import iti.yousef.skymood.data.remote.WeatherRemoteDataSource
import iti.yousef.skymood.BuildConfig

class WeatherRepository(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val weatherDao: WeatherDao,
    private val networkHandler: NetworkHandler
) {
    private val gson = Gson()
    companion object {
        const val API_KEY =BuildConfig.WEATHER_API
        const val TAG="Weather Repository TAG"
    }

    /**
     * Fetches 5-day forecast for the given coordinates as a Flow.
     * If online: calls the remote data source, caches the response, and emits it.
     * If offline: emits the most recent cached data for this location.
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getForecast(
        lat: Double,
        lon: Double,
        units: String = "metric",
        lang: String = "en"
    ): Flow<ForecastResponse> = flow {

        val locationKey = "${lat}_${lon}"

        if (networkHandler.isNetworkAvailable()) {
            try {
                Log.d(TAG, "getForecast: lat=$lat, lon=$lon, units=$units, lang=$lang")
                
                // Fetch from remote via Flow
                remoteDataSource.getForecast(
                    lat = lat,
                    lon = lon,
                    units = units,
                    lang = lang,
                    appId = API_KEY
                ).collect { response ->
                    // Cache the result
                    val json = gson.toJson(response)
                    weatherDao.insertForecast(
                        ForecastEntity(
                            locationKey = locationKey,
                            jsonData = json,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    emit(response)
                }
            } catch (e: Exception) {
                // If remote fetch fails, try cache
                val cached = getCachedForecast(locationKey)
                if (cached != null) {
                    emit(cached)
                } else {
                    throw Exception("Failed to fetch weather data: ${e.message}")
                }
            }
        } else {
            // Offline — read from cache
            val cached = getCachedForecast(locationKey)
            if (cached != null) {
                emit(cached)
            } else {
                throw Exception("No internet connection and no cached data available")
            }
        }
    }

    private suspend fun getCachedForecast(locationKey: String): ForecastResponse? {
        val entity = weatherDao.getForecast(locationKey) ?: return null
        return try {
            gson.fromJson(entity.jsonData, ForecastResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Favorites
    fun getAllFavorites(): Flow<List<FavoriteLocationEntity>> = weatherDao.getAllFavorites()

    suspend fun insertFavorite(favorite: FavoriteLocationEntity) {
        weatherDao.insertFavorite(favorite)
    }

    suspend fun deleteFavorite(favorite: FavoriteLocationEntity) {
        weatherDao.deleteFavorite(favorite)
    }
}

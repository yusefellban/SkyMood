package iti.yousef.skymood.data.repository

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.gson.Gson
import iti.yousef.skymood.data.local.ForecastEntity
import iti.yousef.skymood.data.local.WeatherDao
import iti.yousef.skymood.data.model.ForecastResponse
import iti.yousef.skymood.data.remote.WeatherApiService
import iti.yousef.skymood.BuildConfig

class WeatherRepository(
    private val apiService: WeatherApiService,
    private val weatherDao: WeatherDao,
    private val context: Context
) {
    private val gson = Gson()
    companion object {
        const val API_KEY =BuildConfig.WEATHER_API
        const val TAG="Weather Repository TAG"
    }

    /**
     *
     * Fetches 5-day forecast for the given coordinates.
     * If online: calls the API, caches the response, and returns it.
     * If offline: returns the most recent cached data for this location.
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    suspend fun getForecast(
        lat: Double,
        lon: Double,
        units: String = "metric",
        lang: String = "en"
    ): ForecastResponse {

        val locationKey = "${lat}_${lon}"

        return if (isNetworkAvailable()) {
            try {
                Log.d(TAG, "getForecast:   " +
                        "                  lat = ${lat},\n" +
                        "                    lon = ${lon},\n" +
                        "                    units = ${units},\n" +
                        "                    lang = ${lang},\n" +
                        "                    appId = ${API_KEY} ")
                // Fetch from API
                val response = apiService.getForecast(
                    lat = lat,
                    lon = lon,
                    units = units,
                    lang = lang,
                    appId = API_KEY
                )

                // Cache the result
                val json = gson.toJson(response)
                weatherDao.insertForecast(
                    ForecastEntity(
                        locationKey = locationKey,
                        jsonData = json,
                        timestamp = System.currentTimeMillis()
                    )
                )
                response
            } catch (e: Exception) {
                // If network call fails, try cache
                getCachedForecast(locationKey)
                    ?: throw Exception("Failed to fetch weather data: ${e.message}")
            }
        } else {
            // Offline — read from cache
            getCachedForecast(locationKey)
                ?: throw Exception("No internet connection and no cached data available")
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


    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

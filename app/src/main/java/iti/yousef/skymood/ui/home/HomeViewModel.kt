package iti.yousef.skymood.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.model.WeatherUiState
import iti.yousef.skymood.data.settings.SettingsPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for the Home screen.
 * Fetches the user's location and weather data, exposing it via StateFlow.
 * Respects user settings for temperature units and language.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private  val TAG ="HomeViewModel"
    private val app = application as SkyMood
    private val repository = app.repository
    private val settingsDataStore = app.settingsDataStore
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    /** Observable weather UI state for the Home screen composable */
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()
    private val _settings = MutableStateFlow(SettingsPreferences())
    /** Observable settings for unit/language display */
    val settings: StateFlow<SettingsPreferences> = _settings.asStateFlow()

    init {
        // Observe settings changes
        viewModelScope.launch {
            settingsDataStore.settingsFlow.collect { prefs ->
                _settings.value = prefs
            }
        }
        // Fetch weather on init
        fetchWeather()
    }

    @SuppressLint("MissingPermission")
    fun fetchWeather() {
        _weatherState.value = WeatherUiState.Loading
        viewModelScope.launch {
            try {
                val currentSettings = settingsDataStore.settingsFlow.first()
                val location = getCurrentLocation()
                if (location != null) {
                    val forecast = repository.getForecast(
                        lat = location.latitude,
                        lon = location.longitude,
                        units = currentSettings.temperatureUnit.apiValue,
                        lang = currentSettings.language.apiValue,
                    )
                    _weatherState.value = WeatherUiState.Success(forecast)
                } else {
                    _weatherState.value = WeatherUiState.Error(
                        "Unable to get your location. Please enable GPS and try again."
                    )
                }
            } catch (e: Exception) {
                _weatherState.value = WeatherUiState.Error(
                    e.message ?: "An unexpected error occurred"
                )
                Log.d(TAG, "fetchWeather: "+e.message)

            }
        }
    }


    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): Location? {
        return try {
            val cancellationToken = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).await()
        } catch (e: Exception) {
            null
        }
    }
}

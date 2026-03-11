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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
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
        // Observe settings changes and trigger weather fetch
        viewModelScope.launch {
            settingsDataStore.settingsFlow
                .distinctUntilChanged { old, new ->
                    old.temperatureUnit == new.temperatureUnit &&
                    old.language == new.language &&
                    old.locationMethod == new.locationMethod &&
                    old.customLat == new.customLat &&
                    old.customLon == new.customLon
                }
                .collectLatest { prefs ->
                    _settings.value = prefs
                    fetchWeather()
                }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchWeather() {
        _weatherState.value = WeatherUiState.Loading
        viewModelScope.launch {
            try {
                val currentSettings = settingsDataStore.settingsFlow.first()
                val lat: Double
                val lon: Double

                if (currentSettings.locationMethod == iti.yousef.skymood.data.settings.LocationMethod.MAP &&
                    currentSettings.customLat != null && currentSettings.customLon != null) {
                    lat = currentSettings.customLat
                    lon = currentSettings.customLon
                } else {
                    val location = getCurrentLocation()
                    if (location != null) {
                        lat = location.latitude
                        lon = location.longitude
                    } else {
                        _weatherState.value = WeatherUiState.Error(
                            "Unable to get your location. Please enable GPS and try again."
                        )
                        return@launch
                    }
                }

                val forecast = repository.getForecast(
                    lat = lat,
                    lon = lon,
                    units = currentSettings.temperatureUnit.apiValue,
                    lang = currentSettings.language.apiValue,
                )
                _weatherState.value = WeatherUiState.Success(forecast)
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

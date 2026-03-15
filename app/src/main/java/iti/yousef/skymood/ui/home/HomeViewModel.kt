package iti.yousef.skymood.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.model.WeatherUiState
import iti.yousef.skymood.data.settings.LocationMethod
import iti.yousef.skymood.data.settings.SettingsPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * ViewModel for the Home screen.
 * Fetches the user's location and weather data, exposing it via StateFlow.
 * Respects user settings for temperature units and language.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private  val TAG ="HomeViewModel"
    private val app = application as SkyMood
    private val weatherRepository = app.weatherRepository
    private val settingsRepository = app.settingsRepository
    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    /** Observable weather UI state for the Home screen composable */
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()
    private val _settings = MutableStateFlow(SettingsPreferences())
    /** Observable settings for unit/language display */
    val settings: StateFlow<SettingsPreferences> = _settings.asStateFlow()
    
    private val _isFavorite = MutableStateFlow(false)
    /** Whether the current city displayed is in the favorites list */
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    init {
        // Observe settings changes and trigger weather fetch
        viewModelScope.launch {
            settingsRepository.settingsFlow
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

        // Observe favorites to see if current city is favorited
        viewModelScope.launch {
            combine(weatherRepository.getAllFavorites(), _weatherState) { favorites, state ->
                if (state is WeatherUiState.Success) {
                    favorites.any { it.cityName == state.data.city.name }
                } else {
                    false
                }
            }.collect { favorited ->
                _isFavorite.value = favorited
            }
        }
    }

    fun fetchWeather() {
        _weatherState.value = WeatherUiState.Loading
        viewModelScope.launch {
            try {
                val currentSettings = settingsRepository.settingsFlow.first()
                val lat: Double
                val lon: Double

                if (currentSettings.locationMethod == LocationMethod.MAP &&
                    currentSettings.customLat != null && currentSettings.customLon != null) {
                    lat = currentSettings.customLat
                    lon = currentSettings.customLon
                } else {
                    val location = app.locationRepository.getCurrentLocation()
                    if (location != null) {
                        lat = location.latitude
                        lon = location.longitude
                    } else {
                        // Attempt to load from cache if location is missing
                        val cachedForecast = weatherRepository.getLatestCachedForecast()
                        if (cachedForecast != null) {
                            _weatherState.value = WeatherUiState.Success(cachedForecast)
                            return@launch
                        }

                        _weatherState.value = WeatherUiState.Error(
                            "Unable to get your location. Please enable GPS and try again."
                        )
                        return@launch
                    }
                }

                weatherRepository.getForecast(
                    lat = lat,
                    lon = lon,
                    units = currentSettings.temperatureUnit.apiValue,
                    lang = currentSettings.language.apiValue,
                ).collect { forecast ->
                    _weatherState.value = WeatherUiState.Success(forecast)
                }
            } catch (e: Exception) {
                _weatherState.value = WeatherUiState.Error(
                    e.message ?: "An unexpected error occurred"
                )
                Log.d(TAG, "fetchWeather: "+e.message)
            }
        }
    }

    fun toggleFavorite() {
        val state = _weatherState.value
        if (state is WeatherUiState.Success) {
            viewModelScope.launch {
                val currentFavorites = weatherRepository.getAllFavorites().first()
                val cityName = state.data.city.name
                val existing = currentFavorites.find { it.cityName == cityName }
                
                if (existing != null) {
                    weatherRepository.deleteFavorite(existing)
                } else {
                    weatherRepository.insertFavorite(
                        iti.yousef.skymood.data.local.FavoriteLocationEntity(
                            cityName = cityName,
                            latitude = state.data.city.coord.lat,
                            longitude = state.data.city.coord.lon
                        )
                    )
                }
            }
        }
    }
}

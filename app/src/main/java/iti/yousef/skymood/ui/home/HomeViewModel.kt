package iti.yousef.skymood.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.local.settings.LocationMethod
import iti.yousef.skymood.data.local.settings.SettingsPreferences
import iti.yousef.skymood.data.model.FavoriteLocationEntity
import iti.yousef.skymood.data.model.WeatherUiState
import iti.yousef.skymood.data.model.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    /** One-time UI events like snackbars or navigation */
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()
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
                // 1. Check Internet Connection
                val networkHandler = iti.yousef.skymood.data.utils.AndroidNetworkHandler(app)
                val isOnline = networkHandler.isNetworkAvailable()

                val currentSettings = settingsRepository.settingsFlow.first()
                val lat: Double
                val lon: Double

                // 2. Determine Location
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
                        // If location is missing, try cache first
                        val cachedForecast = weatherRepository.getLatestCachedForecast()
                        if (cachedForecast != null) {
                            _weatherState.value = WeatherUiState.Success(cachedForecast)
                            // If we were supposed to be online but aren't, maybe show a toast?
                            if (!isOnline) {
                                _events.emit(UiEvent.ShowSnackbar("Showing cached data. You are offline."))
                            }
                            return@launch
                        }

                        // No location and no cache -> show explicit Location error
                        val msg = "Location services are disabled. Please enable GPS to see your local weather."
                        _weatherState.value = WeatherUiState.Error("LOCATION_DISABLED")
                        _events.emit(UiEvent.ShowSnackbar(msg))
                        return@launch
                    }
                }

                // 3. Fetch from repository
                if (!isOnline) {
                    val cachedForecast = weatherRepository.getLatestCachedForecast()
                    if (cachedForecast != null) {
                        _weatherState.value = WeatherUiState.Success(cachedForecast)
                        _events.emit(UiEvent.ShowSnackbar("Showing cached data. You are offline."))
                        return@launch
                    } else {
                        _weatherState.value = WeatherUiState.Error("OFFLINE")
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
                val errorMessage = e.message ?: "An unexpected error occurred"
                // Check if it's a network-related exception
                if (e is java.net.UnknownHostException || e is java.net.ConnectException) {
                     _weatherState.value = WeatherUiState.Error("OFFLINE")
                } else {
                     _weatherState.value = WeatherUiState.Error(errorMessage)
                }
                _events.emit(UiEvent.ShowSnackbar(errorMessage))
                Log.d(TAG, "fetchWeather: " + errorMessage)
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
                    _events.emit(UiEvent.ShowSnackbar("${existing.cityName} removed from favorites"))
                } else {
                    val entity = FavoriteLocationEntity(
                        cityName = cityName,
                        latitude = state.data.city.coord.lat,
                        longitude = state.data.city.coord.lon
                    )
                    weatherRepository.insertFavorite(entity)
                    _events.emit(UiEvent.ShowSnackbar("${entity.cityName} added to favorites"))
                }
            }
        }
    }
}

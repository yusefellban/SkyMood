package iti.yousef.skymood.data.model

/**
 * to Hold UI state
 */
public sealed class WeatherUiState {
    data object Loading : WeatherUiState()
    data class Success(val data: ForecastResponse) : WeatherUiState()

    data class Error(val message: String) : WeatherUiState()
}

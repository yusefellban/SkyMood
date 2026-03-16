package iti.yousef.skymood.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.local.settings.Language
import iti.yousef.skymood.data.local.settings.LocationMethod
import iti.yousef.skymood.data.local.settings.SettingsPreferences
import iti.yousef.skymood.data.local.settings.TempUnit
import iti.yousef.skymood.data.local.settings.WindUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SkyMood
    private val settingsRepository = app.settingsRepository

    val settings: StateFlow<SettingsPreferences> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsPreferences()
        )

    fun updateTemperatureUnit(unit: TempUnit) {
        viewModelScope.launch {
            settingsRepository.setTempUnit(unit)
        }
    }

    fun updateWindSpeedUnit(unit: WindUnit) {
        viewModelScope.launch {
            settingsRepository.setWindUnit(unit)
        }
    }

    fun updateLanguage(language: Language) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }

    fun updateLocationMethod(method: LocationMethod) {
        viewModelScope.launch {
            settingsRepository.setLocationMethod(method)
        }
    }

    fun updateCustomLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            settingsRepository.setCustomLocation(lat, lon)
        }
    }
}

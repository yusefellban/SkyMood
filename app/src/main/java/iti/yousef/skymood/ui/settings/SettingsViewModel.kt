package iti.yousef.skymood.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.settings.Language
import iti.yousef.skymood.data.settings.LocationMethod
import iti.yousef.skymood.data.settings.SettingsPreferences
import iti.yousef.skymood.data.settings.TempUnit
import iti.yousef.skymood.data.settings.WindUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SkyMood
    private val settingsDataStore = app.settingsDataStore

    val settings: StateFlow<SettingsPreferences> = settingsDataStore.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsPreferences()
        )

    fun updateTemperatureUnit(unit: TempUnit) {
        viewModelScope.launch {
            settingsDataStore.setTempUnit(unit)
        }
    }

    fun updateWindSpeedUnit(unit: WindUnit) {
        viewModelScope.launch {
            settingsDataStore.setWindUnit(unit)
        }
    }

    fun updateLanguage(language: Language) {
        viewModelScope.launch {
            settingsDataStore.setLanguage(language)
        }
    }

    fun updateLocationMethod(method: LocationMethod) {
        viewModelScope.launch {
            settingsDataStore.setLocationMethod(method)
        }
    }
}

package iti.yousef.skymood.data.repository

import iti.yousef.skymood.data.settings.Language
import iti.yousef.skymood.data.settings.LocationMethod
import iti.yousef.skymood.data.settings.SettingsDataStore
import iti.yousef.skymood.data.settings.SettingsPreferences
import iti.yousef.skymood.data.settings.TempUnit
import iti.yousef.skymood.data.settings.WindUnit
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstracting the Settings DataStore from the UI/ViewModel layer.
 * Enforces Strict MVVM by not exposing the DataStore object itself.
 */
class SettingsRepository(
    private val settingsDataStore: SettingsDataStore
) {
    val settingsFlow: Flow<SettingsPreferences> = settingsDataStore.settingsFlow
    val isOnboardingCompleted: Flow<Boolean?> = settingsDataStore.isOnboardingCompleted

    suspend fun setTempUnit(unit: TempUnit) {
        settingsDataStore.setTempUnit(unit)
    }

    suspend fun setWindUnit(unit: WindUnit) {
        settingsDataStore.setWindUnit(unit)
    }

    suspend fun setLanguage(language: Language) {
        settingsDataStore.setLanguage(language)
    }

    suspend fun setLocationMethod(method: LocationMethod) {
        settingsDataStore.setLocationMethod(method)
    }

    suspend fun setCustomLocation(lat: Double, lon: Double) {
        settingsDataStore.setCustomLocation(lat, lon)
    }
    
    suspend fun setOnboardingCompleted() {
        settingsDataStore.setOnboardingCompleted()
    }
}

package iti.yousef.skymood.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Extension property to create a single DataStore instance per Context */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "skymood_settings")

/**
 * Manages user settings and onboarding state via Jetpack DataStore.
 * Exposes reactive Flows so the UI recomposes when settings change.
 */
class SettingsDataStore(private val context: Context) {

    companion object {
        private val KEY_TEMP_UNIT = stringPreferencesKey("temp_unit")
        private val KEY_WIND_UNIT = stringPreferencesKey("wind_unit")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_LOCATION_METHOD = stringPreferencesKey("location_method")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    /**
     * Emits the current settings whenever any preference changes.
     * Provides default values matching SettingsPreferences defaults.
     */
    val settingsFlow: Flow<SettingsPreferences> = context.dataStore.data.map { prefs ->
        SettingsPreferences(
            temperatureUnit = prefs[KEY_TEMP_UNIT]?.let { TempUnit.valueOf(it) }
                ?: TempUnit.CELSIUS,
            windSpeedUnit = prefs[KEY_WIND_UNIT]?.let { WindUnit.valueOf(it) }
                ?: WindUnit.METER_SEC,
            language = prefs[KEY_LANGUAGE]?.let { Language.valueOf(it) }
                ?: Language.ENGLISH,
            locationMethod = prefs[KEY_LOCATION_METHOD]?.let { LocationMethod.valueOf(it) }
                ?: LocationMethod.GPS
        )
    }


    /** Emits whether the user has completed onboarding */
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }


    suspend fun setTempUnit(unit: TempUnit) {
        context.dataStore.edit { it[KEY_TEMP_UNIT] = unit.name }
    }

    suspend fun setWindUnit(unit: WindUnit) {
        context.dataStore.edit { it[KEY_WIND_UNIT] = unit.name }
    }

    suspend fun setLanguage(language: Language) {
        context.dataStore.edit { it[KEY_LANGUAGE] = language.name }
    }

    suspend fun setLocationMethod(method: LocationMethod) {
        context.dataStore.edit { it[KEY_LOCATION_METHOD] = method.name }
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = true }
    }
}

package iti.yousef.skymood.data.settings

/**
 * Data class holding all user-configurable settings.
 * These preferences are persisted via DataStore and
 * read throughout the app to customize the weather display.
 */
public data class SettingsPreferences(
    val temperatureUnit: TempUnit = TempUnit.CELSIUS,
    val windSpeedUnit: WindUnit = WindUnit.METER_SEC,
    val language: Language = Language.ENGLISH,
    val locationMethod: LocationMethod = LocationMethod.GPS
)

/**
 * Temperature unit options.
 * Maps to the OpenWeatherMap API "units" parameter:
 * - CELSIUS → "metric"
 * - FAHRENHEIT → "imperial"
 * - KELVIN → "standard"
 */
enum class TempUnit(val titleResId: Int, val apiValue: String) {
    CELSIUS(iti.yousef.skymood.R.string.temperature_unit_celsius, "metric"),
    FAHRENHEIT(iti.yousef.skymood.R.string.temperature_unit_fahrenheit, "imperial"),
    KELVIN(iti.yousef.skymood.R.string.temperature_unit_kelvin, "standard")
}

/**
 * Wind speed unit options with display labels.
 */
enum class WindUnit(val titleResId: Int) {
    METER_SEC(iti.yousef.skymood.R.string.wind_unit_ms),
    MILES_HOUR(iti.yousef.skymood.R.string.wind_unit_mph)
}


enum class Language(val titleResId: Int, val apiValue: String) {
    ENGLISH(iti.yousef.skymood.R.string.lang_english, "en"),
    ARABIC(iti.yousef.skymood.R.string.lang_arabic, "ar")
}

/**
 * How the app should determine the user's location.
 */
enum class LocationMethod(val titleResId: Int) {
    GPS(iti.yousef.skymood.R.string.loc_gps),
    MAP(iti.yousef.skymood.R.string.loc_map)
}

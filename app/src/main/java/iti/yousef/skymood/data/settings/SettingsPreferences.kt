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
enum class TempUnit(val displayName: String, val apiValue: String) {
    CELSIUS("°C", "metric"),
    FAHRENHEIT("°F", "imperial"),
    KELVIN("K", "standard")
}

/**
 * Wind speed unit options with display labels.
 */
enum class WindUnit(val displayName: String) {
    METER_SEC("m/s"),
    MILES_HOUR("mph")
}


enum class Language(val displayName: String, val apiValue: String) {
    ENGLISH("English", "en"),
    ARABIC("العربية", "ar")
}

/**
 * How the app should determine the user's location.
 */
enum class LocationMethod(val displayName: String) {
    GPS("GPS"),
    MAP("Choose from Map")
}

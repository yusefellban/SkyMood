package iti.yousef.skymood.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "forecast_cache")
data class ForecastEntity(
    @PrimaryKey
    val locationKey: String,
    val jsonData: String,
    val timestamp: Long
) {
    companion object {
        const val LAST_WEATHER_KEY = "last_weather"
    }
}

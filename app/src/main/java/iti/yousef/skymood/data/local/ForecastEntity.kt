package iti.yousef.skymood.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "forecast_cache")
data class ForecastEntity(
    @PrimaryKey
    val locationKey: String,
    val jsonData: String,
    val timestamp: Long
)

package iti.yousef.skymood.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a user's favorite location.
 * Stores the city name and coordinates for weather retrieval.
 */
@Entity(tableName = "favorite_locations")
data class FavoriteLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cityName: String,
    val latitude: Double,
    val longitude: Double
)
package iti.yousef.skymood.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AlertType { NOTIFICATION, ALARM }

/**
 * Room entity for a user-defined weather alert.
 * Defines the duration window and alert method.
 */
@Entity(tableName = "weather_alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** Label for this alert, e.g. "Morning Rain" */
    val label: String,
    val fromTime: Long,
    val toTime: Long,
    val alertType: AlertType,
    val isActive: Boolean = true
)

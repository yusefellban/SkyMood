package iti.yousef.skymood.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Delete
    suspend fun deleteAlert(alert: AlertEntity)

    @Query("SELECT * FROM weather_alerts ORDER BY fromTime ASC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM weather_alerts WHERE isActive = 1")
    suspend fun getActiveAlerts(): List<AlertEntity>

    @Query("UPDATE weather_alerts SET isActive = :active WHERE id = :alertId")
    suspend fun setAlertActive(alertId: Int, active: Boolean)

    @Query("DELETE FROM weather_alerts WHERE id = :alertId")
    suspend fun deleteById(alertId: Int)
}

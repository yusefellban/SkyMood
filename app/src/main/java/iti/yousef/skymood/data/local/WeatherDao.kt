package iti.yousef.skymood.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
public interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(entity: ForecastEntity)

    @Query("SELECT * FROM forecast_cache WHERE locationKey = :key LIMIT 1")
    suspend fun getForecast(key: String): ForecastEntity?

    @Query("DELETE FROM forecast_cache WHERE locationKey = :key")
    suspend fun deleteForecast(key: String)
    @Query("DELETE FROM forecast_cache")
    suspend fun clearAll()
}

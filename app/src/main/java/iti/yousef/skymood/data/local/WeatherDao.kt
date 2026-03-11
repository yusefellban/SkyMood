package iti.yousef.skymood.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


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

    // Favorites
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteLocationEntity)

    @Query("SELECT * FROM favorite_locations")
    fun getAllFavorites(): Flow<List<FavoriteLocationEntity>>

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteLocationEntity)
}

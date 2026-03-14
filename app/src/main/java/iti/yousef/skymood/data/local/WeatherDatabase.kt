package iti.yousef.skymood.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters


@TypeConverters(WeatherDatabase.Converters::class)
@Database(entities = [ForecastEntity::class, FavoriteLocationEntity::class, AlertEntity::class], version = 3, exportSchema = false)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao
    abstract fun alertDao(): AlertDao

    class Converters {
        @TypeConverter fun fromAlertType(value: AlertType): String = value.name
        @TypeConverter fun toAlertType(value: String): AlertType = AlertType.valueOf(value)
    }
    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getInstance(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "skymood_database"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}

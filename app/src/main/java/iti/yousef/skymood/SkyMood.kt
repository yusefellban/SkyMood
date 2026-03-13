package iti.yousef.skymood

import android.app.Application
import iti.yousef.skymood.data.local.WeatherDatabase
import iti.yousef.skymood.data.remote.RetrofitClient
import iti.yousef.skymood.data.repository.WeatherRepository
import iti.yousef.skymood.data.settings.SettingsDataStore
import iti.yousef.skymood.data.utils.AndroidNetworkHandler

/**
 * Application class that initializes app-wide singletons:
 * database, repository, and settings DataStore.
 * These are accessed from ViewModels via the application context.
 */
class SkyMood : Application() {

    lateinit var database: WeatherDatabase
        private set

    lateinit var repository: WeatherRepository
        private set

    lateinit var settingsDataStore: SettingsDataStore
        private set

    override fun onCreate() {
        super.onCreate()
        database = WeatherDatabase.getInstance(this)

        repository = WeatherRepository(
            apiService = RetrofitClient.apiService,
            weatherDao = database.weatherDao(),
            networkHandler = AndroidNetworkHandler(this)
        )
        settingsDataStore = SettingsDataStore(this)
    }
}

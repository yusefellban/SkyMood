package iti.yousef.skymood

import android.app.Application
import iti.yousef.skymood.data.local.WeatherDatabase
import iti.yousef.skymood.data.remote.RetrofitClient
import iti.yousef.skymood.data.remote.RetrofitWeatherRemoteDataSource
import iti.yousef.skymood.data.repository.AlertsRepository
import iti.yousef.skymood.data.repository.LocationRepository
import iti.yousef.skymood.data.repository.SettingsRepository
import iti.yousef.skymood.data.repository.WeatherRepository
import iti.yousef.skymood.data.local.settings.SettingsDataStore
import iti.yousef.skymood.data.utils.AndroidNetworkHandler

/**
 * Application class that initializes app-wide singletons.
 * Acts as a manual Service Locator for Repositories to enforce Strict MVVM.
 * Data sources (DAOs, DataStore) are kept private.
 */
class SkyMood : Application() {

    private lateinit var database: WeatherDatabase
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var alertDao: iti.yousef.skymood.data.local.AlertDao

    lateinit var weatherRepository: WeatherRepository
        private set

    lateinit var locationRepository: LocationRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var alertsRepository: AlertsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        
        // 1. Initialize hidden Data Sources
        database = WeatherDatabase.getInstance(this)
        settingsDataStore = SettingsDataStore(this)
        alertDao = database.alertDao()
        val remoteDataSource = RetrofitWeatherRemoteDataSource(RetrofitClient.apiService)

        // 2. Initialize and expose Repositories
        weatherRepository = WeatherRepository(
            remoteDataSource = remoteDataSource,
            weatherDao = database.weatherDao(),
            networkHandler = AndroidNetworkHandler(this)
        )
        
        locationRepository = LocationRepository(this)
        settingsRepository = SettingsRepository(settingsDataStore)
        alertsRepository = AlertsRepository(this, alertDao)
    }
}

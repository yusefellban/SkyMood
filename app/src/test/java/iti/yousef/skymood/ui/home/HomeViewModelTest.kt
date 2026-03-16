package iti.yousef.skymood.ui.home

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import android.util.Log
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.model.City
import iti.yousef.skymood.data.model.Coord
import iti.yousef.skymood.data.model.ForecastResponse
import iti.yousef.skymood.data.model.WeatherUiState
import iti.yousef.skymood.data.repository.LocationRepository
import iti.yousef.skymood.data.repository.SettingsRepository
import iti.yousef.skymood.data.repository.WeatherRepository
import iti.yousef.skymood.data.local.settings.LocationMethod
import iti.yousef.skymood.data.local.settings.SettingsPreferences
import iti.yousef.skymood.rules.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var app: SkyMood
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        app = mockk<SkyMood>()
        weatherRepository = mockk<WeatherRepository>(relaxed = true)
        settingsRepository = mockk<SettingsRepository>(relaxed = true)
        locationRepository = mockk<LocationRepository>(relaxed = true)

        every { app.weatherRepository } returns weatherRepository
        every { app.settingsRepository } returns settingsRepository
        every { app.locationRepository } returns locationRepository

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    @org.junit.After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `initial state is Loading and checks settings`() = runTest {
        // Arrange
        val prefs = SettingsPreferences()
        every { settingsRepository.settingsFlow } returns flowOf(prefs)
        every { weatherRepository.getAllFavorites() } returns flowOf(emptyList())

        // Act
        viewModel = HomeViewModel(app)

        // Assert
        viewModel.weatherState.test {
            val initialState = awaitItem()
            assertTrue(initialState is WeatherUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchWeather success translates to Success state`() = runTest {
        // Arrange
        val prefs = SettingsPreferences(locationMethod = LocationMethod.MAP, customLat = 30.0, customLon = 31.0)
        every { settingsRepository.settingsFlow } returns flowOf(prefs)
        every { weatherRepository.getAllFavorites() } returns flowOf(emptyList())

        val mockResponse = ForecastResponse(
            cod = "200", 
            message = 0, 
            count = 0, 
            list = emptyList(), 
            city = City(
                id = 1, 
                name = "Cairo", 
                coord = Coord(30.0, 31.0), 
                country = "EG", 
                population = 1, 
                timezone = 1, 
                sunrise = 1L, 
                sunset = 1L
            )
        )
        every { 
            weatherRepository.getForecast(
                30.0, 31.0, 
                prefs.temperatureUnit.apiValue, 
                prefs.language.apiValue
            ) 
        } returns flowOf(mockResponse)

        // Act
        viewModel = HomeViewModel(app)

        // Assert
        viewModel.weatherState.test {
            // With UnconfinedTestDispatcher, the fetch usually completes 
            // before we even start collecting. So we might see Success immediately.
            val result = awaitItem()
            if (result is WeatherUiState.Loading) {
                val nextResult = awaitItem()
                assertTrue("Expected Success state after Loading", nextResult is WeatherUiState.Success)
                assertEquals(mockResponse, (nextResult as WeatherUiState.Success).data)
            } else {
                assertTrue("Expected Success state", result is WeatherUiState.Success)
                assertEquals(mockResponse, (result as WeatherUiState.Success).data)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchWeather with GPS error emits Error state`() = runTest {
        // Arrange
        val prefs = SettingsPreferences(locationMethod = LocationMethod.GPS)
        every { settingsRepository.settingsFlow } returns flowOf(prefs)
        every { weatherRepository.getAllFavorites() } returns flowOf(emptyList())

        // Location returns null
        coEvery { locationRepository.getCurrentLocation() } returns null

        // Act
        viewModel = HomeViewModel(app)

        // Assert
        viewModel.weatherState.test {
            val result = awaitItem()
            if (result is WeatherUiState.Loading) {
                val errorState = awaitItem()
                assertTrue(errorState is WeatherUiState.Error)
                assertEquals("Unable to get your location. Please enable GPS and try again.", (errorState as WeatherUiState.Error).message)
            } else {
                assertTrue(result is WeatherUiState.Error)
                assertEquals("Unable to get your location. Please enable GPS and try again.", (result as WeatherUiState.Error).message)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavorite adds to favorites if currently not favorited`() = runTest {
        // Arrange - Map mode, simple response
        val prefs = SettingsPreferences(locationMethod = LocationMethod.MAP, customLat = 30.0, customLon = 31.0)
        every { settingsRepository.settingsFlow } returns flowOf(prefs)
        
        // No favorites initially
        every { weatherRepository.getAllFavorites() } returns flowOf(emptyList())

        val mockResponse = ForecastResponse(
            cod = "200", message = 0, count = 0, list = emptyList(), 
            city = City(1, "Cairo", Coord(30.0, 31.0), "EG", 1, 1, 1, 1)
        )
        every { weatherRepository.getForecast(any(), any(), any(), any()) } returns flowOf(mockResponse)

        viewModel = HomeViewModel(app)

        // Wait for state to settle to Success
        viewModel.weatherState.test {
            val firstItem = awaitItem()
            if (firstItem is WeatherUiState.Loading) {
                assertTrue(awaitItem() is WeatherUiState.Success)
            } else {
                assertTrue(firstItem is WeatherUiState.Success)
            }
            cancelAndIgnoreRemainingEvents()
        }

        // Act
        viewModel.toggleFavorite()

        // Assert
        coVerify(exactly = 1) { 
            weatherRepository.insertFavorite(
                match { it.cityName == "Cairo" && it.latitude == 30.0 && it.longitude == 31.0 }
            ) 
        }
    }
}

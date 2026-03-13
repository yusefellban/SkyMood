package iti.yousef.skymood.data.repository

import io.mockk.*
import iti.yousef.skymood.data.local.ForecastEntity
import iti.yousef.skymood.data.local.WeatherDao
import iti.yousef.skymood.data.model.*
import iti.yousef.skymood.data.remote.WeatherApiService
import iti.yousef.skymood.data.utils.NetworkHandler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import android.util.Log
import app.cash.turbine.test
import iti.yousef.skymood.data.local.FavoriteLocationEntity
import kotlinx.coroutines.flow.flowOf

class WeatherRepositoryTest {

    private lateinit var apiService: WeatherApiService
    private lateinit var weatherDao: WeatherDao
    private lateinit var networkHandler: NetworkHandler
    private lateinit var repository: WeatherRepository

    private val lat = 30.0
    private val lon = 31.0
    private val locationKey = "30.0_31.0"
    
    private val mockForecast = ForecastResponse(
        cod = "200",
        message = 0,
        count = 1,
        list = listOf(
            ForecastItem(
                dt = 123456789L,
                main = MainData(25.0, 26.0, 24.0, 27.0, 1013, 1013, 1013, 50),
                weather = listOf(Weather(800, "Clear", "clear sky", "01d")),
                clouds = Clouds(0),
                wind = Wind(5.0, 180),
                visibility = 10000,
                pop = 0.0,
                dtTxt = "2026-03-13 12:00:00"
            )
        ),
        city = City(
            id = 1, name = "Cairo", country = "EG", population = 1000000,
            timezone = 7200, sunrise = 12345L, sunset = 67890L,
            coord = Coord(30.0, 31.0)
        )
    )

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        
        apiService = mockk()
        weatherDao = mockk()
        networkHandler = mockk()
        repository = WeatherRepository(apiService, weatherDao, networkHandler)
    }

    @After
    fun teardown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `getForecast when online should fetch from API and cache data`() = runTest {
        // Given
        every { networkHandler.isNetworkAvailable() } returns true
        coEvery { apiService.getForecast(any(), any(), any(), any(), any()) } returns mockForecast
        coEvery { weatherDao.insertForecast(any()) } just Runs

        // When
        val result = repository.getForecast(lat, lon)

        // Then
        if (mockForecast != result) {
            println("MISMATCH: Expected $mockForecast\nActual $result")
        }
        assertEquals(mockForecast, result)
        coVerify { apiService.getForecast(lat, lon, any(), any(), any()) }
        coVerify { weatherDao.insertForecast(any()) }
    }

    @Test
    fun `getForecast when offline should return cached data`() = runTest {
        // Given
        val cachedJson = "mock_json"
        val cachedEntity = ForecastEntity(locationKey, cachedJson, System.currentTimeMillis())
        
        every { networkHandler.isNetworkAvailable() } returns false
        coEvery { weatherDao.getForecast(locationKey) } returns cachedEntity
        
        // Mock GSON behavior isn't needed if we mock the repo internal helper or just let it run
        // But since we are testing the repo, it will try to deserialize the "mock_json" which will fail
        // Let's use a real JSON for valid testing
        val gson = com.google.gson.Gson()
        val realJson = gson.toJson(mockForecast)
        val realEntity = ForecastEntity(locationKey, realJson, System.currentTimeMillis())
        coEvery { weatherDao.getForecast(locationKey) } returns realEntity

        // When
        val result = repository.getForecast(lat, lon)

        // Then
        assertEquals(mockForecast, result)
        coVerify(exactly = 0) { apiService.getForecast(any(), any(), any(), any(), any()) }
        coVerify { weatherDao.getForecast(locationKey) }
    }

    @Test(expected = Exception::class)
    fun `getForecast when offline and no cache should throw exception`() = runTest {
        // Given
        every { networkHandler.isNetworkAvailable() } returns false
        coEvery { weatherDao.getForecast(locationKey) } returns null

        // When
        repository.getForecast(lat, lon)
    }

    @Test
    fun `getForecast when online but API fails should return cached data`() = runTest {
        // Given
        val gson = com.google.gson.Gson()
        val realJson = gson.toJson(mockForecast)
        val realEntity = ForecastEntity(locationKey, realJson, System.currentTimeMillis())
        
        every { networkHandler.isNetworkAvailable() } returns true
        coEvery { apiService.getForecast(any(), any(), any(), any(), any()) } throws Exception("API Error")
        coEvery { weatherDao.getForecast(locationKey) } returns realEntity

        // When
        val result = repository.getForecast(lat, lon)

        // Then
        assertEquals(mockForecast, result)
        coVerify { apiService.getForecast(any(), any(), any(), any(), any()) }
        coVerify { weatherDao.getForecast(locationKey) }
    }

    @Test
    fun `insertFavorite should call DAO`() = runTest {
        // Given
        val fav = FavoriteLocationEntity(cityName = "Cairo", latitude = 30.0, longitude = 31.0)
        coEvery { weatherDao.insertFavorite(fav) } just Runs

        // When
        repository.insertFavorite(fav)

        // Then
        coVerify { weatherDao.insertFavorite(fav) }
    }

    @Test
    fun `deleteFavorite should call DAO`() = runTest {
        // Given
        val fav = FavoriteLocationEntity(cityName = "Cairo", latitude = 30.0, longitude = 31.0)
        coEvery { weatherDao.deleteFavorite(fav) } just Runs

        // When
        repository.deleteFavorite(fav)

        // Then
        coVerify { weatherDao.deleteFavorite(fav) }
    }

    @Test
    fun `getAllFavorites should return flow from DAO`() = runTest {
        // Given
        val favoritesList = listOf(
            FavoriteLocationEntity(id = 1, cityName = "Cairo", latitude = 30.0, longitude = 31.0)
        )
        every { weatherDao.getAllFavorites() } returns flowOf(favoritesList)

        // When & Then
        repository.getAllFavorites().test {
            assertEquals(favoritesList, awaitItem())
            awaitComplete()
        }
    }
}

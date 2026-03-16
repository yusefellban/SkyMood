package iti.yousef.skymood.ui.favorites

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.coVerify
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.model.FavoriteLocationEntity
import iti.yousef.skymood.data.repository.WeatherRepository
import iti.yousef.skymood.rules.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var app: SkyMood
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var viewModel: FavoritesViewModel

    @Before
    fun setup() {
        app = mockk<SkyMood>()
        weatherRepository = mockk<WeatherRepository>(relaxed = true)
        
        // Mock application returning repository
        every { app.weatherRepository } returns weatherRepository
    }

    @Test
    fun `favorites state flow emits data from repository`() = runTest {
        // Arrange
        val mockFavorites = listOf(
            FavoriteLocationEntity(cityName = "Cairo", latitude = 30.0444, longitude = 31.2357),
            FavoriteLocationEntity(cityName = "Alexandria", latitude = 31.2001, longitude = 29.9187)
        )
        every { weatherRepository.getAllFavorites() } returns flowOf(mockFavorites)

        // Act
        viewModel = FavoritesViewModel(app)

        // Assert
        viewModel.favorites.test {
            assertEquals(mockFavorites, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteFavorite calls repository delete`() = runTest {
        // Arrange
        every { weatherRepository.getAllFavorites() } returns flowOf(emptyList())
        viewModel = FavoritesViewModel(app)
        val entity = FavoriteLocationEntity(cityName = "Cairo", latitude = 30.0, longitude = 31.0)

        // Act
        viewModel.deleteFavorite(entity)

        // Assert
        coVerify(exactly = 1) { weatherRepository.deleteFavorite(entity) }
    }
}

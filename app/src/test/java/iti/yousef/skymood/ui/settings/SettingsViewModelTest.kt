package iti.yousef.skymood.ui.settings

import app.cash.turbine.test
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.repository.SettingsRepository
import iti.yousef.skymood.data.settings.Language
import iti.yousef.skymood.data.settings.LocationMethod
import iti.yousef.skymood.data.settings.SettingsPreferences
import iti.yousef.skymood.data.settings.TempUnit
import iti.yousef.skymood.data.settings.WindUnit
import iti.yousef.skymood.rules.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var app: SkyMood
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        app = mockk<SkyMood>()
        settingsRepository = mockk<SettingsRepository>(relaxed = true)
        every { app.settingsRepository } returns settingsRepository
    }

    @Test
    fun `settings state flow emits data from repository`() = runTest {
        // Arrange
        val expectedSettings = SettingsPreferences(
            temperatureUnit = TempUnit.FAHRENHEIT,
            windSpeedUnit = WindUnit.MILES_HOUR,
            language = Language.ARABIC,
            locationMethod = LocationMethod.MAP,
            customLat = 30.0,
            customLon = 31.0
        )
        every { settingsRepository.settingsFlow } returns flowOf(expectedSettings)

        // Act
        viewModel = SettingsViewModel(app)

        // Assert
        viewModel.settings.test {
            assertEquals(expectedSettings, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateTemperatureUnit calls repository`() = runTest {
        every { settingsRepository.settingsFlow } returns flowOf(SettingsPreferences())
        viewModel = SettingsViewModel(app)
        
        viewModel.updateTemperatureUnit(TempUnit.FAHRENHEIT)
        coVerify(exactly = 1) { settingsRepository.setTempUnit(TempUnit.FAHRENHEIT) }
    }

    @Test
    fun `updateWindSpeedUnit calls repository`() = runTest {
        every { settingsRepository.settingsFlow } returns flowOf(SettingsPreferences())
        viewModel = SettingsViewModel(app)
        
        viewModel.updateWindSpeedUnit(WindUnit.MILES_HOUR)
        coVerify(exactly = 1) { settingsRepository.setWindUnit(WindUnit.MILES_HOUR) }
    }

    @Test
    fun `updateLanguage calls repository`() = runTest {
        every { settingsRepository.settingsFlow } returns flowOf(SettingsPreferences())
        viewModel = SettingsViewModel(app)
        
        viewModel.updateLanguage(Language.ARABIC)
        coVerify(exactly = 1) { settingsRepository.setLanguage(Language.ARABIC) }
    }

    @Test
    fun `updateLocationMethod calls repository`() = runTest {
        every { settingsRepository.settingsFlow } returns flowOf(SettingsPreferences())
        viewModel = SettingsViewModel(app)
        
        viewModel.updateLocationMethod(LocationMethod.MAP)
        coVerify(exactly = 1) { settingsRepository.setLocationMethod(LocationMethod.MAP) }
    }

    @Test
    fun `updateCustomLocation calls repository`() = runTest {
        every { settingsRepository.settingsFlow } returns flowOf(SettingsPreferences())
        viewModel = SettingsViewModel(app)
        
        viewModel.updateCustomLocation(30.0, 31.0)
        coVerify(exactly = 1) { settingsRepository.setCustomLocation(30.0, 31.0) }
    }
}

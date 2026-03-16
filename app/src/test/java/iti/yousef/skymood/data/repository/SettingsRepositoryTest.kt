package iti.yousef.skymood.data.repository

import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import app.cash.turbine.test
import iti.yousef.skymood.data.local.settings.Language
import iti.yousef.skymood.data.local.settings.LocationMethod
import iti.yousef.skymood.data.local.settings.SettingsDataStore
import iti.yousef.skymood.data.local.settings.SettingsPreferences
import iti.yousef.skymood.data.local.settings.TempUnit
import iti.yousef.skymood.data.local.settings.WindUnit

class SettingsRepositoryTest {

    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var repository: SettingsRepository

    @Before
    fun setup() {
        settingsDataStore = mockk()
        repository = SettingsRepository(settingsDataStore)
    }

    @Test
    fun `settingsFlow should emit data from DataStore`() = runTest {
        val prefs = SettingsPreferences(temperatureUnit = TempUnit.FAHRENHEIT)
        every { settingsDataStore.settingsFlow } returns flowOf(prefs)

        repository.settingsFlow.test {
            assertEquals(prefs, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `isOnboardingCompleted should emit data from DataStore`() = runTest {
        every { settingsDataStore.isOnboardingCompleted } returns flowOf(true)

        repository.isOnboardingCompleted.test {
            assertEquals(true, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `setTempUnit should call DataStore`() = runTest {
        coEvery { settingsDataStore.setTempUnit(TempUnit.FAHRENHEIT) } just Runs
        repository.setTempUnit(TempUnit.FAHRENHEIT)
        coVerify { settingsDataStore.setTempUnit(TempUnit.FAHRENHEIT) }
    }

    @Test
    fun `setWindUnit should call DataStore`() = runTest {
        coEvery { settingsDataStore.setWindUnit(WindUnit.MILES_HOUR) } just Runs
        repository.setWindUnit(WindUnit.MILES_HOUR)
        coVerify { settingsDataStore.setWindUnit(WindUnit.MILES_HOUR) }
    }

    @Test
    fun `setLanguage should call DataStore`() = runTest {
        coEvery { settingsDataStore.setLanguage(Language.ARABIC) } just Runs
        repository.setLanguage(Language.ARABIC)
        coVerify { settingsDataStore.setLanguage(Language.ARABIC) }
    }

    @Test
    fun `setLocationMethod should call DataStore`() = runTest {
        coEvery { settingsDataStore.setLocationMethod(LocationMethod.MAP) } just Runs
        repository.setLocationMethod(LocationMethod.MAP)
        coVerify { settingsDataStore.setLocationMethod(LocationMethod.MAP) }
    }

    @Test
    fun `setCustomLocation should call DataStore`() = runTest {
        coEvery { settingsDataStore.setCustomLocation(30.0, 31.0) } just Runs
        repository.setCustomLocation(30.0, 31.0)
        coVerify { settingsDataStore.setCustomLocation(30.0, 31.0) }
    }

    @Test
    fun `setOnboardingCompleted should call DataStore`() = runTest {
        coEvery { settingsDataStore.setOnboardingCompleted() } just Runs
        repository.setOnboardingCompleted()
        coVerify { settingsDataStore.setOnboardingCompleted() }
    }
}

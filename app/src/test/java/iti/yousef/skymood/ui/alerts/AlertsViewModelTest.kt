package iti.yousef.skymood.ui.alerts

import app.cash.turbine.test
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.model.Entity.AlertEntity
import iti.yousef.skymood.data.model.Entity.AlertType
import iti.yousef.skymood.data.repository.AlertsRepository
import iti.yousef.skymood.rules.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

class AlertsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var app: SkyMood
    private lateinit var alertsRepository: AlertsRepository
    private lateinit var viewModel: AlertsViewModel

    @Before
    fun setup() {
        app = mockk<SkyMood>()
        alertsRepository = mockk<AlertsRepository>(relaxed = true)
        every { app.alertsRepository } returns alertsRepository
    }

    @Test
    fun `alerts state flow emits data from repository`() = runTest {
        // Arrange
        val calendar = Calendar.getInstance()
        val mockAlert = AlertEntity(
            id = 1,
            label = "Test",
            fromTime = calendar.timeInMillis,
            toTime = calendar.timeInMillis + 3600000,
            alertType = AlertType.ALARM,
            isActive = true
        )
        every { alertsRepository.getAllAlerts() } returns flowOf(listOf(mockAlert))

        // Act
        viewModel = AlertsViewModel(app)

        // Assert
        viewModel.alerts.test {
            assertEquals(listOf(mockAlert), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addAlert calls repository addAlert`() = runTest {
        // Arrange
        every { alertsRepository.getAllAlerts() } returns flowOf(emptyList())
        viewModel = AlertsViewModel(app)
        val label = "Cairo"
        val fromTime = 1000L
        val toTime = 2000L
        val type = AlertType.NOTIFICATION

        // Act
        viewModel.addAlert(label, fromTime, toTime, type)

        // Assert
        coVerify(exactly = 1) { 
            alertsRepository.addAlert(
                match { it.label == label && it.fromTime == fromTime && it.toTime == toTime && it.alertType == type }
            )
        }
    }

    @Test
    fun `toggleAlert calls repository toggleAlert with inverted active state`() = runTest {
        // Arrange
        every { alertsRepository.getAllAlerts() } returns flowOf(emptyList())
        viewModel = AlertsViewModel(app)
        val mockAlert = AlertEntity(
            id = 1,
            label = "Test",
            fromTime = 1000L,
            toTime = 2000L,
            alertType = AlertType.ALARM,
            isActive = true
        )

        // Act
        viewModel.toggleAlert(mockAlert)

        // Assert
        coVerify(exactly = 1) { alertsRepository.toggleAlert(mockAlert, false) }
    }

    @Test
    fun `deleteAlert calls repository deleteAlert`() = runTest {
        // Arrange
        every { alertsRepository.getAllAlerts() } returns flowOf(emptyList())
        viewModel = AlertsViewModel(app)
        val mockAlert = AlertEntity(id = 1, label = "Test", fromTime = 1000L, toTime = 2000L, alertType = AlertType.NOTIFICATION, isActive = true)

        // Act
        viewModel.deleteAlert(mockAlert)

        // Assert
        coVerify(exactly = 1) { alertsRepository.deleteAlert(mockAlert) }
    }
}

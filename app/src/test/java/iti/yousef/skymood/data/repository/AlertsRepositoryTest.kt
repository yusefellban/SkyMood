package iti.yousef.skymood.data.repository

import android.app.Application
import io.mockk.*
import iti.yousef.skymood.data.local.AlertDao
import iti.yousef.skymood.data.local.AlertEntity
import iti.yousef.skymood.data.local.AlertType
import iti.yousef.skymood.data.work.AlarmScheduler
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import app.cash.turbine.test

class AlertsRepositoryTest {

    private lateinit var application: Application
    private lateinit var alertDao: AlertDao
    private lateinit var repository: AlertsRepository

    @Before
    fun setup() {
        application = mockk()
        alertDao = mockk()
        repository = AlertsRepository(application, alertDao)
        mockkObject(AlarmScheduler)
    }

    @After
    fun teardown() {
        unmockkObject(AlarmScheduler)
    }

    @Test
    fun `getAllAlerts should return flow from DAO`() = runTest {
        val alerts = listOf(
            AlertEntity(id = 1, label = "Test", fromTime = 0, toTime = 1, alertType = AlertType.NOTIFICATION)
        )
        every { alertDao.getAllAlerts() } returns flowOf(alerts)

        repository.getAllAlerts().test {
            assertEquals(alerts, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `addAlert should insert into DAO and schedule alarm if active`() = runTest {
        val alert = AlertEntity(label = "Test", fromTime = 0, toTime = 1, alertType = AlertType.NOTIFICATION, isActive = true)
        coEvery { alertDao.insertAlert(alert) } returns 1L
        every { AlarmScheduler.scheduleAlarm(application, any()) } just Runs

        repository.addAlert(alert)

        coVerify { alertDao.insertAlert(alert) }
        verify { AlarmScheduler.scheduleAlarm(application, alert.copy(id = 1)) }
    }

    @Test
    fun `addAlert should insert into DAO but NOT schedule alarm if inactive`() = runTest {
        val alert = AlertEntity(label = "Test", fromTime = 0, toTime = 1, alertType = AlertType.NOTIFICATION, isActive = false)
        coEvery { alertDao.insertAlert(alert) } returns 1L

        repository.addAlert(alert)

        coVerify { alertDao.insertAlert(alert) }
        verify(exactly = 0) { AlarmScheduler.scheduleAlarm(any(), any()) }
    }

    @Test
    fun `toggleAlert to active should update DAO and schedule alarm`() = runTest {
        val alert = AlertEntity(id = 1, label = "Test", fromTime = 0, toTime = 1, alertType = AlertType.NOTIFICATION, isActive = false)
        coEvery { alertDao.setAlertActive(1, true) } just Runs
        every { AlarmScheduler.scheduleAlarm(application, any()) } just Runs

        repository.toggleAlert(alert, true)

        coVerify { alertDao.setAlertActive(1, true) }
        verify { AlarmScheduler.scheduleAlarm(application, alert.copy(isActive = true)) }
    }

    @Test
    fun `toggleAlert to inactive should update DAO and cancel alarm`() = runTest {
        val alert = AlertEntity(id = 1, label = "Test", fromTime = 0, toTime = 1, alertType = AlertType.NOTIFICATION, isActive = true)
        coEvery { alertDao.setAlertActive(1, false) } just Runs
        every { AlarmScheduler.cancelAlarm(application, 1) } just Runs

        repository.toggleAlert(alert, false)

        coVerify { alertDao.setAlertActive(1, false) }
        verify { AlarmScheduler.cancelAlarm(application, 1) }
    }

    @Test
    fun `deleteAlert should delete from DAO and cancel alarm`() = runTest {
        val alert = AlertEntity(id = 1, label = "Test", fromTime = 0, toTime = 1, alertType = AlertType.NOTIFICATION)
        coEvery { alertDao.deleteAlert(alert) } just Runs
        every { AlarmScheduler.cancelAlarm(application, 1) } just Runs

        repository.deleteAlert(alert)

        coVerify { alertDao.deleteAlert(alert) }
        verify { AlarmScheduler.cancelAlarm(application, 1) }
    }
}

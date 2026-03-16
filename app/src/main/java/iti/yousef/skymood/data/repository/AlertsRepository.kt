package iti.yousef.skymood.data.repository

import android.app.Application
import iti.yousef.skymood.data.local.AlertDao
import iti.yousef.skymood.data.model.Entity.AlertEntity
import iti.yousef.skymood.data.work.AlarmScheduler
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstracting AlertDao database access and AlarmScheduler Android logic.
 * Ensures the AlertsViewModel remains generic and testable.
 */
class AlertsRepository(
    private val application: Application,
    private val alertDao: AlertDao
) {
    fun getAllAlerts(): Flow<List<AlertEntity>> = alertDao.getAllAlerts()

    suspend fun addAlert(alert: AlertEntity) {
        val id = alertDao.insertAlert(alert)
        val finalEntity = alert.copy(id = id.toInt())
        if (finalEntity.isActive) {
            AlarmScheduler.scheduleAlarm(application, finalEntity)
        }
    }

    suspend fun toggleAlert(alert: AlertEntity, isActive: Boolean) {
        val updated = alert.copy(isActive = isActive)
        alertDao.setAlertActive(alert.id, isActive)
        if (isActive) {
            AlarmScheduler.scheduleAlarm(application, updated)
        } else {
            AlarmScheduler.cancelAlarm(application, updated.id)
        }
    }

    suspend fun deleteAlert(alert: AlertEntity) {
        alertDao.deleteAlert(alert)
        AlarmScheduler.cancelAlarm(application, alert.id)
    }
}

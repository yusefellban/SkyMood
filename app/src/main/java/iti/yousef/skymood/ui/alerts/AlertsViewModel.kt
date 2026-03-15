package iti.yousef.skymood.ui.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.local.AlertEntity
import iti.yousef.skymood.data.local.AlertType
import iti.yousef.skymood.data.work.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SkyMood
    private val alertDao = app.alertDao

    /** Live list of all saved alerts */
    val alerts: StateFlow<List<AlertEntity>> = alertDao.getAllAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Adds a new alert and schedules it via AlarmManager.
     * The worker fires at [fromTime] and runs until [toTime].
     */
    fun addAlert(
        label: String,
        fromTime: Long,
        toTime: Long,
        alertType: AlertType
    ) {
        viewModelScope.launch {
            val entity = AlertEntity(
                label = label,
                fromTime = fromTime,
                toTime = toTime,
                alertType = alertType,
                isActive = true
            )
            // Room returns the auto-generated id — use it for AlarmManager tagging
            val generatedId = alertDao.insertAlert(entity)
            val finalEntity = entity.copy(id = generatedId.toInt())
            AlarmScheduler.scheduleAlarm(app, finalEntity)
        }
    }

    fun toggleAlert(alert: AlertEntity) {
        viewModelScope.launch {
            val newActive = !alert.isActive
            alertDao.setAlertActive(alert.id, newActive)
            if (!newActive) {
                AlarmScheduler.cancelAlarm(app, alert.id)
            } else {
                AlarmScheduler.scheduleAlarm(app, alert.copy(isActive = true))
            }
        }
    }

    fun deleteAlert(alert: AlertEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(app, alert.id)
            alertDao.deleteAlert(alert)
        }
    }
}

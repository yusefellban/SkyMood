package iti.yousef.skymood.ui.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.local.AlertEntity
import iti.yousef.skymood.data.local.AlertType
import iti.yousef.skymood.data.work.WeatherAlertWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AlertsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SkyMood
    private val alertDao = app.alertDao
    private val workManager = WorkManager.getInstance(application)

    /** Live list of all saved alerts */
    val alerts: StateFlow<List<AlertEntity>> = alertDao.getAllAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Adds a new alert and schedules it via WorkManager.
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
            alertDao.insertAlert(entity)
            scheduleWork(entity)
        }
    }

    private fun scheduleWork(alert: AlertEntity) {
        val now = System.currentTimeMillis()
        val delay = if (alert.fromTime > now) alert.fromTime - now else 0L
        val duration = alert.toTime - alert.fromTime

        val data = workDataOf(
            WeatherAlertWorker.ALERT_ID_KEY to alert.id,
            WeatherAlertWorker.ALERT_LABEL_KEY to alert.label,
            WeatherAlertWorker.ALERT_TYPE_KEY to alert.alertType.name
        )

        // Fire once after the delay (at fromTime)
        val request = OneTimeWorkRequestBuilder<WeatherAlertWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("alert_${alert.id}")
            .build()

        workManager.enqueue(request)
    }

    fun toggleAlert(alert: AlertEntity) {
        viewModelScope.launch {
            val newActive = !alert.isActive
            alertDao.setAlertActive(alert.id, newActive)
            if (!newActive) {
                workManager.cancelAllWorkByTag("alert_${alert.id}")
            } else {
                scheduleWork(alert.copy(isActive = true))
            }
        }
    }

    fun deleteAlert(alert: AlertEntity) {
        viewModelScope.launch {
            workManager.cancelAllWorkByTag("alert_${alert.id}")
            alertDao.deleteAlert(alert)
        }
    }
}

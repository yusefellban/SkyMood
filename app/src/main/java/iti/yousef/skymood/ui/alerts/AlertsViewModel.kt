package iti.yousef.skymood.ui.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.model.Entity.AlertEntity
import iti.yousef.skymood.data.model.Entity.AlertType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SkyMood
    private val alertsRepository = app.alertsRepository

    /** Live list of all saved alerts */
    val alerts: StateFlow<List<AlertEntity>> = alertsRepository.getAllAlerts()
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
            // Pass to repository to handle insertion and scheduling
            alertsRepository.addAlert(entity)
        }
    }

    fun toggleAlert(alert: AlertEntity) {
        viewModelScope.launch {
            alertsRepository.toggleAlert(alert, !alert.isActive)
        }
    }

    fun deleteAlert(alert: AlertEntity) {
        viewModelScope.launch {
            alertsRepository.deleteAlert(alert)
        }
    }
}

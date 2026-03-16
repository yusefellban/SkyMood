package iti.yousef.skymood.ui.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.model.Entity.AlertEntity
import iti.yousef.skymood.data.model.Entity.AlertType
import iti.yousef.skymood.data.model.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SkyMood
    private val alertsRepository = app.alertsRepository

    /** Live list of all saved alerts */
    val alerts: StateFlow<List<AlertEntity>> = alertsRepository.getAllAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    /** One-time UI events like snackbars or navigation */
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

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
            _events.emit(UiEvent.ShowSnackbar("Alert '${label}' added successfully"))
        }
    }

    fun toggleAlert(alert: AlertEntity) {
        viewModelScope.launch {
            alertsRepository.toggleAlert(alert, !alert.isActive)
            val status = if (!alert.isActive) "activated" else "deactivated"
            _events.emit(UiEvent.ShowSnackbar("Alert '${alert.label}' $status"))
        }
    }

    fun deleteAlert(alert: AlertEntity) {
        viewModelScope.launch {
            alertsRepository.deleteAlert(alert)
            _events.emit(UiEvent.ShowSnackbar("Alert '${alert.label}' deleted"))
        }
    }
}

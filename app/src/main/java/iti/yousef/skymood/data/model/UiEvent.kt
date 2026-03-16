package iti.yousef.skymood.data.model

/**
 * Represents one-time UI events that should be handled by the UI.
 */
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
}

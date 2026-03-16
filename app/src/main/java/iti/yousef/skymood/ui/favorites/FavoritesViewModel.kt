package iti.yousef.skymood.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.model.FavoriteLocationEntity
import iti.yousef.skymood.data.model.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Favorites screen.
 * Observes the favorites list from the database.
 */
class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SkyMood
    private val weatherRepository = app.weatherRepository

    /** StateFlow emitting the current list of favorites */
    val favorites: StateFlow<List<FavoriteLocationEntity>> = weatherRepository.getAllFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    /** One-time UI events like snackbars or navigation */
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    fun deleteFavorite(favorite: FavoriteLocationEntity) {
        viewModelScope.launch {
            weatherRepository.deleteFavorite(favorite)
            _events.emit(UiEvent.ShowSnackbar("${favorite.cityName} removed from favorites"))
        }
    }
}

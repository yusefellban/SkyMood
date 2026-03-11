package iti.yousef.skymood.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.local.FavoriteLocationEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Favorites screen.
 * Observes the favorites list from the database.
 */
class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SkyMood
    private val repository = app.repository

    /** StateFlow emitting the current list of favorites */
    val favorites: StateFlow<List<FavoriteLocationEntity>> = repository.getAllFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteFavorite(favorite: FavoriteLocationEntity) {
        viewModelScope.launch {
            repository.deleteFavorite(favorite)
        }
    }
}

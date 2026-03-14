package iti.yousef.skymood.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import iti.yousef.skymood.data.settings.LocationMethod
import iti.yousef.skymood.data.settings.SettingsDataStore
import iti.yousef.skymood.ui.alerts.AlertsScreen
import iti.yousef.skymood.ui.favorites.FavoritesScreen
import iti.yousef.skymood.ui.home.HomeScreen
import iti.yousef.skymood.ui.onboarding.OnboardingScreen
import iti.yousef.skymood.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

/**
 * Root navigation composable for the app.
 * Determines the start destination based on whether onboarding has been completed.
 * Uses type-safe route-based navigation with smooth transition animations.
 *
 * @param settingsDataStore Used to check and update onboarding completion state
 */
@Composable
public fun AppNavigation(settingsDataStore: SettingsDataStore) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Observe onboarding completion to set the correct start destination
    val isOnboardingDone by settingsDataStore.isOnboardingCompleted.collectAsState(initial = null)

    // Wait until DataStore emits the initial value before rendering
    if (isOnboardingDone == null) return

    val startDestination: Any = if (isOnboardingDone == true) HomeRoute else OnboardingRoute

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(400)) + slideInHorizontally(tween(400)) { it / 4 } },
        exitTransition = { fadeOut(tween(300)) },
        popEnterTransition = { fadeIn(tween(400)) + slideInHorizontally(tween(400)) { -it / 4 } },
        popExitTransition = { fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it / 4 } }
    ) {
        // Onboarding flow
        composable<OnboardingRoute> {
            OnboardingScreen(
                onFinished = {
                    scope.launch {
                        settingsDataStore.setOnboardingCompleted()
                    }
                    navController.navigate(HomeRoute) {
                        popUpTo<OnboardingRoute> { inclusive = true }
                    }
                }
            )
        }

        // Home screen with weather data
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(SettingsRoute) },
                onNavigateToFavorites = { navController.navigate(FavoritesRoute) },
                onNavigateToAlerts = { navController.navigate(AlertsRoute) }
            )
        }

        // Placeholder destinations for future screens
        composable<FavoritesRoute> {
            FavoritesScreen(
                onNavigateBack = { navController.navigateUp() },
                onFavoriteClick = { favorite ->
                    scope.launch {
                        settingsDataStore.setCustomLocation(favorite.latitude, favorite.longitude)
                        settingsDataStore.setLocationMethod(LocationMethod.MAP)
                        navController.navigate(HomeRoute) {
                            popUpTo<HomeRoute> { inclusive = true }
                        }
                    }
                }
            )
        }
        composable<AlertsRoute> {
            AlertsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
        composable<SettingsRoute> {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToMap = { navController.navigate(SettingsMapRoute) }
            )
        }
        composable<SettingsMapRoute> {
            val parentEntry = androidx.compose.runtime.remember(it) { navController.getBackStackEntry(SettingsRoute) }
            val viewModel: iti.yousef.skymood.ui.settings.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(parentEntry)
            iti.yousef.skymood.ui.settings.SettingsMapScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}

package iti.yousef.skymood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import iti.yousef.skymood.navigation.AppNavigation
import iti.yousef.skymood.ui.theme.SkyMoodTheme

/**
 * Single-activity host for the entire SkyMood application.
 * Uses edge-to-edge display for immersive weather backgrounds.
 * All screens are Compose destinations managed by AppNavigation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as SkyMood

        setContent {
            SkyMoodTheme {
                AppNavigation(settingsDataStore = app.settingsDataStore)
            }
        }
    }
}
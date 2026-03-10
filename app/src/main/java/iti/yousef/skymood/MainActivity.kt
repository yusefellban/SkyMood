package iti.yousef.skymood

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import iti.yousef.skymood.navigation.AppNavigation
import iti.yousef.skymood.ui.theme.SkyMoodTheme
import java.util.Locale

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
            val settings by app.settingsDataStore.settingsFlow.collectAsState(initial = null)
            val languageCode = settings?.language?.apiValue ?: "en"

            val context = LocalContext.current
            val locale = Locale(languageCode)
            
            val configuration = remember(languageCode) {
                Configuration(context.resources.configuration).apply {
                    setLocale(locale)
                }
            }

            val localizedContext = remember(languageCode) {
                object : android.content.ContextWrapper(context) {
                    val configContext = context.createConfigurationContext(configuration)
                    override fun getResources(): android.content.res.Resources {
                        return configContext.resources
                    }
                }
            }

            val layoutDirection = if (languageCode == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides configuration,
                LocalLayoutDirection provides layoutDirection
            ) {
                SkyMoodTheme {
                    AppNavigation(settingsDataStore = app.settingsDataStore)
                }
            }
        }
    }
}
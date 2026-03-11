package iti.yousef.skymood.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import iti.yousef.skymood.data.settings.Language
import iti.yousef.skymood.data.settings.LocationMethod
import iti.yousef.skymood.data.settings.TempUnit
import iti.yousef.skymood.data.settings.WindUnit

/**
 * Screen allowing users to configure application preferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Location Settings
            SettingsSection(
                title = androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.location), 
                icon = Icons.Default.LocationOn
            ) {
                val optionsStr = LocationMethod.entries.map { androidx.compose.ui.res.stringResource(it.titleResId) }
                val selectedStr = androidx.compose.ui.res.stringResource(settings.locationMethod.titleResId)
                SettingsOptionGroup(
                    options = optionsStr,
                    selectedOption = selectedStr,
                    onOptionSelected = { displayStr ->
                        val method = LocationMethod.entries.find { optionsStr[LocationMethod.entries.indexOf(it)] == displayStr }
                        method?.let { 
                            viewModel.updateLocationMethod(it) 
                            if (it == LocationMethod.MAP && settings.customLat == null) {
                                onNavigateToMap()
                            }
                        }
                    }
                )
                
                if (settings.locationMethod == LocationMethod.MAP) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateToMap, 
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (settings.customLat != null) 
                                androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.edit_location_map) 
                            else 
                                androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.pick_location_map)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))

            // Temperature Unit Settings
            SettingsSection(
                title = androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.temperature_unit), 
                icon = Icons.Default.Thermostat
            ) {
                val optionsStr = TempUnit.entries.map { androidx.compose.ui.res.stringResource(it.titleResId) }
                val selectedStr = androidx.compose.ui.res.stringResource(settings.temperatureUnit.titleResId)
                SettingsOptionGroup(
                    options = optionsStr,
                    selectedOption = selectedStr,
                    onOptionSelected = { displayStr ->
                        val unit = TempUnit.entries.find { optionsStr[TempUnit.entries.indexOf(it)] == displayStr }
                        unit?.let { viewModel.updateTemperatureUnit(it) }
                    }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))

            // Wind Speed Settings
            SettingsSection(
                title = androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.wind_speed_unit), 
                icon = Icons.Default.Speed
            ) {
                val optionsStr = WindUnit.entries.map { androidx.compose.ui.res.stringResource(it.titleResId) }
                val selectedStr = androidx.compose.ui.res.stringResource(settings.windSpeedUnit.titleResId)
                SettingsOptionGroup(
                    options = optionsStr,
                    selectedOption = selectedStr,
                    onOptionSelected = { displayStr ->
                        val unit = WindUnit.entries.find { optionsStr[WindUnit.entries.indexOf(it)] == displayStr }
                        unit?.let { viewModel.updateWindSpeedUnit(it) }
                    }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))

            // Language Settings
            SettingsSection(
                title = androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.language), 
                icon = Icons.Default.Language
            ) {
                val optionsStr = Language.entries.map { androidx.compose.ui.res.stringResource(it.titleResId) }
                val selectedStr = androidx.compose.ui.res.stringResource(settings.language.titleResId)
                SettingsOptionGroup(
                    options = optionsStr,
                    selectedOption = selectedStr,
                    onOptionSelected = { displayStr ->
                        val lang = Language.entries.find { optionsStr[Language.entries.indexOf(it)] == displayStr }
                        lang?.let { viewModel.updateLanguage(it) }
                    }
                )
            }
        }
    }
}

/**
 * A standard section within the settings screen containing a title, icon, and config components.
 */
@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        content()
    }
}

/**
 * Renders a row of selectable pill-shaped options.
 */
@Composable
private fun SettingsOptionGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEach { option ->
            SettingsOptionPill(
                text = option,
                isSelected = option == selectedOption,
                modifier = Modifier.weight(1f),
                onClick = { onOptionSelected(option) }
            )
        }
    }
}

/**
 * Single selectable pill used for enumerations like units/languages.
 */
@Composable
private fun SettingsOptionPill(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor
        )
    }
}

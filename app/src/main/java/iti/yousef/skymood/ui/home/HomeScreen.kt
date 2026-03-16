package iti.yousef.skymood.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import iti.yousef.skymood.data.model.ForecastItem
import iti.yousef.skymood.data.model.ForecastResponse
import iti.yousef.skymood.data.model.WeatherUiState
import iti.yousef.skymood.data.model.UiEvent
import iti.yousef.skymood.data.local.settings.SettingsPreferences
import iti.yousef.skymood.data.local.settings.WindUnit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Main Home screen composable.
 * Observes the ViewModel's StateFlow and renders the appropriate UI:
 * Loading shimmer, Error with retry, or the full weather display.
 */
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToAlerts: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val weatherState by viewModel.weatherState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (val state = weatherState) {
                is WeatherUiState.Loading -> LoadingView(scaffoldPadding = padding)
                is WeatherUiState.Error -> ErrorView(
                    message = state.message,
                    scaffoldPadding = padding,
                    onRetry = { viewModel.fetchWeather() }
                )
                is WeatherUiState.Success -> WeatherContent(
                    data = state.data,
                    settings = settings,
                    isFavorite = isFavorite,
                    scaffoldPadding = padding,
                    onRefresh = { viewModel.fetchWeather() },
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToFavorites = onNavigateToFavorites,
                    onNavigateToAlerts = onNavigateToAlerts,
                    onToggleFavorite = { viewModel.toggleFavorite() }
                )
            }
        }
    }
}

/**
 * Loading state: centered progress indicator over a calm gradient.
 */
@Composable
private fun LoadingView(scaffoldPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(Color(0xFF1565C0), Color(0xFF42A5F5), Color(0xFF90CAF9))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(scaffoldPadding)
        ) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.fetching_weather),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Error state: shows a creative and attractive error card with a retry button.
 * Handles different error types like Offline or Location Disabled.
 */
@Composable
private fun ErrorView(
    message: String,
    scaffoldPadding: PaddingValues,
    onRetry: () -> Unit
) {
    var locationGranted by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) {
            onRetry()
        }
    }

    // Design tokens based on error type
    val errorData = when (message) {
        "OFFLINE" -> ErrorDesignData(
            icon = Icons.Default.WifiOff,
            title = androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.oops),
            description = "It seems you're offline. Please check your internet connection to get the latest weather updates.",
            gradient = listOf(Color(0xFF454E5F), Color(0xFF2C313C))
        )
        "LOCATION_DISABLED" -> ErrorDesignData(
            icon = Icons.Default.LocationOff,
            title = "Location Required",
            description = "We need your location to show local weather. Please enable GPS and try again.",
            gradient = listOf(Color(0xFF03A9F4), Color(0xFF3F51B5))
        )
        else -> ErrorDesignData(
            icon = Icons.Default.Warning,
            title = androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.oops),
            description = message,
            gradient = listOf(Color(0xFF1A237E), Color(0xFF283593))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(errorData.gradient)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Aesthetic background elements
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(Color.White.copy(alpha = 0.03f), androidx.compose.foundation.shape.CircleShape)
                .align(Alignment.TopEnd)
                .padding(32.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(scaffoldPadding)
                .padding(32.dp)
                .fillMaxWidth()
        ) {
            // Creative Icon Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(32.dp)
                    )
            ) {
                Icon(
                    imageVector = errorData.icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = errorData.title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = errorData.description,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            ElevatedButton(
                onClick = {
                    if (message == "LOCATION_DISABLED") {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        onRetry()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1A237E)
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Retry")
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.try_again),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private data class ErrorDesignData(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val gradient: List<Color>
)

/**
 * Main weather content layout: animated background + scrollable forecast data.
 * Shows current weather, detail cards, hourly forecast, and 5-day forecast.
 */
@Composable
private fun WeatherContent(
    data: ForecastResponse,
    settings: SettingsPreferences,
    isFavorite: Boolean,
    scaffoldPadding: PaddingValues,
    onRefresh: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val currentItem = data.list.firstOrNull()
    val weatherCode = currentItem?.weather?.firstOrNull()?.id ?: 800
    val icon = currentItem?.weather?.firstOrNull()?.icon ?: "01d"
    val isNight = icon.endsWith("n")

    // Determines the temperature unit symbol
    val tempUnit = androidx.compose.ui.res.stringResource(settings.temperatureUnit.titleResId)

    // Animate content entrance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated weather background fills the entire screen
        WeatherAnimationBackground(weatherConditionCode = weatherCode, isNight = isNight)

        // Scrollable content on top of the animation
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = scaffoldPadding.calculateTopPadding(),
                bottom = scaffoldPadding.calculateBottomPadding() + 32.dp
            )
        ) {
            // Top section: City, temperature, description
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -it / 2 }
                ) {
                    CurrentWeatherHeader(
                        data = data,
                        currentItem = currentItem,
                        tempUnit = tempUnit,
                        icon = icon,
                        isFavorite = isFavorite,
                        onRefresh = onRefresh,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToFavorites = onNavigateToFavorites,
                        onNavigateToAlerts = onNavigateToAlerts,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }

            // Detail cards row
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(800, delayMillis = 200))
                ) {
                    WeatherDetailsRow(
                        currentItem = currentItem,
                        settings = settings
                    )
                }
            }

            // Hourly forecast section
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(800, delayMillis = 400))
                ) {
                    HourlyForecastSection(
                        items = getTodayHourlyItems(data),
                        tempUnit = tempUnit
                    )
                }
            }

            // 5-day forecast section
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(800, delayMillis = 600))
                ) {
                    DailyForecastSection(
                        dailyItems = getDailyItems(data),
                        tempUnit = tempUnit
                    )
                }
            }
        }
    }
}

/**
 * Top header showing the city name, current temperature, weather icon,
 * description, and current date/time.
 */
@Composable
private fun CurrentWeatherHeader(
    data: ForecastResponse,
    currentItem: ForecastItem?,
    tempUnit: String,
    icon: String,
    isFavorite: Boolean,
    onRefresh: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top action buttons (Settings and Refresh)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
                IconButton(onClick = onNavigateToFavorites) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Favorites",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
                IconButton(onClick = onNavigateToAlerts) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            Row {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Yellow else Color.White.copy(alpha = 0.8f)
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // City name
        Text(
            text = data.city.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            color = Color.White,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Current date & time
        Text(
            text = formatCurrentDateTime(data.city.timezone),
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Weather icon from OpenWeatherMap
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://openweathermap.org/img/wn/${icon}@4x.png")
                .crossfade(true)
                .build(),
            contentDescription = "Weather icon",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )

        // Temperature
        Text(
            text = "${currentItem?.main?.temp?.toInt() ?: "--"}${tempUnit}",
            fontSize = 72.sp,
            fontWeight = FontWeight.Thin,
            color = Color.White,
            letterSpacing = (-2).sp
        )

        Text(
            text = androidx.compose.ui.res.stringResource(
                id = iti.yousef.skymood.R.string.feels_like,
                currentItem?.main?.feelsLike?.toInt() ?: 0,
                tempUnit
            ),
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Weather description
        val description = currentItem?.weather?.firstOrNull()?.description
            ?.replaceFirstChar { it.uppercase() } ?: ""
        Text(
            text = description,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Row of glass-morphism detail cards showing humidity, wind, pressure, and clouds.
 */
@Composable
private fun WeatherDetailsRow(
    currentItem: ForecastItem?,
    settings: SettingsPreferences
) {
    // Convert wind speed if needed
    val windSpeed = currentItem?.wind?.speed ?: 0.0
    val displayWind = if (settings.windSpeedUnit == WindUnit.MILES_HOUR) {
        "%.1f mph".format(windSpeed * 2.237) // m/s to mph
    } else {
        "%.1f m/s".format(windSpeed)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DetailCard(
            icon = Icons.Default.WaterDrop,
            label = androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.humidity),
            value = "${currentItem?.main?.humidity ?: "--"}%",
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        DetailCard(
            icon = Icons.Default.Air,
            label = androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.wind),
            value = displayWind,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        DetailCard(
            icon = Icons.Default.Thermostat,
            label = androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.pressure),
            value = "${currentItem?.main?.pressure ?: "--"} hPa",
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        DetailCard(
            icon = Icons.Default.Cloud,
            label = androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.clouds),
            value = "${currentItem?.clouds?.all ?: "--"}%",
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * A single detail card with a glassmorphism-style translucent background.
 */
@Composable
private fun DetailCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Horizontal scrollable list of hourly forecast cards for today.
 */
@Composable
private fun HourlyForecastSection(
    items: List<ForecastItem>,
    tempUnit: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Text(
            text = androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.todays_forecast),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                HourlyCard(item = item, tempUnit = tempUnit)
            }
        }
    }
}

/**
 * A single hourly forecast card showing time, icon, and temperature.
 */
@Composable
private fun HourlyCard(item: ForecastItem, tempUnit: String) {
    val icon = item.weather.firstOrNull()?.icon ?: "01d"
    val time = formatTime(item.dt)

    Card(
        modifier = Modifier
            .width(80.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = time,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://openweathermap.org/img/wn/${icon}@2x.png")
                    .crossfade(true)
                    .build(),
                contentDescription = "Weather",
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "${item.main.temp.toInt()}${tempUnit}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Section showing the 5-day forecast as a vertical list of cards.
 */
@Composable
private fun DailyForecastSection(
    dailyItems: List<DailyForecast>,
    tempUnit: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = androidx.compose.ui.res.stringResource(iti.yousef.skymood.R.string.five_day_forecast),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )

        dailyItems.forEach { daily ->
            DailyForecastCard(daily = daily, tempUnit = tempUnit)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * A single daily forecast card showing day name, icon, description,
 * and high/low temperatures.
 */
@Composable
private fun DailyForecastCard(daily: DailyForecast, tempUnit: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day name
            Text(
                text = daily.dayName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.width(80.dp)
            )

            // Weather icon
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://openweathermap.org/img/wn/${daily.icon}@2x.png")
                    .crossfade(true)
                    .build(),
                contentDescription = "Weather",
                modifier = Modifier.size(36.dp)
            )

            // Description
            Text(
                text = daily.description,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                maxLines = 1
            )

            // High / Low temperature
            Text(
                text = "${daily.tempMax.toInt()}° / ${daily.tempMin.toInt()}°",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/** Data class to hold aggregated daily forecast info */
data class DailyForecast(
    val dayName: String,
    val icon: String,
    val description: String,
    val tempMin: Double,
    val tempMax: Double
)

/**
 * Filters forecast items to only include entries for the current day.
 */
private fun getTodayHourlyItems(data: ForecastResponse): List<ForecastItem> {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    return data.list.filter { it.dtTxt.startsWith(today) }
}

/**
 * Groups forecast items by day and aggregates min/max temperatures
 * to produce a list of DailyForecast entries (up to 5 days).
 */
private fun getDailyItems(data: ForecastResponse): List<DailyForecast> {
    val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    val today = dayFormat.format(Date())

    return data.list
        .groupBy { it.dtTxt.substring(0, 10) }
        .filter { it.key != today }
        .map { (dateStr, items) ->
            val date = dayFormat.parse(dateStr)
            val dayName = if (date != null) displayFormat.format(date) else dateStr
            // Use the midday item (or first) for the icon and description
            val midItem = items.find { it.dtTxt.contains("12:00") } ?: items.first()
            DailyForecast(
                dayName = dayName,
                icon = midItem.weather.firstOrNull()?.icon ?: "01d",
                description = midItem.weather.firstOrNull()?.description
                    ?.replaceFirstChar { it.uppercase() } ?: "",
                tempMin = items.minOf { it.main.tempMin },
                tempMax = items.maxOf { it.main.tempMax }
            )
        }
        .take(5)
}

/**
 * Formats a Unix timestamp to a short time string (e.g., "3 PM").
 */
private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h a", Locale.getDefault())
    return sdf.format(Date(timestamp * 1000))
}

/**
 * Formats the current date and time adjusted by the city's timezone offset.
 * Example: "Saturday, 7 Mar 2026 · 5:56 PM"
 */
private fun formatCurrentDateTime(timezoneOffset: Int): String {
    val sdf = SimpleDateFormat("EEEE, d MMM yyyy · h:mm a", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("GMT").also {
        val offsetMs = timezoneOffset * 1000L
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis() + offsetMs
        return sdf.format(cal.time)
    }
}

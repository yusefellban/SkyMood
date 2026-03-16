package iti.yousef.skymood.data.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import iti.yousef.skymood.MainActivity
import iti.yousef.skymood.R
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.model.Entity.AlertType
import iti.yousef.skymood.data.local.settings.LocationMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WeatherAlertReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIF_CHANNEL_ID = "skymood_notifications"
        const val ALARM_CHANNEL_ID = "skymood_alarms"
        const val ACTION_DISMISS_ALARM = "iti.yousef.skymood.ACTION_DISMISS_ALARM"
        const val ALERT_ID_KEY = "alert_id"
        const val ALERT_LABEL_KEY = "alert_label"
        const val ALERT_TYPE_KEY = "alert_type"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_DISMISS_ALARM) {
            val alertId = intent.getIntExtra(ALERT_ID_KEY, -1)
            if (alertId != -1) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(alertId)
            }
            return
        }

        // Reschedule alarms after device reboot
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = context.applicationContext as SkyMood
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeAlerts = app.alertsRepository.getAllAlerts().first()
                    activeAlerts.forEach { alert ->
                        if (alert.isActive && alert.toTime > System.currentTimeMillis()) {
                            AlarmScheduler.scheduleAlarm(context, alert)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        // Handle the incoming scheduled alarm
        val app = context.applicationContext as SkyMood
        val alertId = intent.getIntExtra(ALERT_ID_KEY, -1)
        val alertLabel = intent.getStringExtra(ALERT_LABEL_KEY) ?: "Weather Alert"
        val alertTypeName = intent.getStringExtra(ALERT_TYPE_KEY) ?: AlertType.NOTIFICATION.name
        val alertType = AlertType.valueOf(alertTypeName)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch settings
                val currentSettings = app.settingsRepository.settingsFlow.first()
                var lat: Double? = null
                var lon: Double? = null

                if (currentSettings.locationMethod == LocationMethod.MAP &&
                    currentSettings.customLat != null && currentSettings.customLon != null) {
                    lat = currentSettings.customLat
                    lon = currentSettings.customLon
                } else {
                    // Check for location permissions before accessing lastLocation
                    val hasFine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val hasCoarse = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    
                    if (hasFine || hasCoarse) {
                        try {
                            val location = app.locationRepository.getCurrentLocation()
                            if (location != null) {
                                lat = location.latitude
                                lon = location.longitude
                            }
                        } catch (e: Exception) {
                            // Ignore location errors
                        }
                    }
                }

                var weatherText = "Your weather alert window is now active."

                try {
                    val forecast = if (lat != null && lon != null) {
                        // 1. Try fetching live data if we have coordinates
                        app.weatherRepository.getForecast(
                            lat = lat,
                            lon = lon,
                            units = currentSettings.temperatureUnit.apiValue,
                            lang = currentSettings.language.apiValue
                        ).first()
                    } else {
                        // 2. Fallback to latest cached data if bg location fails
                        app.weatherRepository.getLatestCachedForecast()
                            ?: throw Exception("No cache available")
                    }

                    val currentForecast = forecast.list.firstOrNull()
                    if (currentForecast != null) {
                        val temp = currentForecast.main.temp.toInt()
                        val desc = currentForecast.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: ""
                        val unitSymbol = if (currentSettings.temperatureUnit.apiValue == "metric") "°C" else if (currentSettings.temperatureUnit.apiValue == "imperial") "°F" else "K"
                        weatherText = "$temp$unitSymbol in ${forecast.city.name}, $desc"
                    }
                } catch (e: Exception) {
                    // 3. Keep default text if absolutely everything fails
                }

                // Show Notification
                val notifId = if (alertId > 0) alertId else System.currentTimeMillis().toInt()

                val channelId = if (alertType == AlertType.ALARM) ALARM_CHANNEL_ID else NOTIF_CHANNEL_ID
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val importance = if (alertType == AlertType.ALARM)
                    NotificationManager.IMPORTANCE_HIGH
                else
                    NotificationManager.IMPORTANCE_DEFAULT

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channelName = if (alertType == AlertType.ALARM) "Weather Alarms" else "Weather Notifications"
                    val channel = NotificationChannel(channelId, channelName, importance).apply {
                        description = if (alertType == AlertType.ALARM) "Active weather alarms" else "Regular weather notifications"
                        if (alertType == AlertType.ALARM) {
                            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                            setSound(alarmSound, audioAttributes)
                            enableVibration(true)
                            vibrationPattern = longArrayOf(0, 500, 200, 500)
                        }
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                val tapIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                val pendingIntent = PendingIntent.getActivity(context, notifId, tapIntent, flags)

                val notificationBuilder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("☁️ SkyMood: $alertLabel")
                    .setContentText(weatherText)
                    .setAutoCancel(alertType != AlertType.ALARM)
                    .setContentIntent(pendingIntent)
                    .setPriority(
                        if (alertType == AlertType.ALARM) NotificationCompat.PRIORITY_MAX
                        else NotificationCompat.PRIORITY_DEFAULT
                    )
                    .setCategory(if (alertType == AlertType.ALARM) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_EVENT)

                if (alertType == AlertType.ALARM) {
                    val dismissIntent = Intent(context, WeatherAlertReceiver::class.java).apply {
                        action = ACTION_DISMISS_ALARM
                        putExtra(ALERT_ID_KEY, notifId)
                    }
                    val dismissPendingIntent = PendingIntent.getBroadcast(
                        context, 
                        notifId + 100, 
                        dismissIntent, 
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    notificationBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
                    notificationBuilder.setOngoing(true)
                }

                notificationManager.notify(notifId, notificationBuilder.build())
                
                // If it's an alarm, it will keep making sound until dismissed thanks to high importance channel + ongoing flag
            } finally {
                pendingResult.finish()
            }
        }
    }
}

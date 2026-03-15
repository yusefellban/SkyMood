package iti.yousef.skymood.data.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import iti.yousef.skymood.MainActivity
import iti.yousef.skymood.R
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.local.AlertType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherAlertReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "skymood_alerts"
        const val ALERT_ID_KEY = "alert_id"
        const val ALERT_LABEL_KEY = "alert_label"
        const val ALERT_TYPE_KEY = "alert_type"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Reschedule alarms after device reboot
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = context.applicationContext as SkyMood
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeAlerts = app.alertDao.getActiveAlerts()
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
        val alertId = intent.getIntExtra(ALERT_ID_KEY, -1)
        val alertLabel = intent.getStringExtra(ALERT_LABEL_KEY) ?: "Weather Alert"
        val alertTypeName = intent.getStringExtra(ALERT_TYPE_KEY) ?: AlertType.NOTIFICATION.name
        val alertType = AlertType.valueOf(alertTypeName)

        // Generate a unique notification ID (defaults to current time if alertId is invalid)
        val notifId = if (alertId > 0) alertId else System.currentTimeMillis().toInt()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val importance = if (alertType == AlertType.ALARM)
            NotificationManager.IMPORTANCE_HIGH
        else
            NotificationManager.IMPORTANCE_DEFAULT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "SkyMood Weather Alerts", importance).apply {
                description = "Active weather alerts"
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

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("☁️ SkyMood: $alertLabel")
            .setContentText("Your weather alert window is now active.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                if (alertType == AlertType.ALARM) NotificationCompat.PRIORITY_MAX
                else NotificationCompat.PRIORITY_DEFAULT
            )

        notificationManager.notify(notifId, notificationBuilder.build())
    }
}

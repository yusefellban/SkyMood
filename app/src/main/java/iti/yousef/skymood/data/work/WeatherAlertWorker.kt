package iti.yousef.skymood.data.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import iti.yousef.skymood.MainActivity
import iti.yousef.skymood.R
import iti.yousef.skymood.SkyMood
import iti.yousef.skymood.data.local.AlertType

/**
 * Background worker that checks active alerts.
 * Runs via WorkManager when the alert window is active.
 * Displays either a silent notification or an alarm sound notification.
 */
class WeatherAlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "skymood_alerts"
        const val ALERT_ID_KEY = "alert_id"
        const val ALERT_LABEL_KEY = "alert_label"
        const val ALERT_TYPE_KEY = "alert_type"
    }

    override suspend fun doWork(): Result {
        val app = applicationContext as SkyMood
        val alertId = inputData.getInt(ALERT_ID_KEY, -1)
        val alertLabel = inputData.getString(ALERT_LABEL_KEY) ?: "Weather Alert"
        val alertTypeName = inputData.getString(ALERT_TYPE_KEY) ?: AlertType.NOTIFICATION.name
        val alertType = AlertType.valueOf(alertTypeName)

        // Ensure alert is still active
        val activeAlerts = app.alertDao.getActiveAlerts()
        val isStillActive = activeAlerts.any { it.id == alertId }

        if (!isStillActive) return Result.success()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel
        val channel = NotificationChannel(
            CHANNEL_ID,
            "SkyMood Weather Alerts",
            if (alertType == AlertType.ALARM) NotificationManager.IMPORTANCE_HIGH
            else NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Active weather alerts"
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, alertId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("☁️ SkyMood Alert: $alertLabel")
            .setContentText("Your weather alert is active. Check current conditions.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                if (alertType == AlertType.ALARM) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )

        if (alertType == AlertType.ALARM) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            notificationBuilder.setSound(alarmSound)
            notificationBuilder.setVibrate(longArrayOf(0, 500, 200, 500))
        }

        notificationManager.notify(alertId, notificationBuilder.build())

        return Result.success()
    }
}

package com.example.breez.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.breez.data.db.entity.AlertEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    fun scheduleExactAlarm(alert: AlertEntity) {
        val triggerMs = alert.startTime.time
        if (triggerMs <= System.currentTimeMillis()) {
            Log.w(TAG, "Alert ${alert.id} start time is in the past — skipping")
            return
        }

        val pendingIntent = buildPendingIntent(alert.id) ?: return

        try {
            if (canScheduleExact()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMs,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMs,
                    pendingIntent
                )
            }
        } catch (se: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm for ${alert.id}: ${se.message}")
        }
    }

    fun cancelAlarm(alertId: Long) {
        val pendingIntent = buildPendingIntent(alertId, createIfMissing = false) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Alarm cancelled for alert $alertId")
    }


    private fun buildPendingIntent(alertId: Long, createIfMissing: Boolean = true): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALERT_ID, alertId)
            action = "${AlarmReceiver.ACTION_WEATHER_ALARM}.$alertId"
        }
        val flags = if (createIfMissing)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE

        return PendingIntent.getBroadcast(context, alertId.toInt(), intent, flags)
    }

    private fun canScheduleExact(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            alarmManager.canScheduleExactAlarms()
        else
            true
    }

    companion object {
        private const val TAG = "WeatherAlarmManager"
    }
}
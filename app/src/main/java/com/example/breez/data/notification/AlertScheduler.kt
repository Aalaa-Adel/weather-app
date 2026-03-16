package com.example.breez.data.notification

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.breez.data.db.entity.AlertEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val weatherAlarmManager: WeatherAlarmManager
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleAlert(alert: AlertEntity) {
        if (!alert.isActive) {
            Log.d(TAG, "scheduleAlert: alert ${alert.id} is inactive — skipping")
            return
        }

        if (alert.isOneTime()) {
            Log.d(TAG, "scheduleAlert: scheduling EXACT alarm for alert ${alert.id}")
            weatherAlarmManager.scheduleExactAlarm(alert)
        } else {
            Log.d(TAG, "scheduleAlert: scheduling PERIODIC work for alert ${alert.id}")
            schedulePeriodicWork(alert)
        }
    }

    fun cancelAlert(alertId: Long) {
        Log.d(TAG, "cancelAlert: cancelling alert $alertId")
        weatherAlarmManager.cancelAlarm(alertId)
        workManager.cancelUniqueWork(workName(alertId))
        workManager.cancelAllWorkByTag(workTag(alertId))
    }

    fun scheduleTestNotification(alert: AlertEntity) {
        val req = OneTimeWorkRequestBuilder<WeatherNotificationWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .setInputData(workData(alert.id, isOneTime = true))
            .addTag("test_${workTag(alert.id)}")
            .build()

        workManager.enqueueUniqueWork(
            "test_${workName(alert.id)}",
            ExistingWorkPolicy.REPLACE,
            req
        )
        Log.d(TAG, "Test notification queued for alert ${alert.id}")
    }

    private fun schedulePeriodicWork(alert: AlertEntity) {
        val req = PeriodicWorkRequestBuilder<WeatherNotificationWorker>(
            repeatInterval         = 30,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setInputData(workData(alert.id, isOneTime = false))
            .addTag(workTag(alert.id))
            .build()

        workManager.enqueueUniquePeriodicWork(
            workName(alert.id),
            ExistingPeriodicWorkPolicy.KEEP,
            req
        )
    }

    private fun workData(alertId: Long, isOneTime: Boolean) = Data.Builder()
        .putLong(WeatherNotificationWorker.KEY_ALERT_ID, alertId)
        .putBoolean(WeatherNotificationWorker.KEY_IS_ONE_TIME, isOneTime)
        .build()

    private fun workName(alertId: Long) = "${WeatherNotificationWorker.WORK_TAG_PREFIX}$alertId"
    private fun workTag (alertId: Long) = "${WeatherNotificationWorker.WORK_TAG_PREFIX}$alertId"

    companion object {
        private const val TAG = "AlertScheduler"
    }
}
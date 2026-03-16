package com.example.breez.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alertId = intent.getLongExtra(EXTRA_ALERT_ID, -1L)
        if (alertId == -1L) {
            Log.w(TAG, "AlarmReceiver fired with no alertId")
            return
        }

        Log.d(TAG, "AlarmReceiver: alarm fired for alertId=$alertId")
        val inputData = Data.Builder()
            .putLong(WeatherNotificationWorker.KEY_ALERT_ID, alertId)
            .putBoolean(WeatherNotificationWorker.KEY_IS_ONE_TIME, true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<WeatherNotificationWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context)
            .enqueue(workRequest)
    }

    companion object {
        const val EXTRA_ALERT_ID = "extra_alert_id"
        const val ACTION_WEATHER_ALARM = "com.example.breez.WEATHER_ALARM"
        private const val TAG = "AlarmReceiver"
    }
}
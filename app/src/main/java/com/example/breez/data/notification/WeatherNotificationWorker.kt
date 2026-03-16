package com.example.breez.data.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.breez.data.db.entity.AlertEntity
import com.example.breez.data.location.LocationProvider
import com.example.breez.data.repository.BreezRepository
import com.example.breez.data.util.ApiResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.Date
@HiltWorker
class WeatherNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: BreezRepository,
    private val notificationHelper: NotificationHelper,
    private val locationProvider: LocationProvider
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val alertId   = inputData.getLong(KEY_ALERT_ID, -1L)
        val isOneTime = inputData.getBoolean(KEY_IS_ONE_TIME, false)

        if (alertId == -1L) return Result.failure()

        return try {
            val alert = repository.getAlertById(alertId) ?: return Result.success() // deleted
            if (!alert.isActive) return Result.success()

            if (isOneTime) {
                showNotification(alert)
                repository.updateAlert(alert.copy(isActive = false))
                Result.success()
            } else {
                handlePeriodicWork(alert)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in worker for alert $alertId", e)
            // One-time: don't retry — would cause duplicate notification on retry
            if (isOneTime) Result.failure() else Result.retry()
        }
    }

    // ── Periodic ──────────────────────────────────────────────────────────────

    private suspend fun handlePeriodicWork(alert: AlertEntity): Result {
        val now = Date()

        // Past end date → deactivate and stop
        if (now.after(alert.endTime)) {
            repository.updateAlert(alert.copy(isActive = false))
            return Result.success()
        }

        // Before start date → too early
        if (now.before(alert.startTime)) return Result.success()

        // Check time-of-day window
        if (!isWithinTimeWindow(alert.startTime, alert.endTime)) return Result.success()

        showNotification(alert)
        return Result.success()
    }

    // ── Notification logic ────────────────────────────────────────────────────

    /**
     * Resolves coordinates (stored or live GPS), fetches weather, shows notification.
     */
    private suspend fun showNotification(alert: AlertEntity) {
        val (lat, lon) = resolveCoordinates(alert) ?: run {
            // No coordinates at all — still show a degraded notification
            notificationHelper.showWeatherNotification(
                alertId   = alert.id,
                title     = alert.title,
                message   = "Unable to determine location for this alert.",
                alertType = alert.alertType
            )
            return
        }

        val weatherResult = repository.getCurrentWeather(
            lat   = lat,
            lon   = lon,
            units = "metric",
            lang  = "en"
        )

        val message = when (weatherResult) {
            is ApiResult.Success -> {
                val w    = weatherResult.data
                val city = alert.cityName ?: w.name
                val desc = w.weather.firstOrNull()?.description
                    ?.replaceFirstChar { it.uppercase() } ?: ""
                "$city: ${w.main.temp.toInt()}°C — $desc"
            }
            is ApiResult.Error   ->
                "${alert.cityName ?: "Location"}: Unable to fetch weather (${weatherResult.message})"
            is ApiResult.Loading ->
                "${alert.cityName ?: "Location"}: Fetching weather…"
        }

        notificationHelper.showWeatherNotification(
            alertId   = alert.id,
            title     = alert.title,
            message   = message,
            alertType = alert.alertType
        )
    }

    /**
     * Returns (lat, lon) to use for weather fetch.
     *
     * Priority:
     *  1. Explicit coords stored on the entity (useCurrentLocation == false).
     *  2. Live GPS fix via LocationProvider  (useCurrentLocation == true).
     *  3. null → caller shows a degraded notification.
     */
    private suspend fun resolveCoordinates(alert: AlertEntity): Pair<Double, Double>? {
        if (alert.latitude != null && alert.longitude != null) {
            return Pair(alert.latitude, alert.longitude)
        }
        if (alert.useCurrentLocation) {
            return locationProvider.getCurrentLocation()   // returns null if permission/GPS unavailable
        }
        return null
    }

    // ── Time-of-day window check ──────────────────────────────────────────────

    private fun isWithinTimeWindow(startTime: Date, endTime: Date): Boolean {
        val now = Calendar.getInstance()
        val cur = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val s  = Calendar.getInstance().apply { time = startTime }
        val startMin = s.get(Calendar.HOUR_OF_DAY) * 60 + s.get(Calendar.MINUTE)

        val e  = Calendar.getInstance().apply { time = endTime }
        val endMin = e.get(Calendar.HOUR_OF_DAY) * 60 + e.get(Calendar.MINUTE)

        return if (startMin <= endMin)
            cur in startMin..endMin            // same-day window (e.g. 9 AM–6 PM)
        else
            cur >= startMin || cur <= endMin   // overnight window (e.g. 10 PM–6 AM)
    }

    companion object {
        const val KEY_ALERT_ID    = "alert_id"
        const val KEY_IS_ONE_TIME = "is_one_time"
        const val WORK_TAG_PREFIX = "weather_alert_"
        private const val TAG = "WeatherNotifWorker"
    }
}
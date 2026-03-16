// app/src/main/java/com/example/breez/BreezApp.kt
package com.example.breez

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class.
 *
 * Implements Configuration.Provider so that WorkManager uses
 * HiltWorkerFactory instead of the default factory.
 * This is required for @HiltWorker (WeatherNotificationWorker) to
 * receive its injected dependencies (Repository, LocationProvider, etc.).
 *
 * ALSO REQUIRES in AndroidManifest.xml:
 *   Remove the default WorkManagerInitializer from the startup provider
 *   (see the tools:node="remove" block in the manifest).
 */
@HiltAndroidApp
class BreezApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}


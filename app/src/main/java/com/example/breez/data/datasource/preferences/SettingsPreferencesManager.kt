package com.example.breez.data.datasource.preferences


import android.content.Context
import com.example.breez.data.datasource.preferences.TemperatureUnit.CELSIUS
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TemperatureUnit(
    val storageValue: String,
    val apiValue: String,
    val displayName: String
) {
    CELSIUS("celsius", "metric", "Celsius"),
    FAHRENHEIT("fahrenheit", "imperial", "Fahrenheit"),
    KELVIN("kelvin", "standard", "Kelvin");

    companion object {
        fun fromStorage(value: String?): TemperatureUnit {
            return entries.firstOrNull { it.storageValue == value } ?: CELSIUS
        }
    }
}

enum class LocationSource(
    val storageValue: String,
    val displayName: String
) {
    GPS("gps", "GPS"),
    MAP("map", "Map");

    companion object {
        fun fromStorage(value: String?): LocationSource {
            return entries.firstOrNull { it.storageValue == value } ?: GPS
        }
    }
}

enum class AppLanguage(
    val storageValue: String,
    val apiValue: String,
    val displayName: String
) {
    ARABIC("ar", "ar", "العربية"),
    ENGLISH("en", "en", "English");

    companion object {
        fun fromStorage(value: String?): AppLanguage {
            return entries.firstOrNull { it.storageValue == value } ?: ARABIC
        }
    }
}

enum class ThemeMode(
    val storageValue: String,
    val displayName: String
) {
    SYSTEM("system", "System"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark");

    companion object {
        fun fromStorage(value: String?): ThemeMode {
            return entries.firstOrNull { it.storageValue == value } ?: SYSTEM
        }
    }
}

enum class WindSpeedUnit(
    val storageValue: String,
    val apiValue: String,
    val displayName: String
) {

    METERS_PER_SECOND("m/s", "metric", "m/s"),
    KILOMETERS_PER_HOUR("km/h", "imperial", "km/h"),
    MILES_PER_HOUR("mph", "imperial", "mph");

    companion object {
        fun fromStorage(value: String?): WindSpeedUnit {
            return WindSpeedUnit.entries.firstOrNull { it.storageValue == value }
                ?: METERS_PER_SECOND
        }
    }


}

data class AppSettings(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val locationSource: LocationSource = LocationSource.GPS,
    val language: AppLanguage = AppLanguage.ARABIC,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val windSpeedUnit: WindSpeedUnit = WindSpeedUnit.METERS_PER_SECOND
)

@Singleton
class SettingsPreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {

    private val sharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(readSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    fun getCurrentSettings(): AppSettings = _settingsFlow.value

    fun saveSettings(settings: AppSettings) {
        sharedPreferences.edit()
            .putString(KEY_TEMPERATURE_UNIT, settings.temperatureUnit.storageValue)
            .putString(KEY_LOCATION_SOURCE, settings.locationSource.storageValue)
            .putString(KEY_LANGUAGE, settings.language.storageValue)
            .putString(KEY_THEME_MODE, settings.themeMode.storageValue)
            .putString(KEY_WIND_SPEED_UNIT, settings.windSpeedUnit.storageValue)
            .apply()

        _settingsFlow.value = settings
    }

    private fun readSettings(): AppSettings {
        return AppSettings(
            temperatureUnit = TemperatureUnit.fromStorage(
                sharedPreferences.getString(
                    KEY_TEMPERATURE_UNIT,
                    TemperatureUnit.CELSIUS.storageValue
                )
            ),
            locationSource = LocationSource.fromStorage(
                sharedPreferences.getString(KEY_LOCATION_SOURCE, LocationSource.GPS.storageValue)
            ),
            language = AppLanguage.fromStorage(
                sharedPreferences.getString(KEY_LANGUAGE, AppLanguage.ARABIC.storageValue)
            ),
            themeMode = ThemeMode.fromStorage(
                sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.storageValue)
            ),
            windSpeedUnit = WindSpeedUnit.fromStorage(
                sharedPreferences.getString(
                    KEY_WIND_SPEED_UNIT,
                    WindSpeedUnit.METERS_PER_SECOND.storageValue
                ),
            )
        )
    }

    companion object {
        private const val PREFS_NAME = "breez_settings"

        private const val KEY_TEMPERATURE_UNIT = "temperature_unit"
        private const val KEY_LOCATION_SOURCE = "location_source"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_WIND_SPEED_UNIT = "wind_speed_unit"

    }
}
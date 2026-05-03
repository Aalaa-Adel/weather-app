package com.example.breez.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.core.content.edit
import com.example.breez.data.datasource.preferences.AppLanguage
import java.util.Locale
import javax.inject.Singleton

@Singleton
class LocalizationHelper {
    companion object {
        private const val PREFS_NAME = "localization_prefs"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_SPLASH_SEEN = "splash_seen"

        fun hasSplashBeenSeen(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_SPLASH_SEEN, false)
        }

        fun markSplashAsSeen(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
                putBoolean(KEY_SPLASH_SEEN, true)
            }
        }

        fun saveLanguageToPrefs(context: Context, language: AppLanguage) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
                putString(KEY_LANGUAGE, language.name)
            }
        }

        fun getSavedLanguage(context: Context): AppLanguage {
            val languageCode = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, AppLanguage.ARABIC.name)

            return try {
                AppLanguage.valueOf(languageCode ?: AppLanguage.ARABIC.name)
            } catch (_: Exception) {
                AppLanguage.ARABIC
            }
        }

        fun applyLanguage(context: Context, language: AppLanguage): ContextWrapper {
            val locale = Locale(language.apiValue)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            config.setLayoutDirection(locale)

            val newContext = context.createConfigurationContext(config)
            return ContextWrapper(newContext)
        }

        fun applySavedLanguage(context: Context): ContextWrapper {
            return applyLanguage(context, getSavedLanguage(context))
        }
    }
}
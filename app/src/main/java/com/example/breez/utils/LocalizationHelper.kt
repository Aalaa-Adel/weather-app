package com.example.breez.utils

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import com.example.breez.data.datasource.preferences.AppLanguage
import javax.inject.Singleton
import java.util.Locale


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

        /**
         * تحديد شاشة البداية كمعروضة
         */
        fun markSplashAsSeen(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
                putBoolean(KEY_SPLASH_SEEN, true)
            }
        }

        /**
         * تطبيق اللغة المحفوظة بدون الحقن
         */
        fun applyWithoutDI(context: Context): Context {
            val languageCode = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, null)
            languageCode ?: return context

            val language = try {
                AppLanguage.valueOf(languageCode)
            } catch (_: Exception) {
                return context
            }

            return applyLanguage(context, language)
        }

        /**
         * حفظ اللغة المختارة في التفضيلات
         */
        fun saveLanguageToPrefs(context: Context, language: AppLanguage) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
                putString(KEY_LANGUAGE, language.name)
            }
        }

        /**
         * تطبيق اللغة على السياق
         */
        fun applyLanguage(context: Context, language: AppLanguage): Context {
            val locale = Locale.forLanguageTag(language.apiValue)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            config.setLayoutDirection(locale)

            return context.createConfigurationContext(config)
        }

        /**
         * تطبيق اللغة على السياق مع تحديث الموارد
         */
        fun applyLanguageWithUpdate(context: Context, language: AppLanguage) {
            val locale = Locale.forLanguageTag(language.apiValue)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            config.setLayoutDirection(locale)

            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }

        /**
         * تطبيق اللغة المحفوظة مع تحديث الموارد
         */
        fun applyLanguageWithUpdate(context: Context) {
            val languageCode = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, null)
            languageCode ?: return

            val language = try {
                AppLanguage.valueOf(languageCode)
            } catch (_: Exception) {
                return
            }

            applyLanguageWithUpdate(context, language)
        }
    }
}

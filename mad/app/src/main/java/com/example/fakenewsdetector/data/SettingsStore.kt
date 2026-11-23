package com.example.fakenewsdetector.data

import android.content.Context
import android.content.res.Configuration
import com.example.fakenewsdetector.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class AppLanguage(val tag: String) { ENGLISH("en"), SWAHILI("sw") }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val onboardingSeen: Boolean = false
)

/**
 * Theme, language and first-run flag. SharedPreferences is enough for three
 * values, so no preference library is added.
 *
 * Registered as an OnSharedPreferenceChangeListener-free store: the single
 * instance lives in [FakeNewsApp] and exposes a StateFlow.
 */
class SettingsStore(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun read(): AppSettings = AppSettings(
        themeMode = runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name)!!)
        }.getOrDefault(ThemeMode.SYSTEM),
        language = runCatching {
            AppLanguage.valueOf(prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.name)!!)
        }.getOrDefault(AppLanguage.ENGLISH),
        onboardingSeen = prefs.getBoolean(KEY_ONBOARDING, false)
    )

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
        _settings.value = _settings.value.copy(language = language)
    }

    fun setOnboardingSeen(seen: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING, seen).apply()
        _settings.value = _settings.value.copy(onboardingSeen = seen)
    }

    companion object {
        private const val PREFS = "fake_news_settings"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_ONBOARDING = "onboarding_seen"

        /** Applies the chosen app language to any activity context. */
        fun wrap(context: Context, language: AppLanguage): Context {
            val locale = Locale(language.tag)
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        }
    }
}

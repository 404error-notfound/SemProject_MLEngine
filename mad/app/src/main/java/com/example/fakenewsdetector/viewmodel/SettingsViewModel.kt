package com.example.fakenewsdetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fakenewsdetector.AppContainer
import com.example.fakenewsdetector.data.AppLanguage
import com.example.fakenewsdetector.data.AppSettings
import com.example.fakenewsdetector.data.SettingsStore
import com.example.fakenewsdetector.ui.theme.ThemeMode
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(private val store: SettingsStore) : ViewModel() {

    val settings: StateFlow<AppSettings> = store.settings

    fun setThemeMode(mode: ThemeMode) = store.setThemeMode(mode)

    fun setLanguage(language: AppLanguage) = store.setLanguage(language)

    fun setOnboardingSeen(seen: Boolean) = store.setOnboardingSeen(seen)

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(container.settings) }
        }
    }
}

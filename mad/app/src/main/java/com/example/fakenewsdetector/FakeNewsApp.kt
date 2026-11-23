package com.example.fakenewsdetector

import android.app.Application
import android.content.Context
import com.example.fakenewsdetector.data.AppDatabase
import com.example.fakenewsdetector.data.HistoryRepository
import com.example.fakenewsdetector.data.RoomHistoryRepository
import com.example.fakenewsdetector.data.SettingsStore
import com.example.fakenewsdetector.domain.AnalysisRepository
import com.example.fakenewsdetector.domain.ClassifierAnalysisRepository
import com.example.fakenewsdetector.domain.CorrectionFactory
import com.example.fakenewsdetector.ml.MockClassifier

/**
 * Manual dependency container. Small enough that a DI framework would be
 * more setup than value.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val settings: SettingsStore by lazy { SettingsStore(appContext) }

    val analysisRepository: AnalysisRepository by lazy {
        // Swap MockClassifier for TfliteClassifier / an HTTP client here only.
        ClassifierAnalysisRepository(MockClassifier())
    }

    val historyRepository: HistoryRepository by lazy {
        RoomHistoryRepository(AppDatabase.get(appContext).checkDao())
    }

    val correctionFactory: CorrectionFactory by lazy { CorrectionFactory(appContext) }
}

class FakeNewsApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

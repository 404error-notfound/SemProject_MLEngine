package com.example.fakenewsdetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fakenewsdetector.AppContainer
import com.example.fakenewsdetector.domain.AnalysisFailure
import com.example.fakenewsdetector.domain.AnalysisRepository
import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.domain.toCheckResult
import com.example.fakenewsdetector.data.HistoryRepository
import com.example.fakenewsdetector.ml.Language
import com.example.fakenewsdetector.ml.LanguageDetector
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** What went wrong, in terms the screen knows how to explain. */
sealed interface CheckError {
    data object Offline : CheckError
    data object Timeout : CheckError
    data object Server : CheckError
    data object EmptyText : CheckError
    data class TooLong(val maxChars: Int) : CheckError
}

/** One sealed state per screen, as required by the brief. */
sealed interface CheckUiState {
    /** Nothing typed yet (also the state after Clear/Cancel). */
    data object Idle : CheckUiState

    data class Checking(val text: String, val language: Language) : CheckUiState

    data class Finished(val result: CheckResult) : CheckUiState

    data class Failed(val error: CheckError, val retryText: String) : CheckUiState
}

class CheckViewModel(
    private val analysisRepository: AnalysisRepository,
    private val historyRepository: HistoryRepository,
    private val detector: LanguageDetector = LanguageDetector(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel() {

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    private val _state = MutableStateFlow<CheckUiState>(CheckUiState.Idle)
    val state: StateFlow<CheckUiState> = _state.asStateFlow()

    private val _detectedLanguage = MutableStateFlow(Language.UNKNOWN)
    val detectedLanguage: StateFlow<Language> = _detectedLanguage.asStateFlow()

    private var job: Job? = null
    private var lastRequestedText: String = ""
    private var lastSourceApp: String? = null

    fun onInputChange(text: String) {
        _input.value = text.take(MAX_CHARS)
        updateLanguage(_input.value)
        if (_state.value is CheckUiState.Failed) _state.value = CheckUiState.Idle
    }

    /** Explicit paste (button or suggestion chip); never read silently. */
    fun paste(text: String) {
        onInputChange(text)
    }

    fun analyze(text: String? = null, sourceApp: String? = null) {
        val clean = (text ?: _input.value).trim()
        if (text != null) {
            _input.value = clean.take(MAX_CHARS)
            updateLanguage(_input.value)
        }
        when {
            clean.isEmpty() -> {
                _state.value = CheckUiState.Failed(CheckError.EmptyText, "")
                return
            }
            clean.length > MAX_CHARS -> {
                _state.value = CheckUiState.Failed(CheckError.TooLong(MAX_CHARS), clean)
                return
            }
        }
        lastRequestedText = clean
        lastSourceApp = sourceApp
        job?.cancel()
        _state.value = CheckUiState.Checking(clean, _detectedLanguage.value)
        job = scope.launch {
            try {
                val classification = analysisRepository.analyze(clean)
                val result = classification.toCheckResult(
                    text = clean,
                    sourceApp = sourceApp
                )
                val saved = historyRepository.save(result)
                _state.value = CheckUiState.Finished(saved)
            } catch (e: CancellationException) {
                throw e
            } catch (e: AnalysisFailure) {
                _state.value = CheckUiState.Failed(e.toCheckError(), clean)
            } catch (e: Exception) {
                _state.value = CheckUiState.Failed(CheckError.Server, clean)
            }
        }
    }

    /** Cancels the in-flight request and returns the screen to its empty state. */
    fun cancel() {
        job?.cancel()
        job = null
        _state.value = CheckUiState.Idle
    }

    fun retry() {
        analyze(lastRequestedText.ifBlank { _input.value }, lastSourceApp)
    }

    /** Call after navigating to the result so back returns to an empty Check screen. */
    fun consumeResult() {
        if (_state.value is CheckUiState.Finished) _state.value = CheckUiState.Idle
    }

    fun clear() {
        job?.cancel()
        job = null
        _input.value = ""
        _detectedLanguage.value = Language.UNKNOWN
        _state.value = CheckUiState.Idle
    }

    private fun updateLanguage(text: String) {
        _detectedLanguage.value = if (text.isBlank()) Language.UNKNOWN else detector.detect(text)
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    companion object {
        /** Maximum characters accepted in the post field. */
        const val MAX_CHARS = 2000

        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CheckViewModel(container.analysisRepository, container.historyRepository)
            }
        }
    }
}

private fun AnalysisFailure.toCheckError(): CheckError = when (this) {
    AnalysisFailure.Offline -> CheckError.Offline
    AnalysisFailure.Timeout -> CheckError.Timeout
    AnalysisFailure.Failed -> CheckError.Server
}

package com.example.fakenewsdetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fakenewsdetector.AppContainer
import com.example.fakenewsdetector.data.HistoryRepository
import com.example.fakenewsdetector.domain.AnalysisFailure
import com.example.fakenewsdetector.domain.AnalysisRepository
import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.domain.toCheckResult
import com.example.fakenewsdetector.ml.Verdict
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryFilter { ALL, LIKELY_FALSE, UNVERIFIED, LIKELY_TRUE }

data class HistoryUiState(
    val isLoading: Boolean = true,
    val items: List<CheckResult> = emptyList(),
    val filter: HistoryFilter = HistoryFilter.ALL
) {
    val filtered: List<CheckResult>
        get() = when (filter) {
            HistoryFilter.ALL -> items
            HistoryFilter.LIKELY_FALSE -> items.filter { it.verdict == Verdict.LIKELY_FAKE }
            HistoryFilter.UNVERIFIED -> items.filter { it.verdict == Verdict.UNVERIFIED }
            HistoryFilter.LIKELY_TRUE -> items.filter { it.verdict == Verdict.LIKELY_GENUINE }
        }
}

class HistoryViewModel(
    private val historyRepository: HistoryRepository,
    private val analysisRepository: AnalysisRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel() {

    private val _filter = MutableStateFlow(HistoryFilter.ALL)
    private val _rechecked = MutableStateFlow<CheckResult?>(null)
    private val _recheckError = MutableStateFlow<CheckError?>(null)
    private val _pendingUndo = MutableStateFlow<CheckResult?>(null)

    val pendingUndo: StateFlow<CheckResult?> = _pendingUndo.asStateFlow()
    val rechecked: StateFlow<CheckResult?> = _rechecked.asStateFlow()
    val recheckError: StateFlow<CheckError?> = _recheckError.asStateFlow()

    val state: StateFlow<HistoryUiState> = combine(
        historyRepository.history,
        _filter,
        _rechecked
    ) { items, filter, _ ->
        HistoryUiState(isLoading = false, items = items, filter = filter)
    }.stateIn(scope, SharingStarted.Eagerly, HistoryUiState())

    fun setFilter(filter: HistoryFilter) {
        _filter.value = filter
    }

    fun delete(result: CheckResult) {
        val id = result.storedId ?: return
        scope.launch {
            historyRepository.delete(id)
            _pendingUndo.value = result
        }
    }

    /** Undo a swipe-to-delete; restores the row with its original id. */
    fun undoDelete() {
        val pending = _pendingUndo.value ?: return
        _pendingUndo.value = null
        scope.launch { historyRepository.restore(pending) }
    }

    fun dismissedUndo() {
        _pendingUndo.value = null
    }

    fun clearAll() {
        scope.launch {
            historyRepository.clear()
            _pendingUndo.value = null
        }
    }

    /**
     * Facts change: check the stored post again and replace the saved row.
     * The UI navigates to the refreshed result.
     */
    fun recheck(result: CheckResult) {
        val id = result.storedId ?: return
        scope.launch {
            try {
                val classification = analysisRepository.analyze(result.text)
                val refreshed = classification.toCheckResult(
                    text = result.text,
                    sourceApp = result.sourceApp,
                    storedId = id
                )
                val replaced = historyRepository.replace(id, refreshed)
                _rechecked.value = replaced
            } catch (e: CancellationException) {
                throw e
            } catch (e: AnalysisFailure) {
                _recheckError.value = when (e) {
                    AnalysisFailure.Offline -> CheckError.Offline
                    AnalysisFailure.Timeout -> CheckError.Timeout
                    AnalysisFailure.Failed -> CheckError.Server
                }
            } catch (e: Exception) {
                _recheckError.value = CheckError.Server
            }
        }
    }

    fun consumeRechecked() {
        _rechecked.value = null
    }

    fun consumeRecheckError() {
        _recheckError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HistoryViewModel(container.historyRepository, container.analysisRepository)
            }
        }
    }
}

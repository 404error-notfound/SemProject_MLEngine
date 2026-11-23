package com.example.fakenewsdetector

import com.example.fakenewsdetector.data.HistoryRepository
import com.example.fakenewsdetector.domain.AnalysisFailure
import com.example.fakenewsdetector.domain.AnalysisRepository
import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.ml.Classification
import com.example.fakenewsdetector.ml.Language
import com.example.fakenewsdetector.ml.Verdict
import com.example.fakenewsdetector.viewmodel.CheckError
import com.example.fakenewsdetector.viewmodel.CheckUiState
import com.example.fakenewsdetector.viewmodel.CheckViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private val sample = Classification(
    verdict = Verdict.LIKELY_FAKE,
    confidence = 0.9f,
    reason = "Asks you to forward it.",
    language = Language.SWAHILI,
    summary = "Looks like a chain message.",
    reasons = listOf("Asks you to forward it.")
)

private class FakeAnalysis(
    private val block: suspend (String) -> Classification
) : AnalysisRepository {
    val requested = mutableListOf<String>()
    override suspend fun analyze(text: String): Classification {
        requested += text
        return block(text)
    }
}

private class FakeHistory : HistoryRepository {
    val stored = MutableStateFlow<List<CheckResult>>(emptyList())
    var savedCount = 0

    override val history: Flow<List<CheckResult>> = stored

    override suspend fun save(result: CheckResult): CheckResult {
        savedCount++
        val withId = result.copy(storedId = savedCount.toLong())
        stored.value = listOf(withId) + stored.value
        return withId
    }

    override suspend fun replace(id: Long, result: CheckResult): CheckResult =
        result.copy(storedId = id)

    override suspend fun restore(result: CheckResult) = Unit

    override suspend fun delete(id: Long) = Unit

    override suspend fun clear() = Unit

    override suspend fun find(id: Long): CheckResult? = stored.value.find { it.storedId == id }
}

private fun viewModel(
    analysis: AnalysisRepository,
    history: HistoryRepository = FakeHistory()
) = CheckViewModel(
    analysisRepository = analysis,
    historyRepository = history,
    scope = CoroutineScope(Dispatchers.Unconfined)
)

class CheckViewModelTest {

    @Test
    fun `idle to checking to result`() = runBlocking {
        val gate = CompletableDeferred<Classification>()
        val history = FakeHistory()
        val vm = viewModel(FakeAnalysis { gate.await() }, history)

        assertEquals(CheckUiState.Idle, vm.state.value)

        vm.analyze("HABARI KUU tuma sasa")
        assertTrue(vm.state.value is CheckUiState.Checking)

        gate.complete(sample)

        val finished = vm.state.value
        assertTrue(finished is CheckUiState.Finished)
        val result = (finished as CheckUiState.Finished).result
        assertEquals(Verdict.LIKELY_FAKE, result.verdict)
        assertEquals(1, history.savedCount)
        assertEquals(1L, result.storedId)
    }

    @Test
    fun `timeout maps to timeout error`() = runBlocking {
        val vm = viewModel(FakeAnalysis { throw AnalysisFailure.Timeout })
        vm.analyze("some claim")
        val state = vm.state.value as CheckUiState.Failed
        assertEquals(CheckError.Timeout, state.error)
    }

    @Test
    fun `offline and server failures map to their errors`() = runBlocking {
        val offline = viewModel(FakeAnalysis { throw AnalysisFailure.Offline })
        offline.analyze("claim")
        assertEquals(CheckError.Offline, (offline.state.value as CheckUiState.Failed).error)

        val server = viewModel(FakeAnalysis { throw AnalysisFailure.Failed })
        server.analyze("claim")
        assertEquals(CheckError.Server, (server.state.value as CheckUiState.Failed).error)
    }

    @Test
    fun `blank and too long text are rejected without a request`() = runBlocking {
        val analysis = FakeAnalysis { sample }
        val vm = viewModel(analysis)

        vm.analyze("   ")
        assertEquals(CheckError.EmptyText, (vm.state.value as CheckUiState.Failed).error)

        vm.analyze("a".repeat(CheckViewModel.MAX_CHARS + 1))
        val tooLong = vm.state.value as CheckUiState.Failed
        assertTrue(tooLong.error is CheckError.TooLong)
        assertTrue(analysis.requested.isEmpty())
    }

    @Test
    fun `input is capped at the character limit`() {
        val vm = viewModel(FakeAnalysis { sample })
        vm.onInputChange("b".repeat(CheckViewModel.MAX_CHARS + 50))
        assertEquals(CheckViewModel.MAX_CHARS, vm.input.value.length)
    }

    @Test
    fun `cancel returns to idle`() = runBlocking {
        val never = CompletableDeferred<Classification>()
        val vm = viewModel(FakeAnalysis { never.await() })
        vm.analyze("claim")
        assertTrue(vm.state.value is CheckUiState.Checking)
        vm.cancel()
        assertEquals(CheckUiState.Idle, vm.state.value)
    }

    @Test
    fun `retry repeats the last request`() = runBlocking {
        var fail = true
        val analysis = FakeAnalysis {
            if (fail) throw AnalysisFailure.Failed else sample
        }
        val vm = viewModel(analysis)
        vm.analyze("claim")
        assertTrue(vm.state.value is CheckUiState.Failed)

        fail = false
        vm.retry()
        assertTrue(vm.state.value is CheckUiState.Finished)
        assertEquals(listOf("claim", "claim"), analysis.requested)
    }

    @Test
    fun `clear resets input and language`() {
        val vm = viewModel(FakeAnalysis { sample })
        vm.onInputChange("Habari ya serikali leo")
        assertTrue(vm.detectedLanguage.value != Language.UNKNOWN)
        vm.clear()
        assertEquals("", vm.input.value)
        assertEquals(Language.UNKNOWN, vm.detectedLanguage.value)
        assertEquals(CheckUiState.Idle, vm.state.value)
    }
}

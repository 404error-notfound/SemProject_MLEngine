package com.example.fakenewsdetector

import android.content.Intent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.ml.Language
import com.example.fakenewsdetector.ml.Verdict
import com.example.fakenewsdetector.ui.CheckContent
import com.example.fakenewsdetector.ui.HistoryContent
import com.example.fakenewsdetector.ui.theme.FakeNewsDetectorTheme
import com.example.fakenewsdetector.viewmodel.CheckUiState
import com.example.fakenewsdetector.viewmodel.HistoryFilter
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CheckInputTest {

    @get:Rule
    val rule = createComposeRule()

    private fun content(input: String) {
        rule.setContent {
            FakeNewsDetectorTheme {
                CheckContent(
                    input = input,
                    detectedLanguage = Language.UNKNOWN,
                    state = CheckUiState.Idle,
                    showPasteSuggestion = false,
                    onInputChange = {},
                    onPaste = {},
                    onDismissPasteSuggestion = {},
                    onCheck = {},
                    onClear = {},
                    onCancel = {},
                    onRetry = {},
                    onOpenSettings = {}
                )
            }
        }
    }

    @Test
    fun checkButton_isDisabledForEmptyInput() {
        content("")
        val label = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.action_check)
        rule.onNodeWithText(label).assertIsNotEnabled()
    }

    @Test
    fun checkButton_isEnabledOnceTextIsEntered() {
        content("HABARI KUU: tuma sasa")
        val label = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.action_check)
        rule.onNodeWithText(label).assertIsEnabled()
    }
}

class HistoryItemTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun tappingHistoryItem_opensStoredResult() {
        val stored = CheckResult(
            text = "Serikali itatoa pesa kwa kila mtu",
            verdict = Verdict.LIKELY_FAKE,
            confidence = 0.9f,
            language = Language.SWAHILI,
            summary = "Chain message",
            reasons = listOf("Asks you to forward it"),
            checkedAt = System.currentTimeMillis(),
            storedId = 7L
        )
        var opened: CheckResult? = null

        rule.setContent {
            FakeNewsDetectorTheme {
                val snackbar = remember { SnackbarHostState() }
                HistoryContent(
                    items = listOf(stored),
                    filter = HistoryFilter.ALL,
                    isLoading = false,
                    snackbarHostState = snackbar,
                    onFilterChange = {},
                    onOpen = { opened = it },
                    onDelete = {},
                    onClearRequest = {}
                )
            }
        }

        rule.onNodeWithText(stored.text).performClick()
        assertEquals(7L, opened?.storedId)
    }
}

class QuickCheckShareTest {

    @get:Rule
    val rule = createEmptyComposeRule()

    @Test
    fun shareIntent_showsQuickCheckSheetAndVerdict() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sharedText = "HABARI KUU: Serikali itatoa KSh 5,000. Tuma sasa kwa vikundi 10."

        val intent = Intent(context, QuickCheckActivity::class.java).apply {
            putExtra(ShareReceiverActivity.EXTRA_SHARED_TEXT, sharedText)
            putExtra(ShareReceiverActivity.EXTRA_SOURCE_APP, "WhatsApp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        ActivityScenario.launch<QuickCheckActivity>(intent).use {
            val seeFullResult = context.getString(R.string.action_see_full_result)
            rule.waitUntil(timeoutMillis = 15_000) {
                rule.onAllNodesWithText(seeFullResult).fetchSemanticsNodes().isNotEmpty()
            }
            rule.onNodeWithText(
                context.getString(R.string.share_from_source, "WhatsApp")
            ).assertIsDisplayed()
            rule.onNodeWithText(seeFullResult).assertIsDisplayed()
        }
    }
}

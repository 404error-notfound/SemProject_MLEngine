package com.example.fakenewsdetector.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fakenewsdetector.data.AppLanguage
import com.example.fakenewsdetector.data.AppSettings
import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.ml.FactCheckSource
import com.example.fakenewsdetector.ml.Language
import com.example.fakenewsdetector.ml.Verdict
import com.example.fakenewsdetector.ui.components.PrimaryButton
import com.example.fakenewsdetector.ui.theme.FakeNewsDetectorTheme
import com.example.fakenewsdetector.ui.theme.ThemeMode
import com.example.fakenewsdetector.viewmodel.CheckError
import com.example.fakenewsdetector.viewmodel.CheckUiState
import com.example.fakenewsdetector.viewmodel.HistoryFilter

private val swahiliPost =
    "HABARI KUU: Serikali itatoa KSh 5,000 kwa kila Mkenya atakayetuma ujumbe huu kwa vikundi 10 vya WhatsApp leo. Tuma sasa usikose!"

private val englishPost =
    "Public transport fares will double from Monday, an official has confirmed. Please tell everyone you know."

private val falseResult = CheckResult(
    text = swahiliPost,
    verdict = Verdict.LIKELY_FAKE,
    confidence = 0.85f,
    language = Language.SWAHILI,
    summary = "This looks like a chain-message hoax. We found no official announcement of any payout.",
    reasons = listOf(
        "Asks you to forward it. Real announcements don't depend on you sharing.",
        "Promises money for a message. A common pattern in scam and hoax chains.",
        "No official source found."
    ),
    sources = listOf(
        FactCheckSource(
            title = "No, the government is not paying KSh 5,000 per forward",
            publisher = "PesaCheck",
            url = "https://example.org/fact-check-1"
        ),
        FactCheckSource(
            title = "Kenya's 2026 payout hoax, explained",
            publisher = "Africa Check",
            url = "https://example.org/fact-check-2"
        )
    ),
    checkedAt = 1_764_000_000_000
)

private val unverifiedResult = CheckResult(
    text = englishPost,
    verdict = Verdict.UNVERIFIED,
    confidence = 0.42f,
    language = Language.ENGLISH,
    summary = "We couldn't find a reliable source that confirms or denies this. Hold off on sharing until there's an official statement.",
    reasons = listOf(
        "No named official. The post doesn't say who made the announcement.",
        "Not in trusted news. No matching report from the outlets we check."
    ),
    checkedAt = 1_764_100_000_000
)

private val trueResult = unverifiedResult.copy(
    verdict = Verdict.LIKELY_GENUINE,
    confidence = 0.91f,
    summary = "The school calendar change matches the ministry's own notice.",
    reasons = listOf("Official notice published by the Ministry of Education.")
)

@Composable
private fun PreviewFrame(dark: Boolean, content: @Composable () -> Unit) {
    FakeNewsDetectorTheme(mode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT) {
        content()
    }
}

@Preview(name = "Check empty · Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CheckEmptyLight() = PreviewFrame(dark = false) {
    CheckContent(
        input = "",
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

@Preview(name = "Check empty · Dark", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CheckEmptyDark() = PreviewFrame(dark = true) {
    CheckContent(
        input = "",
        detectedLanguage = Language.UNKNOWN,
        state = CheckUiState.Idle,
        showPasteSuggestion = true,
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

@Preview(name = "Check ready · Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CheckReadyLight() = PreviewFrame(dark = false) {
    CheckContent(
        input = swahiliPost,
        detectedLanguage = Language.SWAHILI,
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

@Preview(name = "Check ready · Dark", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CheckReadyDark() = PreviewFrame(dark = true) {
    CheckContent(
        input = swahiliPost,
        detectedLanguage = Language.SWAHILI,
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

@Preview(name = "Checking · Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CheckingLight() = PreviewFrame(dark = false) {
    CheckContent(
        input = swahiliPost,
        detectedLanguage = Language.SWAHILI,
        state = CheckUiState.Checking(swahiliPost, Language.SWAHILI),
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

@Preview(name = "Checking · Dark", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CheckingDark() = PreviewFrame(dark = true) {
    CheckContent(
        input = swahiliPost,
        detectedLanguage = Language.SWAHILI,
        state = CheckUiState.Checking(swahiliPost, Language.SWAHILI),
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

@Preview(name = "Error · Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ErrorLight() = PreviewFrame(dark = false) {
    CheckContent(
        input = englishPost,
        detectedLanguage = Language.ENGLISH,
        state = CheckUiState.Failed(CheckError.Timeout, englishPost),
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

@Preview(name = "Error · Dark", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ErrorDark() = PreviewFrame(dark = true) {
    CheckContent(
        input = englishPost,
        detectedLanguage = Language.ENGLISH,
        state = CheckUiState.Failed(CheckError.Offline, englishPost),
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

@Preview(name = "Result false · Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ResultFalseLight() = PreviewFrame(dark = false) {
    ResultSurfacePreview(result = falseResult)
}

@Preview(name = "Result false · Dark", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ResultFalseDark() = PreviewFrame(dark = true) {
    ResultSurfacePreview(result = falseResult)
}

@Preview(name = "Result unverified · Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ResultUnverifiedLight() = PreviewFrame(dark = false) {
    ResultSurfacePreview(result = unverifiedResult)
}

@Preview(name = "Result unverified · Dark", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ResultUnverifiedDark() = PreviewFrame(dark = true) {
    ResultSurfacePreview(result = unverifiedResult)
}

@Preview(name = "Result true · Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ResultTrueLight() = PreviewFrame(dark = false) {
    ResultSurfacePreview(result = trueResult)
}

@Preview(name = "Result true · Dark", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ResultTrueDark() = PreviewFrame(dark = true) {
    ResultSurfacePreview(result = trueResult)
}

@Preview(name = "History · Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HistoryLight() = PreviewFrame(dark = false) {
    HistorySurfacePreview(
        items = listOf(falseResult, unverifiedResult, trueResult),
        filter = HistoryFilter.ALL
    )
}

@Preview(name = "History · Dark", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HistoryDark() = PreviewFrame(dark = true) {
    HistorySurfacePreview(
        items = listOf(falseResult, unverifiedResult, trueResult),
        filter = HistoryFilter.UNVERIFIED
    )
}

@Preview(name = "History empty · Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HistoryEmptyLight() = PreviewFrame(dark = false) {
    HistorySurfacePreview(items = emptyList(), filter = HistoryFilter.ALL)
}

@Preview(name = "History empty · Dark", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HistoryEmptyDark() = PreviewFrame(dark = true) {
    HistorySurfacePreview(items = emptyList(), filter = HistoryFilter.ALL)
}

@Preview(name = "Quick check · Light", showBackground = true, widthDp = 390, heightDp = 500)
@Composable
private fun QuickCheckLight() = PreviewFrame(dark = false) {
    QuickCheckContent(
        state = CheckUiState.Finished(falseResult),
        sourceApp = "WhatsApp",
        onSeeFullResult = {},
        onShareCorrection = {},
        onRetry = {},
        onDismiss = {}
    )
}

@Preview(name = "Quick check · Dark", showBackground = true, widthDp = 390, heightDp = 500)
@Composable
private fun QuickCheckDark() = PreviewFrame(dark = true) {
    QuickCheckContent(
        state = CheckUiState.Checking(englishPost, Language.ENGLISH),
        sourceApp = null,
        onSeeFullResult = {},
        onShareCorrection = {},
        onRetry = {},
        onDismiss = {}
    )
}

@Preview(name = "Settings · Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsLight() = PreviewFrame(dark = false) {
    SettingsScreen(
        settings = AppSettings(ThemeMode.SYSTEM, AppLanguage.ENGLISH, onboardingSeen = true),
        onBack = {},
        onThemeModeChange = {},
        onLanguageChange = {},
        onClearHistory = {}
    )
}

@Preview(name = "Settings · Dark", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsDark() = PreviewFrame(dark = true) {
    SettingsScreen(
        settings = AppSettings(ThemeMode.DARK, AppLanguage.SWAHILI, onboardingSeen = true),
        onBack = {},
        onThemeModeChange = {},
        onLanguageChange = {},
        onClearHistory = {}
    )
}

@Preview(name = "Onboarding · Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingLight() = PreviewFrame(dark = false) {
    OnboardingScreen(onDone = {})
}

@Preview(name = "Onboarding · Dark", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingDark() = PreviewFrame(dark = true) {
    OnboardingScreen(onDone = {})
}

/** Preview-only shells so the stateless screens can be rendered without a ViewModel. */
@Composable
internal fun HistorySurfacePreview(
    items: List<CheckResult>,
    filter: HistoryFilter
) {
    HistoryContent(
        items = items,
        filter = filter,
        isLoading = false,
        snackbarHostState = SnackbarHostState(),
        onFilterChange = {},
        onOpen = {},
        onDelete = {},
        onClearRequest = {}
    )
}

@Composable
internal fun ResultSurfacePreview(result: CheckResult) {
    ResultScreenContentOnly(result)
}

@Composable
private fun ResultScreenContentOnly(result: CheckResult) {
    // The real ResultScreen needs navigation callbacks; previews keep them inert.
    ResultScreen(
        result = result,
        onBack = {},
        onShareCorrection = {},
        onSecondaryAction = {},
        secondaryLabel = "Check another",
        onOpenSource = {}
    )
}

@Preview(name = "Button · Light", showBackground = true)
@Composable
private fun ButtonPreview() = PreviewFrame(dark = false) {
    PrimaryButton(text = "Check", onClick = {}, modifier = Modifier.padding(16.dp))
}

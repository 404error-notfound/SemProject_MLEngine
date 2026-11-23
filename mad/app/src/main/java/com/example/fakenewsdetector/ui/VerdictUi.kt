package com.example.fakenewsdetector.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.fakenewsdetector.R
import com.example.fakenewsdetector.domain.ConfidenceLevel
import com.example.fakenewsdetector.ml.Language
import com.example.fakenewsdetector.ml.Verdict
import com.example.fakenewsdetector.ui.theme.verdictColors

/** Text label for a verdict. Verdicts always carry a label, never colour alone. */
@StringRes
fun verdictLabelRes(verdict: Verdict): Int = when (verdict) {
    Verdict.LIKELY_FAKE -> R.string.verdict_likely_false
    Verdict.UNVERIFIED -> R.string.verdict_unverified
    Verdict.LIKELY_GENUINE -> R.string.verdict_likely_true
}

/** False = close, unverified = info, true = check. Never an emoji. */
fun verdictIcon(verdict: Verdict): ImageVector = when (verdict) {
    Verdict.LIKELY_FAKE -> Icons.Filled.Close
    Verdict.UNVERIFIED -> Icons.Filled.Info
    Verdict.LIKELY_GENUINE -> Icons.Filled.Check
}

/** Icon used inside explanation lists and the "what we found" list. */
fun verdictReasonIcon(verdict: Verdict): ImageVector = when (verdict) {
    Verdict.LIKELY_FAKE -> Icons.Filled.Warning
    Verdict.UNVERIFIED -> Icons.Filled.Info
    Verdict.LIKELY_GENUINE -> Icons.Filled.Check
}

@StringRes
fun languageLabelRes(language: Language): Int = when (language) {
    Language.ENGLISH -> R.string.language_english
    Language.SWAHILI -> R.string.language_swahili
    Language.SHENG -> R.string.language_sheng
    Language.MIXED -> R.string.language_mixed
    Language.UNKNOWN -> R.string.language_unknown
}

@StringRes
fun confidenceLabelRes(level: ConfidenceLevel): Int = when (level) {
    ConfidenceLevel.HIGH -> R.string.confidence_high
    ConfidenceLevel.MEDIUM -> R.string.confidence_medium
    ConfidenceLevel.LOW -> R.string.confidence_low
}

/** Container background of a verdict card / pill. */
@Composable
fun verdictContainerColor(verdict: Verdict): Color = verdictColors().let {
    when (verdict) {
        Verdict.LIKELY_FAKE -> it.falseContainer
        Verdict.UNVERIFIED -> it.unverifiedContainer
        Verdict.LIKELY_GENUINE -> it.trueContainer
    }
}

/** Text and icon colour on top of [verdictContainerColor]. */
@Composable
fun verdictOnContainerColor(verdict: Verdict): Color = verdictColors().let {
    when (verdict) {
        Verdict.LIKELY_FAKE -> it.onFalseContainer
        Verdict.UNVERIFIED -> it.onUnverifiedContainer
        Verdict.LIKELY_GENUINE -> it.onTrueContainer
    }
}

/** Accent used for the icon circle and the filled confidence segments. */
@Composable
fun verdictAccentColor(verdict: Verdict): Color = verdictColors().let {
    when (verdict) {
        Verdict.LIKELY_FAKE -> it.falseAccent
        Verdict.UNVERIFIED -> it.unverifiedAccent
        Verdict.LIKELY_GENUINE -> it.trueAccent
    }
}

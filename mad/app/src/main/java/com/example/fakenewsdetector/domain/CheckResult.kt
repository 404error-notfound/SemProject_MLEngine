package com.example.fakenewsdetector.domain

import com.example.fakenewsdetector.ml.Classification
import com.example.fakenewsdetector.ml.FactCheckSource
import com.example.fakenewsdetector.ml.Language
import com.example.fakenewsdetector.ml.Verdict

/** How sure the check is, as the design's three levels. */
enum class ConfidenceLevel { HIGH, MEDIUM, LOW }

/**
 * The app's own view of one check. It is what the UI renders and what history
 * stores, and it is independent of how the answer was produced.
 */
data class CheckResult(
    val text: String,
    val verdict: Verdict,
    val confidence: Float,
    val language: Language,
    val summary: String,
    val reasons: List<String>,
    val sources: List<FactCheckSource> = emptyList(),
    val checkedAt: Long = System.currentTimeMillis(),
    /** Package label of the app the post was shared from, when known. */
    val sourceApp: String? = null,
    /** Room row id once the check is saved. */
    val storedId: Long? = null
) {
    val confidenceLevel: ConfidenceLevel
        get() = when {
            confidence >= 0.75f -> ConfidenceLevel.HIGH
            confidence >= 0.5f -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }

    /** Filled segments of the five-segment meter (always at least one). */
    val confidenceSegments: Int
        get() = Math.round(confidence * 5f).coerceIn(1, 5)
}

/**
 * Map a classifier answer to a stored/rendered result.
 *
 * The classifier contract has no summary/reasons/sources fields, so we derive
 * them conservatively instead of inventing structure: a missing summary falls
 * back to the reason, missing reasons fall back to the single reason sentence,
 * and missing sources simply hide the fact-check section.
 */
fun Classification.toCheckResult(
    text: String,
    checkedAt: Long = System.currentTimeMillis(),
    sourceApp: String? = null,
    storedId: Long? = null
): CheckResult {
    val trimmedReason = reason.trim()
    val resolvedSummary = summary?.trim()?.takeIf { it.isNotEmpty() } ?: trimmedReason
    val resolvedReasons = reasons.map { it.trim() }.filter { it.isNotEmpty() }
        .ifEmpty { if (trimmedReason.isEmpty()) emptyList() else listOf(trimmedReason) }
    return CheckResult(
        text = text,
        verdict = verdict,
        confidence = confidence.coerceIn(0f, 1f),
        language = language,
        summary = resolvedSummary,
        reasons = resolvedReasons,
        sources = sources,
        checkedAt = checkedAt,
        sourceApp = sourceApp,
        storedId = storedId
    )
}

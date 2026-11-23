package com.example.fakenewsdetector.ml

import kotlinx.coroutines.delay

/**
 * PLACEHOLDER classifier so the whole app can be built and demoed before the
 * real model is ready. It does NOT detect misinformation: it only looks for a few
 * common "viral forward" phrases and otherwise gives a stable pseudo-random answer.
 *
 * It returns structured [Classification.summary] and [Classification.reasons] so
 * the result screen can be exercised, and no sources because none exist yet.
 */
class MockClassifier(
    private val languageDetector: LanguageDetector = LanguageDetector()
) : Classifier {

    private val pressureCues = setOf(
        "breaking", "urgent", "leaked", "before it is deleted", "wanataka kuficha"
    )

    private val forwardCues = setOf(
        "forward to", "share before", "lazima share", "sambaza", "tuma sasa", "share"
    )

    override suspend fun classify(text: String): Classification {
        delay(600) // pretend the model is thinking
        val language = languageDetector.detect(text)
        val lower = text.lowercase()
        val pressure = pressureCues.filter { lower.contains(it) }
        val forwards = forwardCues.filter { lower.contains(it) }
        val hasLink = lower.contains("http")
        val hits = pressure.size + forwards.size

        return if (hits > 0) {
            val reasons = buildList {
                if (pressure.isNotEmpty()) {
                    add("Uses urgent or sensational wording (\"${pressure.first()}\").")
                }
                if (forwards.isNotEmpty()) {
                    add("Asks you to forward or share the message before it disappears.")
                }
                if (!hasLink) {
                    add("No named official source or link is cited.")
                }
            }
            Classification(
                verdict = Verdict.LIKELY_FAKE,
                confidence = (0.60f + 0.10f * hits).coerceAtMost(0.95f),
                reason = reasons.joinToString(" "),
                language = language,
                summary = "This has the shape of a viral forward: pressure to resend it and no source to check.",
                reasons = reasons,
                sources = emptyList()
            )
        } else {
            // Stable pseudo-random result so the same text always gives the same answer.
            val bucket = (text.hashCode().toLong() and 0x7fffffff) % 3
            val verdict = Verdict.entries[bucket.toInt()]
            val reason = when (verdict) {
                Verdict.LIKELY_GENUINE ->
                    "Nothing in the wording matches the patterns we flag, but no source could be matched either."
                Verdict.UNVERIFIED ->
                    "We could not match this to a trusted source either way."
                Verdict.LIKELY_FAKE ->
                    "The claim is stated without any official source to check."
            }
            Classification(
                verdict = verdict,
                confidence = 0.55f + (text.length % 30) / 100f,
                reason = reason,
                language = language,
                summary = reason,
                reasons = listOf(reason),
                sources = emptyList()
            )
        }
    }
}

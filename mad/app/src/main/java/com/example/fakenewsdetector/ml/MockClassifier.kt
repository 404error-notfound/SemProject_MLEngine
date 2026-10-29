package com.example.fakenewsdetector.ml

import kotlinx.coroutines.delay

/**
 * PLACEHOLDER classifier so the whole app can be built and demoed before the
 * real model is ready. It does NOT detect misinformation: it only looks for a few
 * common "viral forward" phrases and otherwise gives a stable pseudo-random answer.
 */
class MockClassifier(
    private val languageDetector: LanguageDetector = LanguageDetector()
) : Classifier {

    private val viralCues = listOf(
        "breaking", "forward to", "share before", "before it is deleted",
        "lazima share", "sambaza", "urgent", "wanataka kuficha", "leaked"
    )

    override suspend fun classify(text: String): Classification {
        delay(600) // pretend the model is thinking
        val language = languageDetector.detect(text)
        val lower = text.lowercase()
        val hits = viralCues.filter { lower.contains(it) }

        return if (hits.isNotEmpty()) {
            Classification(
                verdict = Verdict.LIKELY_FAKE,
                confidence = (0.60f + 0.10f * hits.size).coerceAtMost(0.95f),
                reason = "Uses pressure wording often seen in viral forwards (${hits.joinToString { "\"$it\"" }}). " +
                    "No source is cited. (Demo result, not a real model.)",
                language = language
            )
        } else {
            // Stable pseudo-random result so the same text always gives the same answer.
            val bucket = (text.hashCode().toLong() and 0x7fffffff) % 3
            val verdict = Verdict.entries[bucket.toInt()]
            Classification(
                verdict = verdict,
                confidence = 0.55f + (text.length % 30) / 100f,
                reason = "Demo result from the placeholder classifier. Connect the real model to get a real score.",
                language = language
            )
        }
    }
}

package com.example.fakenewsdetector.ml

/** Three-way label (the literature we reviewed mostly used binary fake/genuine). */
enum class Verdict { LIKELY_FAKE, UNVERIFIED, LIKELY_GENUINE }

/** Language mix detected in the text. */
enum class Language { ENGLISH, SWAHILI, SHENG, MIXED, UNKNOWN }

/** A related fact-check the result screen can open in a Custom Tab. */
data class FactCheckSource(
    val title: String,
    val publisher: String? = null,
    val url: String
)

/** What the UI shows after a check. */
data class Classification(
    val verdict: Verdict,
    /** 0.0 to 1.0, how sure the model is about [verdict]. */
    val confidence: Float,
    /** Plain-language explanation so the score is not a black box. */
    val reason: String,
    val language: Language,
    /**
     * Optional one-sentence summary. When the backend does not send one we fall
     * back to [reason]; the UI still renders.
     */
    val summary: String? = null,
    /**
     * Optional ordered explanation bullets. When empty the UI falls back to the
     * single [reason] string so nothing is invented.
     */
    val reasons: List<String> = emptyList(),
    /** Optional related fact-checks. Empty hides the section entirely. */
    val sources: List<FactCheckSource> = emptyList()
)

/**
 * The only thing the rest of the app knows about the AI.
 * Swap [MockClassifier] for [TfliteClassifier] later without touching any UI code.
 */
interface Classifier {
    suspend fun classify(text: String): Classification
}

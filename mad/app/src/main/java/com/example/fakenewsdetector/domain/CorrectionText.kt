package com.example.fakenewsdetector.domain

import com.example.fakenewsdetector.ml.Language
import com.example.fakenewsdetector.ml.Verdict

/**
 * The correction message is assembled from editable string resources so the
 * wording can change without touching code.
 */
data class CorrectionStrings(
    val verdictFalse: String,
    val verdictUnverified: String,
    val verdictTrue: String,
    /** Two placeholders: the verdict line and the one-line reason. */
    val template: String
)

object CorrectionBuilder {

    /** Swahili (and Sheng) posts get a Swahili correction; everything else English. */
    fun useSwahili(language: Language): Boolean =
        language == Language.SWAHILI || language == Language.SHENG

    fun build(result: CheckResult, strings: CorrectionStrings): String {
        val verdictLine = when (result.verdict) {
            Verdict.LIKELY_FAKE -> strings.verdictFalse
            Verdict.UNVERIFIED -> strings.verdictUnverified
            Verdict.LIKELY_GENUINE -> strings.verdictTrue
        }
        val reason = oneLineReason(result)
        return if (reason.isEmpty()) {
            verdictLine
        } else {
            strings.template.format(verdictLine, reason)
        }
    }

    /**
     * First sentence of the strongest reason, on one line, with no links: a
     * correction must be safe to forward as-is.
     */
    fun oneLineReason(result: CheckResult): String {
        val source = result.reasons.firstOrNull()?.takeIf { it.isNotBlank() } ?: result.summary
        val singleLine = source.replace(Regex("\\s+"), " ").trim()
        val withoutLinks = singleLine
            .replace(Regex("https?://\\S+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        val firstSentence = withoutLinks.split(". ").firstOrNull().orEmpty().trim()
        return firstSentence.trimEnd('.', '!', ' ')
    }
}

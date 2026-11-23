package com.example.fakenewsdetector

import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.domain.CorrectionBuilder
import com.example.fakenewsdetector.domain.CorrectionStrings
import com.example.fakenewsdetector.ml.Language
import com.example.fakenewsdetector.ml.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val english = CorrectionStrings(
    verdictFalse = "This message is likely false",
    verdictUnverified = "Please don't share this yet — I couldn't verify it",
    verdictTrue = "This message appears to be accurate",
    template = "%1\$s. %2\$s"
)

private val swahili = CorrectionStrings(
    verdictFalse = "Ujumbe huu una uwezekano mkubwa wa kuwa si wa kweli",
    verdictUnverified = "Tafadhali usisambaze bado — sikuweza kuthibitisha huu ujumbe",
    verdictTrue = "Ujumbe huu unaonekana kuwa wa kweli",
    template = "%1\$s. %2\$s"
)

private fun result(
    language: Language,
    verdict: Verdict = Verdict.LIKELY_FAKE,
    reasons: List<String> = listOf("Asks you to forward it. Real announcements don't depend on sharing.")
) = CheckResult(
    text = "post",
    verdict = verdict,
    confidence = 0.8f,
    language = language,
    summary = "summary",
    reasons = reasons
)

class CorrectionBuilderTest {

    @Test
    fun `swahili posts get the swahili correction`() {
        val message = CorrectionBuilder.build(result(Language.SWAHILI), swahili)
        assertTrue(message.startsWith("Ujumbe huu una uwezekano mkubwa wa kuwa si wa kweli."))
        assertFalse(message.contains("This message is likely false"))
    }

    @Test
    fun `sheng posts also get the swahili correction`() {
        assertTrue(CorrectionBuilder.useSwahili(Language.SHENG))
        val message = CorrectionBuilder.build(result(Language.SHENG), swahili)
        assertTrue(message.contains("Ujumbe huu"))
    }

    @Test
    fun `english and mixed posts get the english correction`() {
        assertFalse(CorrectionBuilder.useSwahili(Language.ENGLISH))
        assertFalse(CorrectionBuilder.useSwahili(Language.MIXED))
        val message = CorrectionBuilder.build(result(Language.ENGLISH), english)
        assertTrue(message.startsWith("This message is likely false."))
        assertTrue(message.contains("Asks you to forward it"))
    }

    @Test
    fun `unverified corrections use the cautious wording`() {
        val message = CorrectionBuilder.build(
            result(Language.ENGLISH, verdict = Verdict.UNVERIFIED),
            english
        )
        assertTrue(message.startsWith("Please don't share this yet"))
    }

    @Test
    fun `correction is one line, one sentence and link free`() {
        val message = CorrectionBuilder.build(
            result(
                Language.ENGLISH,
                reasons = listOf(
                    "First reason with a link https://example.org/tracking\nand a newline. Second sentence."
                )
            ),
            english
        )
        assertFalse(message.contains("http"))
        assertFalse(message.contains("\n"))
        assertFalse(message.contains("Second sentence"))
        assertEquals(
            "This message is likely false. First reason with a link and a newline",
            message
        )
    }
}

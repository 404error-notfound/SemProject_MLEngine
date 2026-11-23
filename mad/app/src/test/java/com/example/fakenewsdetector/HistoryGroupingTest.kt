package com.example.fakenewsdetector

import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.ml.Language
import com.example.fakenewsdetector.ml.Verdict
import com.example.fakenewsdetector.ui.HistoryGroupLabel
import com.example.fakenewsdetector.ui.groupHistory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

private fun item(text: String, timestamp: Long) = CheckResult(
    text = text,
    verdict = Verdict.UNVERIFIED,
    confidence = 0.5f,
    language = Language.ENGLISH,
    summary = "s",
    reasons = listOf("r"),
    checkedAt = timestamp
)

class HistoryGroupingTest {

    @Test
    fun `checks are grouped into today yesterday and earlier`() {
        val zone = ZoneId.of("UTC")
        val today = LocalDate.of(2026, 11, 23)
        val now = today.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()

        val groups = groupHistory(
            items = listOf(
                item("today", today.atTime(9, 0).atZone(zone).toInstant().toEpochMilli()),
                item("yesterday", today.minusDays(1).atTime(9, 0).atZone(zone).toInstant().toEpochMilli()),
                item("earlier", today.minusDays(9).atTime(9, 0).atZone(zone).toInstant().toEpochMilli())
            ),
            now = now,
            zone = zone
        )

        assertEquals(
            listOf(HistoryGroupLabel.TODAY, HistoryGroupLabel.YESTERDAY, HistoryGroupLabel.EARLIER),
            groups.map { it.label }
        )
        assertEquals(listOf("today"), groups[0].items.map { it.text })
        assertEquals(listOf("yesterday"), groups[1].items.map { it.text })
        assertEquals(listOf("earlier"), groups[2].items.map { it.text })
    }
}

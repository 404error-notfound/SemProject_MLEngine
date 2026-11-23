package com.example.fakenewsdetector.ui

import androidx.annotation.StringRes
import com.example.fakenewsdetector.R
import com.example.fakenewsdetector.domain.CheckResult
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HistoryGroupLabel { TODAY, YESTERDAY, EARLIER }

data class HistoryGroup(val label: HistoryGroupLabel, val items: List<CheckResult>)

@StringRes
fun HistoryGroupLabel.labelRes(): Int = when (this) {
    HistoryGroupLabel.TODAY -> R.string.group_today
    HistoryGroupLabel.YESTERDAY -> R.string.group_yesterday
    HistoryGroupLabel.EARLIER -> R.string.group_earlier
}

/** Groups newest-first checks into Today / Yesterday / Earlier. Pure for testing. */
fun groupHistory(
    items: List<CheckResult>,
    now: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault()
): List<HistoryGroup> {
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    val ordered = listOf(HistoryGroupLabel.TODAY, HistoryGroupLabel.YESTERDAY, HistoryGroupLabel.EARLIER)
    return items
        .groupBy { labelFor(it.checkedAt, today, zone) }
        .toSortedMap(compareBy { ordered.indexOf(it) })
        .map { (label, groupItems) -> HistoryGroup(label, groupItems) }
}

private fun labelFor(timestamp: Long, today: LocalDate, zone: ZoneId): HistoryGroupLabel {
    val day = Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()
    return when (day) {
        today -> HistoryGroupLabel.TODAY
        today.minusDays(1) -> HistoryGroupLabel.YESTERDAY
        else -> HistoryGroupLabel.EARLIER
    }
}

/**
 * Row timestamp: time of day for today, "Yesterday", otherwise a short date
 * such as "Sat 21 Nov".
 */
fun formatHistoryTimestamp(
    timestamp: Long,
    yesterdayLabel: String,
    now: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault()
): String {
    val day = Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    return when (day) {
        today -> DateTimeFormatter.ofPattern("HH:mm", locale)
            .format(Instant.ofEpochMilli(timestamp).atZone(zone))
        today.minusDays(1) -> yesterdayLabel
        else -> DateTimeFormatter.ofPattern("EEE d MMM", locale)
            .format(Instant.ofEpochMilli(timestamp).atZone(zone))
    }
}

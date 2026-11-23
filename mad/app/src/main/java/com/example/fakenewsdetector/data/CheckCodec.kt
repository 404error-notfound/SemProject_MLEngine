package com.example.fakenewsdetector.data

import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.ml.FactCheckSource
import com.example.fakenewsdetector.ml.Language
import com.example.fakenewsdetector.ml.Verdict

/**
 * Delimited encoding for the list columns. The separators are control
 * characters that cannot appear in post text, so no escaping is required and
 * no extra serialisation dependency is pulled in.
 */
internal object CheckCodec {
    private const val ITEM = '\u001F'
    private const val FIELD = '\u001E'

    fun encodeReasons(items: List<String>): String = items.joinToString(ITEM.toString())

    fun decodeReasons(raw: String): List<String> =
        if (raw.isEmpty()) emptyList()
        else raw.split(ITEM).map { it.trim() }.filter { it.isNotEmpty() }

    fun encodeSources(items: List<FactCheckSource>): String =
        items.joinToString(ITEM.toString()) { source ->
            listOf(source.title, source.publisher.orEmpty(), source.url)
                .joinToString(FIELD.toString())
        }

    fun decodeSources(raw: String): List<FactCheckSource> =
        if (raw.isEmpty()) emptyList()
        else raw.split(ITEM).mapNotNull { row ->
            val parts = row.split(FIELD)
            if (parts.size < 3 || parts[2].isBlank()) return@mapNotNull null
            FactCheckSource(
                title = parts[0],
                publisher = parts[1].ifBlank { null },
                url = parts[2]
            )
        }
}

/** Verdict and language are stored by enum name and decoded defensively. */
internal fun String.toVerdictOrUnverified(): Verdict =
    runCatching { Verdict.valueOf(this) }.getOrDefault(Verdict.UNVERIFIED)

internal fun String.toLanguageOrUnknown(): Language =
    runCatching { Language.valueOf(this) }.getOrDefault(Language.UNKNOWN)

fun CheckResult.toEntity(): CheckEntity = CheckEntity(
    id = storedId ?: 0L,
    text = text,
    verdict = verdict.name,
    confidence = confidence,
    reason = reasons.joinToString(" "),
    summary = summary,
    reasons = CheckCodec.encodeReasons(reasons),
    sources = CheckCodec.encodeSources(sources),
    language = language.name,
    sourceApp = sourceApp,
    createdAt = checkedAt
)

fun CheckEntity.toCheckResult(): CheckResult {
    val decodedReasons = CheckCodec.decodeReasons(reasons)
        .ifEmpty { listOf(reason).filter { it.isNotBlank() } }
    return CheckResult(
        text = text,
        verdict = verdict.toVerdictOrUnverified(),
        confidence = confidence,
        language = language.toLanguageOrUnknown(),
        summary = summary.ifBlank { reason },
        reasons = decodedReasons,
        sources = CheckCodec.decodeSources(sources),
        checkedAt = createdAt,
        sourceApp = sourceApp,
        storedId = id
    )
}

package com.example.fakenewsdetector.ml

/**
 * Very simple word-list language detector for English / Swahili / Sheng.
 * It is a starting point: extend the word lists using the team's dataset,
 * or replace it with a trained model later.
 */
class LanguageDetector {

    private val swahili = setOf(
        "na", "ya", "wa", "kwa", "ni", "si", "hii", "huu", "hiyo", "yake", "yao",
        "serikali", "rais", "watu", "kenya", "habari", "uchaguzi", "kura", "sasa",
        "sana", "kama", "lakini", "kwamba", "pia", "wote", "leo", "kesho", "jana",
        "mimi", "wewe", "yeye", "tunataka", "hakuna", "kuna", "mambo", "pesa"
    )

    private val sheng = setOf(
        "mbogi", "manze", "msee", "wasee", "sherehe", "ocha", "mtaa", "ngori",
        "doh", "kuchoma", "cheki", "nare", "dem", "buda", "form", "poa",
        "fiti", "vibe", "kejani", "mathree", "chapaa", "aje", "nduthi"
    )

    private val english = setOf(
        "the", "and", "is", "are", "to", "of", "in", "that", "this", "for", "with",
        "government", "president", "election", "news", "people", "share", "will",
        "have", "has", "was", "were", "not", "from", "they", "all", "breaking"
    )

    fun detect(text: String): Language {
        val words = text.lowercase()
            .split(Regex("[^\\p{L}']+"))
            .filter { it.isNotBlank() }
        if (words.isEmpty()) return Language.UNKNOWN

        val sw = words.count { it in swahili }
        val sh = words.count { it in sheng }
        val en = words.count { it in english }
        val total = sw + sh + en
        if (total == 0) return Language.UNKNOWN

        val swShare = sw.toFloat() / total
        val shShare = sh.toFloat() / total
        val enShare = en.toFloat() / total

        return when {
            // Two or more languages each carry a meaningful share: code-switched text.
            listOf(swShare, shShare, enShare).count { it >= 0.25f } >= 2 -> Language.MIXED
            shShare > 0f && shShare >= swShare && shShare >= enShare -> Language.SHENG
            swShare >= enShare -> Language.SWAHILI
            else -> Language.ENGLISH
        }
    }
}

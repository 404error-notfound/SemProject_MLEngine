package com.example.fakenewsdetector.ui

import androidx.compose.ui.graphics.Color
import com.example.fakenewsdetector.ml.Language
import com.example.fakenewsdetector.ml.Verdict

/** Display helpers shared by the Check and History screens. */

fun Verdict.label(): String = when (this) {
    Verdict.LIKELY_FAKE -> "Likely misinformation"
    Verdict.UNVERIFIED -> "Unverified"
    Verdict.LIKELY_GENUINE -> "Likely genuine"
}

fun Verdict.color(): Color = when (this) {
    Verdict.LIKELY_FAKE -> Color(0xFFC62828)
    Verdict.UNVERIFIED -> Color(0xFFEF6C00)
    Verdict.LIKELY_GENUINE -> Color(0xFF2E7D32)
}

fun Language.label(): String = when (this) {
    Language.ENGLISH -> "English"
    Language.SWAHILI -> "Swahili"
    Language.SHENG -> "Sheng"
    Language.MIXED -> "Mixed (code-switched)"
    Language.UNKNOWN -> "Unknown"
}

fun String.toVerdict(): Verdict =
    runCatching { Verdict.valueOf(this) }.getOrDefault(Verdict.UNVERIFIED)

fun String.toLanguage(): Language =
    runCatching { Language.valueOf(this) }.getOrDefault(Language.UNKNOWN)

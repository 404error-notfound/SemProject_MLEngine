package com.example.fakenewsdetector.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Material shape scale used by stock components. */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * The explicit scale from the design. Composables reference these names
 * instead of inventing radii.
 */
object ShapeTokens {
    /** Chips and filter chips. */
    val chip = RoundedCornerShape(8.dp)

    /** Text fields. */
    val field = RoundedCornerShape(12.dp)

    /** Cards and list rows. */
    val card = RoundedCornerShape(16.dp)

    /** Buttons, fully rounded. */
    val button = RoundedCornerShape(24.dp)

    /** Verdict cards. */
    val verdictCard = RoundedCornerShape(24.dp)

    /** Top corners of bottom sheets. */
    val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    /** Confidence meter segments. */
    val segment = RoundedCornerShape(4.dp)
}

/** Spacing scale: 8dp base unit. */
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

/** Fixed component sizes. */
object Sizes {
    /** Screen side margins. */
    val screenMargin = 16.dp

    /** Minimum touch target. */
    val minTouchTarget = 48.dp

    val icon = 24.dp
    val verdictIcon = 30.dp
    val stepCircle = 32.dp
    val verdictIconCircle = 56.dp
    val progressBarHeight = 4.dp
    val confidenceSegmentHeight = 8.dp
    val confidenceSegmentWidth = 28.dp
    val confidenceSegmentGap = 6.dp
    val listChevron = 20.dp
    val navPillIcon = 24.dp
}

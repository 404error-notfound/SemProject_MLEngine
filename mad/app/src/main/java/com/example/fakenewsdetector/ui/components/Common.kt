package com.example.fakenewsdetector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fakenewsdetector.R
import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.ml.Language
import com.example.fakenewsdetector.ml.Verdict
import com.example.fakenewsdetector.ui.confidenceLabelRes
import com.example.fakenewsdetector.ui.languageLabelRes
import com.example.fakenewsdetector.ui.verdictAccentColor
import com.example.fakenewsdetector.ui.verdictContainerColor
import com.example.fakenewsdetector.ui.verdictIcon
import com.example.fakenewsdetector.ui.verdictLabelRes
import com.example.fakenewsdetector.ui.verdictReasonIcon
import com.example.fakenewsdetector.ui.verdictOnContainerColor
import com.example.fakenewsdetector.ui.theme.ShapeTokens
import com.example.fakenewsdetector.ui.theme.Sizes
import com.example.fakenewsdetector.ui.theme.Spacing

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ShapeTokens.button,
        contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.m),
        modifier = modifier.heightIn(min = Sizes.minTouchTarget)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Spacing.s))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = ShapeTokens.button,
        contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.m),
        modifier = modifier.heightIn(min = Sizes.minTouchTarget)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Small verdict label used in list rows and compact sheet headers. */
@Composable
fun VerdictPill(verdict: Verdict, modifier: Modifier = Modifier) {
    Surface(
        color = verdictContainerColor(verdict),
        contentColor = verdictOnContainerColor(verdict),
        shape = ShapeTokens.chip,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xs)
        ) {
            Icon(
                imageVector = verdictIcon(verdict),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(verdictLabelRes(verdict)),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        }
    }
}

/**
 * Five-segment confidence meter. The filled count is also announced as text so
 * confidence is never conveyed by colour alone.
 */
@Composable
fun ConfidenceMeter(filled: Int, verdict: Verdict, modifier: Modifier = Modifier) {
    val accent = verdictAccentColor(verdict)
    val description = stringResource(R.string.confidence_meter_desc, filled)
    Row(
        horizontalArrangement = Arrangement.spacedBy(Sizes.confidenceSegmentGap),
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = description
        }
    ) {
        repeat(5) { index ->
            Box(
                Modifier
                    .width(Sizes.confidenceSegmentWidth)
                    .height(Sizes.confidenceSegmentHeight)
                    .clip(ShapeTokens.segment)
                    .background(if (index < filled) accent else accent.copy(alpha = 0.30f))
            )
        }
    }
}

/**
 * The verdict card. Always shows icon + "VERDICT" + verdict name + confidence,
 * so the answer survives colour blindness, greyscale and TalkBack.
 */
@Composable
fun VerdictCard(
    result: CheckResult,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val container = verdictContainerColor(result.verdict)
    val onContainer = verdictOnContainerColor(result.verdict)
    val accent = verdictAccentColor(result.verdict)
    val confidence = stringResource(confidenceLabelRes(result.confidenceLevel))

    Surface(
        color = container,
        contentColor = onContainer,
        shape = ShapeTokens.verdictCard,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
    ) {
        Column(
            modifier = Modifier.padding(if (compact) Spacing.l else Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.l)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(if (compact) Sizes.minTouchTarget else Sizes.verdictIconCircle)
                        .clip(CircleShape)
                        .background(accent)
                ) {
                    Icon(
                        imageVector = verdictIcon(result.verdict),
                        contentDescription = null,
                        tint = container,
                        modifier = Modifier.size(if (compact) Sizes.icon else Sizes.verdictIcon)
                    )
                }
                Column {
                    if (!compact) {
                        Text(
                            text = stringResource(R.string.verdict_eyebrow),
                            style = MaterialTheme.typography.labelMedium,
                            color = onContainer.copy(alpha = 0.85f)
                        )
                    }
                    Text(
                        text = stringResource(verdictLabelRes(result.verdict)),
                        style = if (compact) {
                            MaterialTheme.typography.titleLarge
                        } else {
                            MaterialTheme.typography.headlineMedium
                        },
                        color = onContainer
                    )
                    if (compact) {
                        Text(
                            text = confidence,
                            style = MaterialTheme.typography.bodyMedium,
                            color = onContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            if (!compact) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    Text(
                        text = confidence,
                        style = MaterialTheme.typography.titleMedium,
                        color = onContainer,
                        modifier = Modifier.weight(1f)
                    )
                    ConfidenceMeter(filled = result.confidenceSegments, verdict = result.verdict)
                }
            }
        }
    }
}

/** Post preview card on the checking and result screens. */
@Composable
fun PostCard(
    text: String,
    language: Language,
    modifier: Modifier = Modifier,
    collapsedLines: Int = 2
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val languageLabel = stringResource(languageLabelRes(language))

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = ShapeTokens.card,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.post_checked_label, languageLabel),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = stringResource(
                            if (expanded) R.string.icon_collapse_post else R.string.icon_expand_post
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = if (expanded) Int.MAX_VALUE else collapsedLines,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Tonal explainer card, e.g. "Skip the copy and paste". */
@Composable
fun TonalInfoCard(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = ShapeTokens.card,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Spacing.l),
            horizontalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(Sizes.minTouchTarget)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Sizes.icon)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Bulleted explanation list. Uses the verdict's accent for its leading icon so
 * the scan path matches the verdict card without relying on colour alone.
 */
@Composable
fun ReasonList(
    items: List<String>,
    verdict: Verdict,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: androidx.compose.ui.graphics.Color? = null
) {
    val leading = icon ?: verdictReasonIcon(verdict)
    val iconTint = tint ?: verdictAccentColor(verdict)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                Icon(
                    imageVector = leading,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .padding(top = Spacing.xxs)
                        .size(20.dp)
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


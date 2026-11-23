package com.example.fakenewsdetector.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.fakenewsdetector.R
import com.example.fakenewsdetector.ui.components.PrimaryButton
import com.example.fakenewsdetector.ui.theme.Sizes
import com.example.fakenewsdetector.ui.theme.Spacing

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Sizes.screenMargin, vertical = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            ShareIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Spacing.xxl * 5)
            )
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.onboarding_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OnboardingPoint(Icons.Filled.Share, stringResource(R.string.onboarding_point_share))
            OnboardingPoint(Icons.Filled.History, stringResource(R.string.onboarding_point_history))
            OnboardingPoint(Icons.Filled.Translate, stringResource(R.string.onboarding_point_language))
            Spacer(Modifier.height(Spacing.s))
            PrimaryButton(
                text = stringResource(R.string.action_get_started),
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            )
            androidx.compose.material3.TextButton(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.action_skip))
            }
        }
    }
}

@Composable
private fun OnboardingPoint(icon: ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier.size(Spacing.xxl)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(Spacing.xs)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * A simple chat-card + share-arrow illustration drawn from theme colours, so it
 * follows light/dark without shipping two bitmaps.
 */
@Composable
private fun ShareIllustration(modifier: Modifier = Modifier) {
    val bubbleColor = MaterialTheme.colorScheme.surfaceContainer
    val accent = MaterialTheme.colorScheme.primary
    val strongBubble = MaterialTheme.colorScheme.secondaryContainer
    val line = MaterialTheme.colorScheme.outlineVariant
    val description = stringResource(R.string.onboarding_title)

    Canvas(modifier = modifier.semantics { contentDescription = description }) {
        val corner = CornerRadius(size.height * 0.08f, size.height * 0.08f)
        val bubbleHeight = size.height * 0.26f
        val leftBubble = Size(size.width * 0.62f, bubbleHeight)

        drawRoundRect(
            color = bubbleColor,
            topLeft = Offset(size.width * 0.04f, size.height * 0.08f),
            size = leftBubble,
            cornerRadius = corner
        )
        drawRoundRect(
            color = strongBubble,
            topLeft = Offset(size.width * 0.34f, size.height * 0.38f),
            size = Size(size.width * 0.60f, bubbleHeight),
            cornerRadius = corner
        )

        // Text lines inside the bubbles.
        repeat(2) { index ->
            val y = size.height * (0.16f + index * 0.07f)
            drawLine(
                color = line,
                start = Offset(size.width * 0.10f, y),
                end = Offset(size.width * (0.52f - index * 0.10f), y),
                strokeWidth = size.height * 0.02f
            )
        }
        repeat(2) { index ->
            val y = size.height * (0.46f + index * 0.07f)
            drawLine(
                color = line,
                start = Offset(size.width * 0.40f, y),
                end = Offset(size.width * (0.82f - index * 0.10f), y),
                strokeWidth = size.height * 0.02f
            )
        }

        // Share arrow rising from the second bubble.
        val arrowX = size.width * 0.78f
        val arrowBottom = size.height * 0.90f
        val arrowTop = size.height * 0.68f
        drawLine(accent, Offset(arrowX, arrowBottom), Offset(arrowX, arrowTop), strokeWidth = size.height * 0.025f)
        val head = Path().apply {
            moveTo(arrowX, arrowTop)
            lineTo(arrowX - size.width * 0.05f, arrowTop + size.height * 0.08f)
            lineTo(arrowX + size.width * 0.05f, arrowTop + size.height * 0.08f)
            close()
        }
        drawPath(head, accent)

        // A check badge marks the verdict.
        drawCircle(
            color = accent,
            radius = size.height * 0.10f,
            center = Offset(size.width * 0.14f, size.height * 0.80f)
        )
        val tick = Path().apply {
            moveTo(size.width * 0.10f, size.height * 0.80f)
            lineTo(size.width * 0.13f, size.height * 0.84f)
            lineTo(size.width * 0.19f, size.height * 0.75f)
        }
        drawPath(tick, bubbleColor, style = Stroke(width = size.height * 0.03f))
    }
}

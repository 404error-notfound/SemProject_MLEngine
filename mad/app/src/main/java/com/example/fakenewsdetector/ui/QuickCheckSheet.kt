package com.example.fakenewsdetector.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fakenewsdetector.R
import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.ui.components.PrimaryButton
import com.example.fakenewsdetector.ui.components.SecondaryButton
import com.example.fakenewsdetector.ui.components.VerdictCard
import com.example.fakenewsdetector.ui.theme.ShapeTokens
import com.example.fakenewsdetector.ui.theme.Sizes
import com.example.fakenewsdetector.ui.theme.Spacing
import com.example.fakenewsdetector.viewmodel.CheckUiState

/**
 * Content of the sheet shown over the source app after a share. The answer is
 * available without opening the main app.
 */
@Composable
fun QuickCheckContent(
    state: CheckUiState,
    sourceApp: String?,
    onSeeFullResult: (CheckResult) -> Unit,
    onShareCorrection: (CheckResult) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl)
            .padding(bottom = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.l)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            Icon(
                Icons.Filled.Share,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = sourceApp?.let { stringResource(R.string.share_from_source, it) }
                    ?: stringResource(R.string.share_from_unknown),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when (val current = state) {
            is CheckUiState.Checking -> CheckingRows()
            is CheckUiState.Finished -> FinishedRows(
                result = current.result,
                onSeeFullResult = onSeeFullResult,
                onShareCorrection = onShareCorrection,
                onDismiss = onDismiss
            )
            is CheckUiState.Failed -> {
                Text(
                    text = stringResource(current.error.titleRes()),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = current.error.bodyText(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    PrimaryButton(
                        text = stringResource(R.string.action_retry),
                        onClick = onRetry,
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryButton(
                        text = stringResource(R.string.action_dismiss),
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            CheckUiState.Idle -> Unit
        }
    }
}

@Composable
private fun CheckingRows() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
        Text(
            text = stringResource(R.string.checking_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(Sizes.progressBarHeight)
                .clip(ShapeTokens.segment),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )
        Text(
            text = stringResource(R.string.checking_helper),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FinishedRows(
    result: CheckResult,
    onSeeFullResult: (CheckResult) -> Unit,
    onShareCorrection: (CheckResult) -> Unit,
    onDismiss: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
        VerdictCard(result = result, compact = true)
        Text(
            text = result.summary,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        PrimaryButton(
            text = stringResource(R.string.action_see_full_result),
            onClick = { onSeeFullResult(result) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            SecondaryButton(
                text = stringResource(R.string.action_share_correction),
                onClick = { onShareCorrection(result) },
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_dismiss),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

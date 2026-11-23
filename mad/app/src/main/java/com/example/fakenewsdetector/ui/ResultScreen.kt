package com.example.fakenewsdetector.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.fakenewsdetector.R
import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.ml.FactCheckSource
import com.example.fakenewsdetector.ml.Verdict
import com.example.fakenewsdetector.ui.components.PostCard
import com.example.fakenewsdetector.ui.components.PrimaryButton
import com.example.fakenewsdetector.ui.components.ReasonList
import com.example.fakenewsdetector.ui.components.SectionTitle
import com.example.fakenewsdetector.ui.components.SecondaryButton
import com.example.fakenewsdetector.ui.components.VerdictCard
import com.example.fakenewsdetector.ui.theme.Sizes
import com.example.fakenewsdetector.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    result: CheckResult,
    onBack: () -> Unit,
    onShareCorrection: () -> Unit,
    onSecondaryAction: () -> Unit,
    secondaryLabel: String,
    onOpenSource: (FactCheckSource) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(result) {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val isUnverified = result.verdict == Verdict.UNVERIFIED
    val primaryLabel = stringResource(
        if (isUnverified) R.string.action_share_caution else R.string.action_share_correction
    )

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.result_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.icon_back_desc)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = Sizes.screenMargin, vertical = Spacing.m)
                    ) {
                        PrimaryButton(
                            text = primaryLabel,
                            onClick = onShareCorrection,
                            icon = Icons.Filled.Share,
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryButton(
                            text = secondaryLabel,
                            onClick = onSecondaryAction,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Sizes.screenMargin, vertical = Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            VerdictCard(result = result)

            Text(
                text = result.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            PostCard(text = result.text, language = result.language)

            if (isUnverified) {
                SectionTitle(stringResource(R.string.what_we_found))
                ReasonList(items = result.reasons, verdict = result.verdict)
                SectionTitle(stringResource(R.string.what_you_can_do))
                ReasonList(
                    items = listOf(
                        stringResource(R.string.action_do_1),
                        stringResource(R.string.action_do_2),
                        stringResource(R.string.action_do_3)
                    ),
                    verdict = result.verdict,
                    icon = Icons.Filled.Check,
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                SectionTitle(stringResource(R.string.why_we_think_so))
                ReasonList(items = result.reasons, verdict = result.verdict)
            }

            if (result.sources.isNotEmpty()) {
                SectionTitle(stringResource(R.string.related_fact_checks))
                Column {
                    result.sources.forEachIndexed { index, source ->
                        SourceRow(source = source, onClick = { onOpenSource(source) })
                        if (index != result.sources.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.result_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Spacing.l)
            )
        }
    }
}

@Composable
private fun SourceRow(source: FactCheckSource, onClick: () -> Unit) {
    val openDescription = stringResource(R.string.icon_open_source_desc)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.minTouchTarget)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.m)
            .semantics { contentDescription = "${source.title}, $openDescription" }
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = source.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!source.publisher.isNullOrBlank()) {
                Text(
                    text = source.publisher,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Sizes.listChevron)
        )
    }
}

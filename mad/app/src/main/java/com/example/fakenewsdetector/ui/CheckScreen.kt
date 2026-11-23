package com.example.fakenewsdetector.ui

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fakenewsdetector.R
import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.ml.Language
import com.example.fakenewsdetector.ui.components.PrimaryButton
import com.example.fakenewsdetector.ui.components.ScreenTitle
import com.example.fakenewsdetector.ui.components.SecondaryButton
import com.example.fakenewsdetector.ui.components.TonalInfoCard
import com.example.fakenewsdetector.ui.theme.ShapeTokens
import com.example.fakenewsdetector.ui.theme.Sizes
import com.example.fakenewsdetector.ui.theme.Spacing
import com.example.fakenewsdetector.viewmodel.CheckError
import com.example.fakenewsdetector.viewmodel.CheckUiState
import com.example.fakenewsdetector.viewmodel.CheckViewModel
import kotlinx.coroutines.delay

/** Wires the ViewModel to the stateless [CheckContent]. */
@Composable
fun CheckScreen(
    viewModel: CheckViewModel,
    onOpenSettings: () -> Unit,
    onOpenResult: (CheckResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val input by viewModel.input.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language by viewModel.detectedLanguage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var suggestionVisible by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(state) {
        val current = state
        if (current is CheckUiState.Finished) {
            onOpenResult(current.result)
            viewModel.consumeResult()
        }
    }

    CheckContent(
        input = input,
        detectedLanguage = language,
        state = state,
        showPasteSuggestion = suggestionVisible && input.isBlank(),
        onInputChange = viewModel::onInputChange,
        onPaste = {
            readClipboard(context)?.let { text ->
                viewModel.paste(text)
                suggestionVisible = false
            } ?: Toast.makeText(context, R.string.clipboard_empty, Toast.LENGTH_SHORT).show()
        },
        onDismissPasteSuggestion = { suggestionVisible = false },
        onCheck = { viewModel.analyze() },
        onClear = viewModel::clear,
        onCancel = viewModel::cancel,
        onRetry = viewModel::retry,
        onOpenSettings = onOpenSettings,
        modifier = modifier
    )
}

/** Stateless Check screen: idle/ready, checking (in place) and error. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckContent(
    input: String,
    detectedLanguage: Language,
    state: CheckUiState,
    showPasteSuggestion: Boolean,
    onInputChange: (String) -> Unit,
    onPaste: () -> Unit,
    onDismissPasteSuggestion: () -> Unit,
    onCheck: () -> Unit,
    onClear: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is CheckUiState.Checking -> CheckingContent(
            text = state.text,
            language = state.language,
            onCancel = onCancel,
            modifier = modifier
        )
        else -> IdleContent(
            input = input,
            detectedLanguage = detectedLanguage,
            error = (state as? CheckUiState.Failed)?.error,
            showPasteSuggestion = showPasteSuggestion,
            onInputChange = onInputChange,
            onPaste = onPaste,
            onDismissPasteSuggestion = onDismissPasteSuggestion,
            onCheck = onCheck,
            onClear = onClear,
            onRetry = onRetry,
            onOpenSettings = onOpenSettings,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IdleContent(
    input: String,
    detectedLanguage: Language,
    error: CheckError?,
    showPasteSuggestion: Boolean,
    onInputChange: (String) -> Unit,
    onPaste: () -> Unit,
    onDismissPasteSuggestion: () -> Unit,
    onCheck: () -> Unit,
    onClear: () -> Unit,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuOpen by remember { mutableStateOf(false) }
    val canCheck = input.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Sizes.screenMargin, vertical = Sizes.screenMargin),
        verticalArrangement = Arrangement.spacedBy(Spacing.l)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ScreenTitle(stringResource(R.string.check_title), Modifier.weight(1f))
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.icon_settings_desc),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.settings_title)) },
                        onClick = {
                            menuOpen = false
                            onOpenSettings()
                        },
                        leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) }
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.check_helper),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (showPasteSuggestion) {
            PasteSuggestion(onPaste = onPaste, onDismiss = onDismissPasteSuggestion)
        }

        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 6,
            maxLines = 12,
            shape = ShapeTokens.field,
            label = { Text(stringResource(R.string.post_text_label)) },
            placeholder = { Text(stringResource(R.string.post_text_placeholder)) },
            textStyle = MaterialTheme.typography.bodyLarge,
            isError = error != null,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onPaste) {
                Icon(
                    Icons.Filled.ContentPaste,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(Spacing.s))
                Text(stringResource(R.string.paste_from_clipboard))
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(
                    R.string.char_counter,
                    input.length,
                    CheckViewModel.MAX_CHARS
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (input.isNotBlank() && detectedLanguage != Language.UNKNOWN) {
            LanguageChip(detectedLanguage)
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            PrimaryButton(
                text = stringResource(R.string.action_check),
                onClick = onCheck,
                enabled = canCheck,
                icon = Icons.Filled.Search
            )
            SecondaryButton(
                text = stringResource(R.string.action_clear),
                onClick = onClear
            )
        }

        if (error != null) {
            ErrorCard(error = error, onRetry = onRetry)
        }

        TonalInfoCard(
            icon = Icons.Filled.Share,
            title = stringResource(R.string.skip_title),
            body = stringResource(R.string.skip_body)
        )
    }
}

@Composable
private fun LanguageChip(language: Language) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = ShapeTokens.chip
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s)
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = stringResource(
                    R.string.language_detected,
                    stringResource(languageLabelRes(language))
                ),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun PasteSuggestion(onPaste: () -> Unit, onDismiss: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Surface(
            onClick = onPaste,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = ShapeTokens.chip
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                modifier = Modifier
                    .height(Sizes.minTouchTarget)
                    .padding(horizontal = Spacing.m)
            ) {
                Icon(
                    Icons.Filled.ContentPaste,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.paste_suggestion),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        IconButton(onClick = onDismiss) {
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.action_dismiss),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CheckingContent(
    text: String,
    language: Language,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        R.string.step_read_post,
        R.string.step_spot_claim,
        R.string.step_search_sources,
        R.string.step_compare,
        R.string.step_write_result
    )
    // Visual-only progress: the backend has no progress events. The last step is
    // never completed here; the result screen replaces this only when it arrives.
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (step < steps.lastIndex - 1) {
            delay(1_400)
            step += 1
        }
    }
    val progress by animateFloatAsState(
        targetValue = (step + 1).toFloat() / steps.size * 0.85f,
        animationSpec = tween(durationMillis = 700),
        label = "checkProgress"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Sizes.screenMargin),
        verticalArrangement = Arrangement.spacedBy(Spacing.l)
    ) {
        ScreenTitle(stringResource(R.string.checking_title))
        Text(
            text = stringResource(R.string.checking_helper),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = ShapeTokens.card
        ) {
            Column(
                modifier = Modifier.padding(Spacing.l),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = stringResource(
                        R.string.your_post_language,
                        stringResource(languageLabelRes(language))
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(Sizes.progressBarHeight)
                .clip(ShapeTokens.segment),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            steps.forEachIndexed { index, labelRes ->
                StepRow(
                    label = stringResource(labelRes),
                    done = index < step,
                    active = index == step
                )
            }
        }

        SecondaryButton(text = stringResource(R.string.action_cancel), onClick = onCancel)
    }
}

@Composable
private fun StepRow(label: String, done: Boolean, active: Boolean) {
    val outline = MaterialTheme.colorScheme.outline
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.l)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(Sizes.stepCircle)) {
            when {
                done -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(Sizes.stepCircle)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                active -> {
                    Box(
                        Modifier
                            .size(Sizes.stepCircle)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                else -> Box(
                    Modifier
                        .size(Sizes.stepCircle)
                        .clip(CircleShape)
                        .border(1.dp, outline, CircleShape)
                )
            }
        }
        Text(
            text = label,
            style = if (active) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
            color = when {
                active -> MaterialTheme.colorScheme.onSurface
                done -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun ErrorCard(error: CheckError, onRetry: () -> Unit) {
    val title = error.titleRes()
    val body = error.bodyText()

    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = ShapeTokens.card,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
            }
            Text(body, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
}

/** Reads the clipboard only when the user asked for it. */
private fun readClipboard(context: Context): String? {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        ?: return null
    val clip = manager.primaryClip ?: return null
    if (clip.itemCount == 0) return null
    return clip.getItemAt(0).coerceToText(context)?.toString()?.takeIf { it.isNotBlank() }
}

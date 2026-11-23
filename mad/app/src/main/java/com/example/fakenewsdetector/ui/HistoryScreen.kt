package com.example.fakenewsdetector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fakenewsdetector.R
import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.ui.components.ScreenTitle
import com.example.fakenewsdetector.ui.components.VerdictPill
import com.example.fakenewsdetector.ui.theme.ShapeTokens
import com.example.fakenewsdetector.ui.theme.Sizes
import com.example.fakenewsdetector.ui.theme.Spacing
import com.example.fakenewsdetector.viewmodel.HistoryFilter
import com.example.fakenewsdetector.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onOpenResult: (CheckResult) -> Unit,
    onRechecked: (CheckResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pendingUndo by viewModel.pendingUndo.collectAsStateWithLifecycle()
    val rechecked by viewModel.rechecked.collectAsStateWithLifecycle()
    val recheckError by viewModel.recheckError.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var confirmClear by remember { mutableStateOf(false) }

    LaunchedEffect(pendingUndo) {
        val pending = pendingUndo ?: return@LaunchedEffect
        val outcome = snackbarHostState.showSnackbar(
            message = context.getString(R.string.check_deleted),
            actionLabel = context.getString(R.string.action_undo)
        )
        if (outcome == SnackbarResult.ActionPerformed) {
            viewModel.undoDelete()
        } else {
            viewModel.dismissedUndo()
        }
    }

    LaunchedEffect(rechecked) {
        rechecked?.let {
            onRechecked(it)
            viewModel.consumeRechecked()
        }
    }

    LaunchedEffect(recheckError) {
        val error = recheckError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(context.getString(error.titleRes()))
        viewModel.consumeRecheckError()
    }

    HistoryContent(
        items = state.filtered,
        filter = state.filter,
        isLoading = state.isLoading,
        snackbarHostState = snackbarHostState,
        onFilterChange = viewModel::setFilter,
        onOpen = onOpenResult,
        onDelete = viewModel::delete,
        onClearRequest = { confirmClear = true },
        modifier = modifier
    )

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.clear_history_title)) },
            text = { Text(stringResource(R.string.clear_history_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    viewModel.clearAll()
                }) { Text(stringResource(R.string.action_clear_history)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun HistoryContent(
    items: List<CheckResult>,
    filter: HistoryFilter,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    onFilterChange: (HistoryFilter) -> Unit,
    onOpen: (CheckResult) -> Unit,
    onDelete: (CheckResult) -> Unit,
    onClearRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Sizes.screenMargin)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ScreenTitle(stringResource(R.string.history_title), Modifier.weight(1f))
                if (items.isNotEmpty()) {
                    IconButton(onClick = onClearRequest) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.icon_clear_history_desc),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            FilterRow(filter = filter, onFilterChange = onFilterChange)

            when {
                isLoading -> Unit
                items.isEmpty() -> EmptyHistory()
                else -> HistoryList(
                    items = items,
                    onOpen = onOpen,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun FilterRow(filter: HistoryFilter, onFilterChange: (HistoryFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = Spacing.s),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        HistoryFilter.entries.forEach { entry ->
            val label = when (entry) {
                HistoryFilter.ALL -> R.string.filter_all
                HistoryFilter.LIKELY_FALSE -> R.string.filter_likely_false
                HistoryFilter.UNVERIFIED -> R.string.filter_unverified
                HistoryFilter.LIKELY_TRUE -> R.string.filter_likely_true
            }
            val selected = entry == filter
            FilterChip(
                selected = selected,
                onClick = { onFilterChange(entry) },
                label = { Text(stringResource(label)) },
                leadingIcon = if (selected) {
                    {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
private fun EmptyHistory() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.xxl * 2),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = stringResource(R.string.history_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.xl)
        )
    }
}

@Composable
private fun HistoryList(
    items: List<CheckResult>,
    onOpen: (CheckResult) -> Unit,
    onDelete: (CheckResult) -> Unit
) {
    val groups = groupHistory(items)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = Spacing.xxl)
    ) {
        groups.forEach { group ->
            item(key = "header-${group.label.name}") {
                Text(
                    text = stringResource(group.label.labelRes()),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.m, bottom = Spacing.xs)
                )
            }
            items(group.items, key = { it.storedId ?: it.hashCode().toLong() }) { item ->
                SwipeableHistoryRow(
                    item = item,
                    onOpen = { onOpen(item) },
                    onDelete = { onDelete(item) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableHistoryRow(
    item: CheckResult,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        ShapeTokens.card
                    )
                    .padding(horizontal = Spacing.l),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        HistoryRow(item = item, onOpen = onOpen)
    }
}

@Composable
private fun HistoryRow(item: CheckResult, onOpen: () -> Unit) {
    val time = formatHistoryTimestamp(
        timestamp = item.checkedAt,
        yesterdayLabel = stringResource(R.string.relative_yesterday)
    )
    Surface(
        onClick = onOpen,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = ShapeTokens.card,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Spacing.m)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    VerdictPill(item.verdict)
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.icon_open_check_desc),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = Spacing.s)
                    .size(Sizes.listChevron)
            )
        }
    }
}

package com.example.fakenewsdetector.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fakenewsdetector.data.CheckEntity
import com.example.fakenewsdetector.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Screen 2: every past check, newest first, with delete and clear-all. */
@Composable
fun HistoryScreen(viewModel: HistoryViewModel, modifier: Modifier = Modifier) {
    val items by viewModel.history.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("History", style = MaterialTheme.typography.headlineSmall)
            if (items.isNotEmpty()) {
                TextButton(onClick = viewModel::clearAll) { Text("Clear all") }
            }
        }

        if (items.isEmpty()) {
            Text(
                "No checks yet. Posts you check will appear here.",
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    HistoryItem(item, onDelete = { viewModel.delete(item.id) })
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(item: CheckEntity, onDelete: () -> Unit) {
    val verdict = item.verdict.toVerdict()
    val date = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
        .format(Date(item.createdAt))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "${verdict.label()} · ${(item.confidence * 100).toInt()}%",
                color = verdict.color(),
                fontWeight = FontWeight.Bold
            )
            Text(item.text, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text(
                "$date · ${item.language.toLanguage().label()}",
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

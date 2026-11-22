package com.example.fakenewsdetector.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.example.fakenewsdetector.ml.Classification
import com.example.fakenewsdetector.viewmodel.CheckUiState
import com.example.fakenewsdetector.viewmodel.CheckViewModel

/**
 * Screen 1: paste or receive text, run the check, show verdict + score + reason.
 * [sharedText] is text that arrived through the Android share sheet.
 */
@Composable
fun CheckScreen(
    viewModel: CheckViewModel,
    sharedText: String?,
    onSharedTextHandled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var input by remember { mutableStateOf("") }

    // Text shared from WhatsApp/X etc. is checked automatically.
    LaunchedEffect(sharedText) {
        if (!sharedText.isNullOrBlank()) {
            input = sharedText
            viewModel.analyze(sharedText)
            onSharedTextHandled()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Check a post", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Paste a claim, or share it to this app from WhatsApp, X or Telegram.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            label = { Text("Post text (English, Swahili or Sheng)") }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { viewModel.analyze(input) },
                enabled = input.isNotBlank() && state !is CheckUiState.Loading
            ) { Text("Check") }

            OutlinedButton(onClick = {
                input = ""
                viewModel.reset()
            }) { Text("Clear") }
        }

        when (val s = state) {
            CheckUiState.Idle -> Unit
            CheckUiState.Loading -> CircularProgressIndicator()
            is CheckUiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
            is CheckUiState.Done -> ResultCard(s.result)
        }
    }
}

@Composable
private fun ResultCard(result: Classification) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                result.verdict.label(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = result.verdict.color()
            )
            Text("Confidence: ${(result.confidence * 100).toInt()}%")
            LinearProgressIndicator(
                progress = { result.confidence },
                modifier = Modifier.fillMaxWidth(),
                color = result.verdict.color()
            )
            Spacer(Modifier.height(4.dp))
            Text("Why", fontWeight = FontWeight.SemiBold)
            Text(result.reason)
            Text(
                "Language detected: ${result.language.label()}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "This is an automated estimate, not a fact-check. Verify with a trusted source.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

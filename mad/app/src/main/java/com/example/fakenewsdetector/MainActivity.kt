package com.example.fakenewsdetector

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fakenewsdetector.ui.CheckScreen
import com.example.fakenewsdetector.ui.HistoryScreen
import com.example.fakenewsdetector.viewmodel.CheckViewModel
import com.example.fakenewsdetector.viewmodel.HistoryViewModel

class MainActivity : ComponentActivity() {

    // Text handed over by ShareReceiverActivity; cleared once the Check screen used it.
    private var sharedText by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedText = intent?.getStringExtra(EXTRA_SHARED_TEXT)

        setContent {
            MaterialTheme {
                AppRoot(
                    sharedText = sharedText,
                    onSharedTextHandled = { sharedText = null }
                )
            }
        }
    }

    // Called when the app is already open and the user shares something new.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedText = intent.getStringExtra(EXTRA_SHARED_TEXT)
    }

    companion object {
        const val EXTRA_SHARED_TEXT = "shared_text"
    }
}

@Composable
private fun AppRoot(sharedText: String?, onSharedTextHandled: () -> Unit) {
    val checkViewModel: CheckViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()
    var tab by remember { mutableIntStateOf(0) }

    // A new share always jumps back to the Check tab.
    if (sharedText != null && tab != 0) tab = 0

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Text("🔍") },
                    label = { Text("Check") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Text("🕘") },
                    label = { Text("History") }
                )
            }
        }
    ) { padding ->
        when (tab) {
            0 -> CheckScreen(
                viewModel = checkViewModel,
                sharedText = sharedText,
                onSharedTextHandled = onSharedTextHandled,
                modifier = Modifier.padding(padding)
            )
            else -> HistoryScreen(
                viewModel = historyViewModel,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

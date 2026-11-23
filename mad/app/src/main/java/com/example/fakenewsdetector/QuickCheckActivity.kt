package com.example.fakenewsdetector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fakenewsdetector.data.SettingsStore
import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.ui.QuickCheckContent
import com.example.fakenewsdetector.ui.SystemBarIcons
import com.example.fakenewsdetector.ui.sharePlainText
import com.example.fakenewsdetector.ui.theme.FakeNewsDetectorTheme
import com.example.fakenewsdetector.ui.theme.ShapeTokens
import com.example.fakenewsdetector.ui.theme.isDark
import com.example.fakenewsdetector.viewmodel.CheckViewModel

/**
 * Translucent, dialog-themed activity: the user stays in WhatsApp/X/Telegram
 * with a bottom sheet on top.
 */
class QuickCheckActivity : ComponentActivity() {

    private val container: AppContainer
        get() = (application as FakeNewsApp).container

    private val checkViewModel: CheckViewModel by viewModels {
        CheckViewModel.factory(container)
    }

    override fun attachBaseContext(newBase: Context) {
        val language = SettingsStore(newBase).settings.value.language
        super.attachBaseContext(SettingsStore.wrap(newBase, language))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedText = intent.getCharSequenceExtra(ShareReceiverActivity.EXTRA_SHARED_TEXT)?.toString()
        val sourceApp = intent.getStringExtra(ShareReceiverActivity.EXTRA_SOURCE_APP)

        if (savedInstanceState == null && !sharedText.isNullOrBlank()) {
            checkViewModel.analyze(sharedText, sourceApp)
        }

        setContent {
            val settings by container.settings.settings.collectAsStateWithLifecycle()
            FakeNewsDetectorTheme(mode = settings.themeMode) {
                SystemBarIcons(window = window, dark = settings.themeMode.isDark())
                val state by checkViewModel.state.collectAsStateWithLifecycle()
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                ModalBottomSheet(
                    onDismissRequest = { finish() },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.background,
                    shape = ShapeTokens.sheet
                ) {
                    QuickCheckContent(
                        state = state,
                        sourceApp = sourceApp,
                        onSeeFullResult = { result -> openFullResult(result) },
                        onShareCorrection = { result ->
                            sharePlainText(
                                this@QuickCheckActivity,
                                container.correctionFactory.build(result),
                                getString(R.string.share_chooser_title)
                            )
                        },
                        onRetry = checkViewModel::retry,
                        onDismiss = { finish() }
                    )
                }
            }
        }
    }

    private fun openFullResult(result: CheckResult) {
        val open = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_RESULT_ID, result.storedId ?: -1L)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(open)
        finish()
    }
}
